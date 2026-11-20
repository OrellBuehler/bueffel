package ch.orellbuehler.bueffel.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.store.PackSummary
import ch.orellbuehler.bueffel.store.SamplePack
import ch.orellbuehler.bueffel.store.Stats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn

data class LibraryState(
    val loaded: Boolean = false,
    val packs: List<PackSummary> = emptyList(),
    val stats: Stats? = null,
    val samples: List<SamplePack> = emptyList(),
) {
    val continuePack: PackSummary? get() = pickContinuePack(packs)
    val totalDue: Int get() = packs.sumOf { it.dueCount }
}

fun pickContinuePack(packs: List<PackSummary>): PackSummary? {
    val recent = compareByDescending<PackSummary> { it.lastStudiedAt }
    val due = packs.filter { it.dueCount > 0 }.sortedWith(compareByDescending<PackSummary> { it.dueCount }.then(recent))
    if (due.isNotEmpty()) return due.first()
    return packs.filter { it.newCount > 0 }.sortedWith(recent).firstOrNull()
}

class LibraryViewModel(
    private val sdk: BueffelSdk,
) : ViewModel() {
    private val samples by lazy { sdk.samplePacks() }

    val state: StateFlow<LibraryState> =
        combine(sdk.observePacks(), sdk.observeStats(7)) { packs, stats ->
            LibraryState(loaded = true, packs = packs, stats = stats, samples = samples)
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState())
}
