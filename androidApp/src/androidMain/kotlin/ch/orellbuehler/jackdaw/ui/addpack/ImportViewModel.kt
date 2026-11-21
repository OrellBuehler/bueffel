package ch.orellbuehler.jackdaw.ui.addpack

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.data.PackTextReader
import ch.orellbuehler.jackdaw.data.ReadOutcome
import ch.orellbuehler.jackdaw.format.ImportError
import ch.orellbuehler.jackdaw.store.ImportPreview
import ch.orellbuehler.jackdaw.store.ImportResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ImportProblem {
    data class Sdk(
        val error: ImportError,
    ) : ImportProblem

    data object ReadFailed : ImportProblem

    data object NotUtf8 : ImportProblem

    data object Empty : ImportProblem

    data object TooLarge : ImportProblem
}

sealed interface ImportUiState {
    data object Idle : ImportUiState

    data object Reading : ImportUiState

    data class Preview(
        val text: String,
        val preview: ImportPreview.Valid,
        val busy: Boolean = false,
    ) : ImportUiState

    data class Failed(
        val problem: ImportProblem,
    ) : ImportUiState
}

sealed interface ImportEvent {
    data class Imported(
        val result: ImportResult.Success,
        val fromSample: Boolean,
    ) : ImportEvent

    data class SampleFailed(
        val problem: ImportProblem,
    ) : ImportEvent
}

class ImportViewModel(
    private val sdk: JackdawSdk,
    private val reader: PackTextReader,
) : ViewModel() {
    private val _state = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val state: StateFlow<ImportUiState> = _state

    private val _events = Channel<ImportEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val samples = sdk.samplePacks()

    val authoringPrompt: String by lazy { sdk.authoringPrompt() }

    val installedIds: StateFlow<Set<String>> =
        sdk
            .observePacks()
            .map { packs -> packs.mapTo(HashSet()) { it.id } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun openUri(uri: Uri) {
        _state.value = ImportUiState.Reading
        viewModelScope.launch {
            when (val outcome = reader.read(uri)) {
                is ReadOutcome.Text -> preview(outcome.text)
                is ReadOutcome.Failed -> _state.value = ImportUiState.Failed(outcome.problem)
            }
        }
    }

    fun openText(text: String) {
        _state.value = ImportUiState.Reading
        viewModelScope.launch { preview(text) }
    }

    private suspend fun preview(text: String) {
        if (text.isBlank()) {
            _state.value = ImportUiState.Failed(ImportProblem.Empty)
            return
        }
        _state.value =
            when (val preview = sdk.previewImport(text)) {
                is ImportPreview.Valid -> ImportUiState.Preview(text, preview)
                is ImportPreview.Invalid -> ImportUiState.Failed(ImportProblem.Sdk(preview.error))
            }
    }

    fun confirm() {
        val current = _state.value as? ImportUiState.Preview ?: return
        if (current.busy) return
        _state.value = current.copy(busy = true)
        viewModelScope.launch {
            val result =
                if (current.preview.isDowngrade) {
                    sdk.importPackAllowingDowngrade(current.text)
                } else {
                    sdk.importPack(current.text)
                }
            when (result) {
                is ImportResult.Success -> {
                    _state.value = ImportUiState.Idle
                    _events.send(ImportEvent.Imported(result, fromSample = false))
                }
                is ImportResult.Failure -> _state.value = ImportUiState.Failed(ImportProblem.Sdk(result.error))
            }
        }
    }

    fun dismiss() {
        _state.update { if (it is ImportUiState.Preview && it.busy) it else ImportUiState.Idle }
    }

    fun addSample(sampleId: String) {
        viewModelScope.launch {
            when (val result = sdk.importSample(sampleId)) {
                is ImportResult.Success -> _events.send(ImportEvent.Imported(result, fromSample = true))
                is ImportResult.Failure -> _events.send(ImportEvent.SampleFailed(ImportProblem.Sdk(result.error)))
            }
        }
    }
}
