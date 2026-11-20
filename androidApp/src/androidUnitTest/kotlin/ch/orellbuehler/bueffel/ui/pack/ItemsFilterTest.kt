package ch.orellbuehler.bueffel.ui.pack

import app.cash.turbine.test
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.MainDispatcherRule
import ch.orellbuehler.bueffel.itemSummary
import ch.orellbuehler.bueffel.packDetail
import ch.orellbuehler.bueffel.store.ItemStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class ItemsFilterTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val items =
        listOf(
            itemSummary("1", title = "el agua", subtitle = "water", status = ItemStatus.NEW),
            itemSummary("2", title = "el fuego", subtitle = "fire", status = ItemStatus.LEARNING),
            itemSummary("3", title = "la tierra", subtitle = "Earth", status = ItemStatus.MASTERED),
        )

    @Test
    fun emptyQueryAndNoStatusKeepsEverything() {
        assertEquals(3, filterItems(items, "", null).size)
    }

    @Test
    fun searchMatchesTitleAndSubtitleIgnoringCase() {
        assertEquals(listOf("1"), filterItems(items, "AGUA", null).map { it.id })
        assertEquals(listOf("3"), filterItems(items, "earth", null).map { it.id })
        assertEquals(emptyList(), filterItems(items, "zzz", null).map { it.id })
    }

    @Test
    fun statusFilterNarrowsTheList() {
        assertEquals(listOf("2"), filterItems(items, "", ItemStatus.LEARNING).map { it.id })
        assertEquals(listOf("2"), filterItems(items, "fu", ItemStatus.LEARNING).map { it.id })
        assertEquals(emptyList(), filterItems(items, "agua", ItemStatus.LEARNING).map { it.id })
    }

    @Test
    fun surroundingWhitespaceInTheQueryIsIgnored() {
        assertEquals(listOf("1"), filterItems(items, "  agua ", null).map { it.id })
    }

    @Test
    fun viewModelAppliesQueryAndStatus() =
        runTest {
            val sdk = mockk<BueffelSdk>()
            every { sdk.observeItems("p") } returns flowOf(items)
            every { sdk.observePack("p") } returns flowOf(packDetail())
            val vm = ItemsViewModel(sdk, "p")
            vm.state.test {
                var state = awaitItem()
                while (!state.loaded) state = awaitItem()
                assertEquals(3, state.visible.size)
                vm.setQuery("el")
                state = awaitItem()
                assertEquals(2, state.visible.size)
                vm.setStatus(ItemStatus.NEW)
                state = awaitItem()
                assertEquals(listOf("1"), state.visible.map { it.id })
            }
        }
}
