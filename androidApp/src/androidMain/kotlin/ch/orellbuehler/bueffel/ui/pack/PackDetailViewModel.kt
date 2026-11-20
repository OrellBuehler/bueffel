package ch.orellbuehler.bueffel.ui.pack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.store.PackDetail
import ch.orellbuehler.bueffel.study.SessionConfig
import ch.orellbuehler.bueffel.study.SessionPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PackDetailState {
    data object Loading : PackDetailState

    data object Missing : PackDetailState

    data class Ready(
        val detail: PackDetail,
        val learn: SessionPreview,
    ) : PackDetailState
}

class PackDetailViewModel(
    private val sdk: BueffelSdk,
    val packId: String,
) : ViewModel() {
    val state: StateFlow<PackDetailState> =
        sdk
            .observePack(packId)
            .mapLatest { detail ->
                if (detail == null) {
                    PackDetailState.Missing
                } else {
                    PackDetailState.Ready(detail, sdk.previewSession(packId, SessionConfig.learn()))
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PackDetailState.Loading)

    fun resetProgress() {
        viewModelScope.launch { sdk.resetProgress(packId) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            sdk.deletePack(packId)
            onDeleted()
        }
    }

    suspend fun exportJson(): String? = sdk.exportPack(packId)
}
