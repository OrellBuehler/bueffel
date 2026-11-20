package ch.orellbuehler.bueffel.ui.nav

import ch.orellbuehler.bueffel.study.PracticeFilter
import ch.orellbuehler.bueffel.study.SessionConfig
import ch.orellbuehler.bueffel.study.SessionMode
import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute

@Serializable
data object StatsRoute

@Serializable
data object SettingsRoute

@Serializable
data object OnboardingRoute

@Serializable
data object AddPackRoute

@Serializable
data class PackRoute(
    val packId: String,
)

@Serializable
data class ItemsRoute(
    val packId: String,
)

@Serializable
data class StudyRoute(
    val packId: String,
    val mode: String = SessionMode.LEARN.name,
    val sectionId: String? = null,
    val filter: String = PracticeFilter.ALL.name,
) {
    fun toConfig(): SessionConfig =
        SessionConfig(
            mode = SessionMode.entries.firstOrNull { it.name == mode } ?: SessionMode.LEARN,
            sectionId = sectionId,
            size = 0,
            newLimit = -1,
            filter = PracticeFilter.entries.firstOrNull { it.name == filter } ?: PracticeFilter.ALL,
            rescheduleInPractice = false,
        )
}
