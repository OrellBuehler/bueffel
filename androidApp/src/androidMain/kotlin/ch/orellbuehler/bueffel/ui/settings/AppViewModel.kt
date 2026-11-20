package ch.orellbuehler.bueffel.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.store.AppSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val sdk: BueffelSdk,
) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        sdk.observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun finishOnboarding() {
        viewModelScope.launch { sdk.setOnboardingSeen(true) }
    }

    fun refresh() = sdk.refresh()
}
