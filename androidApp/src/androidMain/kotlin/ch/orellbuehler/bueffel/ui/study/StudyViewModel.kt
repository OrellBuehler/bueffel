package ch.orellbuehler.bueffel.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.store.Stats
import ch.orellbuehler.bueffel.study.Answer
import ch.orellbuehler.bueffel.study.Grade
import ch.orellbuehler.bueffel.study.SessionConfig
import ch.orellbuehler.bueffel.study.SessionMode
import ch.orellbuehler.bueffel.study.SessionState
import ch.orellbuehler.bueffel.study.SessionSummary
import ch.orellbuehler.bueffel.study.StudySession
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface StudyUiState {
    data object Loading : StudyUiState

    data class Empty(
        val mode: SessionMode,
    ) : StudyUiState

    data class Active(
        val step: Int,
        val state: SessionState,
    ) : StudyUiState

    data class Done(
        val summary: SessionSummary,
        val stats: Stats?,
    ) : StudyUiState

    data object Closed : StudyUiState
}

data class StudyPackInfo(
    val title: String,
    val icon: String?,
    val color: String?,
)

class StudyViewModel(
    private val sdk: BueffelSdk,
    private val packId: String,
    private val config: SessionConfig,
) : ViewModel() {
    private val _ui = MutableStateFlow<StudyUiState>(StudyUiState.Loading)
    val ui: StateFlow<StudyUiState> = _ui

    private val _pack = MutableStateFlow<StudyPackInfo?>(null)
    val pack: StateFlow<StudyPackInfo?> = _pack

    private var session: StudySession? = null
    private var collector: Job? = null
    private var practiceInstead = false

    val mode: SessionMode get() = if (practiceInstead) SessionMode.PRACTICE else config.mode
    val sectionId: String? get() = config.sectionId

    init {
        viewModelScope.launch {
            sdk
                .observePack(packId)
                .first()
                ?.summary
                ?.let { _pack.value = StudyPackInfo(it.title, it.icon, it.color) }
        }
        start()
    }

    private fun activeConfig(): SessionConfig = if (practiceInstead) config.copy(mode = SessionMode.PRACTICE) else config

    private fun start() {
        collector?.cancel()
        _ui.value = StudyUiState.Loading
        collector =
            viewModelScope.launch {
                val started = sdk.startSession(packId, activeConfig())
                session = started
                var step = 0
                var previous: SessionState? = null
                started.state.collect { state ->
                    _ui.value =
                        when (state) {
                            is SessionState.Finished ->
                                when {
                                    state.summary.abandoned -> StudyUiState.Closed
                                    state.summary.total == 0 -> StudyUiState.Empty(started.mode)
                                    else -> StudyUiState.Done(state.summary, runCatching { sdk.observeStats(1).first() }.getOrNull())
                                }
                            else -> {
                                val startsNewStep =
                                    state is SessionState.Question && (previous == null || previous is SessionState.Feedback)
                                if (startsNewStep || previous == null) step++
                                StudyUiState.Active(step, state)
                            }
                        }
                    previous = state
                }
            }
    }

    fun restart() = start()

    fun practiceInstead() {
        practiceInstead = true
        start()
    }

    fun reveal() {
        session?.reveal()
    }

    fun revealHint() {
        session?.revealHint()
    }

    fun submit(answer: Answer) {
        val current = session ?: return
        viewModelScope.launch { current.submit(answer) }
    }

    fun selfGrade(grade: Grade) {
        val current = session ?: return
        viewModelScope.launch { current.selfGrade(grade) }
    }

    fun next() {
        session?.next()
    }

    fun finish() {
        session?.finish()
    }
}
