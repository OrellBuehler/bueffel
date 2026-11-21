package ch.orellbuehler.jackdaw.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.store.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val sdk: JackdawSdk,
) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        sdk.observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun finishOnboarding() {
        viewModelScope.launch { sdk.setOnboardingSeen(true) }
    }

    fun refresh() = sdk.refresh()
}
