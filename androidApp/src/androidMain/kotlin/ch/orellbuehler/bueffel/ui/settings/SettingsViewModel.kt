package ch.orellbuehler.bueffel.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.bueffel.BueffelSdk
import ch.orellbuehler.bueffel.store.AppSettings
import ch.orellbuehler.bueffel.store.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sdk: BueffelSdk,
) : ViewModel() {
    val settings: StateFlow<AppSettings?> =
        sdk.observeSettings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setDailyGoal(goal: Int) {
        viewModelScope.launch { sdk.setDailyGoal(goal) }
    }

    fun setSessionSize(size: Int) {
        viewModelScope.launch { sdk.setDefaultSessionSize(size) }
    }

    fun setTheme(theme: ThemePreference) {
        viewModelScope.launch { sdk.setTheme(theme) }
    }
}
