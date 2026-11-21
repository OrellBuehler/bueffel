package ch.orellbuehler.jackdaw.ui.library

import app.cash.turbine.test
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.MainDispatcherRule
import ch.orellbuehler.jackdaw.packSummary
import ch.orellbuehler.jackdaw.statsOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LibraryViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    @Test
    fun picksTheBusiestPackWithDueItems() {
        val packs =
            listOf(
                packSummary("a", due = 2, lastStudiedAt = 5),
                packSummary("b", due = 9, lastStudiedAt = 1),
                packSummary("c", due = 0, new = 30),
            )
        assertEquals("b", pickContinuePack(packs)?.id)
    }

    @Test
    fun breaksTiesByRecentStudy() {
        val packs = listOf(packSummary("a", due = 4, lastStudiedAt = 5), packSummary("b", due = 4, lastStudiedAt = 9))
        assertEquals("b", pickContinuePack(packs)?.id)
    }

    @Test
    fun fallsBackToPacksWithNewItems() {
        val packs = listOf(packSummary("a", new = 3, lastStudiedAt = 1), packSummary("b", new = 8, lastStudiedAt = 4))
        assertEquals("b", pickContinuePack(packs)?.id)
    }

    @Test
    fun hasNoTargetWhenEverythingIsDone() {
        assertNull(pickContinuePack(listOf(packSummary("a"))))
        assertNull(pickContinuePack(emptyList()))
    }

    @Test
    fun combinesPacksAndStats() =
        runTest {
            val sdk = mockk<JackdawSdk>()
            val packs = MutableStateFlow(listOf(packSummary("a", due = 3)))
            every { sdk.observePacks() } returns packs
            every { sdk.observeStats(7) } returns flowOf(statsOf(streak = 4, reviewsToday = 5))
            every { sdk.samplePacks() } returns emptyList()
            val viewModel = LibraryViewModel(sdk)
            viewModel.state.test {
                var state = awaitItem()
                while (!state.loaded) state = awaitItem()
                assertEquals(4, state.stats?.currentStreak)
                assertEquals(3, state.totalDue)
                assertEquals("a", state.continuePack?.id)
                assertFalse(state.packs.isEmpty())
                packs.value = emptyList()
                state = awaitItem()
                assertTrue(state.packs.isEmpty())
                assertNull(state.continuePack)
            }
        }
}
