package ch.orellbuehler.jackdaw.ui.settings

import app.cash.turbine.test
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.MainDispatcherRule
import ch.orellbuehler.jackdaw.settingsOf
import ch.orellbuehler.jackdaw.store.ThemePreference
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SettingsViewModelTest {
    @get:Rule
    val main = MainDispatcherRule()

    private val sdk = mockk<JackdawSdk>(relaxed = true)

    @Test
    fun settingsAreForwardedToTheSdk() =
        runTest {
            every { sdk.observeSettings() } returns MutableStateFlow(settingsOf())
            val vm = SettingsViewModel(sdk)
            vm.setDailyGoal(35)
            vm.setSessionSize(15)
            vm.setTheme(ThemePreference.DARK)
            coVerify { sdk.setDailyGoal(35) }
            coVerify { sdk.setDefaultSessionSize(15) }
            coVerify { sdk.setTheme(ThemePreference.DARK) }
        }

    @Test
    fun appViewModelWaitsForSettingsAndMarksOnboarding() =
        runTest {
            val flow = MutableStateFlow(settingsOf(onboardingSeen = false))
            every { sdk.observeSettings() } returns flow
            coEvery { sdk.setOnboardingSeen(true) } returns Unit
            val vm = AppViewModel(sdk)
            vm.settings.test {
                var current = awaitItem()
                if (current == null) current = awaitItem()
                assertEquals(false, current?.onboardingSeen)
                vm.finishOnboarding()
                flow.value = settingsOf(onboardingSeen = true)
                assertEquals(true, awaitItem()?.onboardingSeen)
            }
            coVerify { sdk.setOnboardingSeen(true) }
        }

    @Test
    fun startsWithoutSettings() {
        every { sdk.observeSettings() } returns MutableStateFlow(settingsOf())
        val vm = SettingsViewModel(sdk)
        assertNull(vm.settings.value)
    }
}
