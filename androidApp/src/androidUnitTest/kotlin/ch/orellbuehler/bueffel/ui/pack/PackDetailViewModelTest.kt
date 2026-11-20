package ch.orellbuehler.bueffel.ui.pack

import app.cash.turbine.test
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.MainDispatcherRule
import ch.orellbuehler.bueffel.packDetail
import ch.orellbuehler.bueffel.packSummary
import ch.orellbuehler.bueffel.study.SessionPreview
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class PackDetailViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val sdk = mockk<BueffelSdk>(relaxed = true)

    @Test
    fun combinesDetailAndLearnPreview() =
        runTest {
            every { sdk.observePack("p") } returns MutableStateFlow(packDetail(packSummary("p", due = 2, new = 5)))
            coEvery { sdk.previewSession("p", any()) } returns SessionPreview(2, 5, 7)
            val vm = PackDetailViewModel(sdk, "p")
            vm.state.test {
                var state = awaitItem()
                while (state is PackDetailState.Loading) state = awaitItem()
                val ready = assertIs<PackDetailState.Ready>(state)
                assertEquals(7, ready.learn.totalCount)
                assertEquals(2, ready.detail.summary.dueCount)
            }
        }

    @Test
    fun missingPackIsReported() =
        runTest {
            every { sdk.observePack("gone") } returns MutableStateFlow(null)
            val vm = PackDetailViewModel(sdk, "gone")
            vm.state.test {
                var state = awaitItem()
                while (state is PackDetailState.Loading) state = awaitItem()
                assertIs<PackDetailState.Missing>(state)
            }
        }

    @Test
    fun resetAndDeleteReachTheSdk() =
        runTest {
            every { sdk.observePack("p") } returns MutableStateFlow(null)
            val vm = PackDetailViewModel(sdk, "p")
            var deleted = false
            vm.resetProgress()
            vm.delete { deleted = true }
            coVerify { sdk.resetProgress("p") }
            coVerify { sdk.deletePack("p") }
            assertEquals(true, deleted)
        }

    @Test
    fun exportReturnsTheStoredJson() =
        runTest {
            every { sdk.observePack("p") } returns MutableStateFlow(null)
            coEvery { sdk.exportPack("p") } returns "{\"x\":1}"
            assertEquals("{\"x\":1}", PackDetailViewModel(sdk, "p").exportJson())
        }
}
