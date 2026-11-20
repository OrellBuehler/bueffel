package ch.orellbuehler.bueffel.ui.addpack

import android.net.Uri
import app.cash.turbine.test
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.MainDispatcherRule
import ch.orellbuehler.bueffel.data.PackTextReader
import ch.orellbuehler.bueffel.data.ReadOutcome
import ch.orellbuehler.bueffel.format.ImportError
import ch.orellbuehler.bueffel.format.ImportErrorCode
import ch.orellbuehler.bueffel.packSummary
import ch.orellbuehler.bueffel.store.ImportCounts
import ch.orellbuehler.bueffel.store.ImportPreview
import ch.orellbuehler.bueffel.store.ImportResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ImportViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val sdk = mockk<BueffelSdk>()
    private var readOutcome: ReadOutcome = ReadOutcome.Text("{}")
    private val reader = PackTextReader { readOutcome }

    private fun counts() = ImportCounts(0, 0, 0, 0)

    private fun valid(
        isUpdate: Boolean = false,
        isDowngrade: Boolean = false,
    ) = ImportPreview.Valid(
        packId = "p",
        title = "Pack",
        descriptionText = null,
        icon = null,
        color = null,
        language = null,
        revision = 1,
        itemCount = 3,
        sectionCount = 0,
        typeCounts = emptyList(),
        skippedCount = 0,
        warnings = emptyList(),
        isUpdate = isUpdate,
        installedTitle = null,
        installedRevision = 0,
        isDowngrade = isDowngrade,
        counts = counts(),
    )

    private fun success(isUpdate: Boolean = false) = ImportResult.Success("p", "Pack", 3, 0, isUpdate, counts(), emptyList())

    private fun viewModel(): ImportViewModel {
        every { sdk.samplePacks() } returns emptyList()
        every { sdk.observePacks() } returns flowOf(listOf(packSummary("p")))
        return ImportViewModel(sdk, reader)
    }

    @Test
    fun previewsValidTextWithoutImporting() =
        runTest {
            coEvery { sdk.previewImport("text") } returns valid()
            val vm = viewModel()
            vm.openText("text")
            val state = assertIs<ImportUiState.Preview>(vm.state.value)
            assertEquals("Pack", state.preview.title)
            coVerify(exactly = 0) { sdk.importPack(any()) }
        }

    @Test
    fun reportsInvalidPacksAsFailures() =
        runTest {
            val error = ImportError(ImportErrorCode.NOT_JSON, "The file is not valid JSON.")
            coEvery { sdk.previewImport("oops") } returns ImportPreview.Invalid(error)
            val vm = viewModel()
            vm.openText("oops")
            val state = assertIs<ImportUiState.Failed>(vm.state.value)
            assertEquals(ImportProblem.Sdk(error), state.problem)
            assertEquals("The file is not valid JSON.", state.problem.technicalDetail())
        }

    @Test
    fun blankTextIsRejectedLocally() =
        runTest {
            val vm = viewModel()
            vm.openText("   ")
            assertEquals(ImportUiState.Failed(ImportProblem.Empty), vm.state.value)
            coVerify(exactly = 0) { sdk.previewImport(any()) }
        }

    @Test
    fun readFailuresSurfaceWithoutCallingTheSdk() =
        runTest {
            readOutcome = ReadOutcome.Failed(ImportProblem.NotUtf8)
            val vm = viewModel()
            vm.openUri(mockk<Uri>())
            assertEquals(ImportUiState.Failed(ImportProblem.NotUtf8), vm.state.value)
        }

    @Test
    fun filesAreReadThenPreviewed() =
        runTest {
            readOutcome = ReadOutcome.Text("file text")
            coEvery { sdk.previewImport("file text") } returns valid(isUpdate = true)
            val vm = viewModel()
            vm.openUri(mockk<Uri>())
            assertTrue(assertIs<ImportUiState.Preview>(vm.state.value).preview.isUpdate)
        }

    @Test
    fun confirmingImportsAndEmitsAnEvent() =
        runTest {
            coEvery { sdk.previewImport("text") } returns valid()
            coEvery { sdk.importPack("text") } returns success()
            val vm = viewModel()
            vm.openText("text")
            vm.events.test {
                vm.confirm()
                val event = assertIs<ImportEvent.Imported>(awaitItem())
                assertEquals("p", event.result.packId)
                assertEquals(false, event.fromSample)
            }
            assertEquals(ImportUiState.Idle, vm.state.value)
        }

    @Test
    fun downgradesGoThroughTheDowngradeEntryPoint() =
        runTest {
            coEvery { sdk.previewImport("old") } returns valid(isDowngrade = true)
            coEvery { sdk.importPackAllowingDowngrade("old") } returns success()
            val vm = viewModel()
            vm.openText("old")
            vm.confirm()
            coVerify { sdk.importPackAllowingDowngrade("old") }
            coVerify(exactly = 0) { sdk.importPack(any()) }
        }

    @Test
    fun importFailureKeepsTheUserInformed() =
        runTest {
            val error = ImportError(ImportErrorCode.STORAGE_ERROR, "disk full")
            coEvery { sdk.previewImport("text") } returns valid()
            coEvery { sdk.importPack("text") } returns ImportResult.Failure(error)
            val vm = viewModel()
            vm.openText("text")
            vm.confirm()
            assertEquals(ImportUiState.Failed(ImportProblem.Sdk(error)), vm.state.value)
        }

    @Test
    fun dismissResetsToIdle() =
        runTest {
            coEvery { sdk.previewImport("text") } returns valid()
            val vm = viewModel()
            vm.openText("text")
            vm.dismiss()
            assertEquals(ImportUiState.Idle, vm.state.value)
        }

    @Test
    fun samplesImportDirectlyAndReportTheOutcome() =
        runTest {
            coEvery { sdk.importSample("s1") } returns success()
            coEvery { sdk.importSample("nope") } returns ImportResult.Failure(ImportError(ImportErrorCode.UNKNOWN_SAMPLE, "unknown"))
            val vm = viewModel()
            vm.events.test {
                vm.addSample("s1")
                assertEquals(true, assertIs<ImportEvent.Imported>(awaitItem()).fromSample)
                vm.addSample("nope")
                assertIs<ImportEvent.SampleFailed>(awaitItem())
            }
        }

    @Test
    fun installedIdsFollowTheLibrary() =
        runTest {
            val vm = viewModel()
            vm.installedIds.test {
                var ids = awaitItem()
                while (ids.isEmpty()) ids = awaitItem()
                assertEquals(setOf("p"), ids)
            }
        }
}
