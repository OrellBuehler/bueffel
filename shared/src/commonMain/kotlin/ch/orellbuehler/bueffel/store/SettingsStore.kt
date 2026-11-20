package ch.orellbuehler.bueffel.store

import ch.orellbuehler.bueffel.db.BueffelDatabase

internal class SettingsStore(
    private val db: BueffelDatabase,
    private val env: Env,
) {
    fun get(): AppSettings {
        val values =
            db.settingQueries
                .selectSettings()
                .executeAsList()
                .associate { it.name to it.content }
        return AppSettings(
            dailyGoal = values[DAILY_GOAL]?.toIntOrNull()?.coerceIn(MIN_GOAL, MAX_GOAL) ?: DEFAULT_DAILY_GOAL,
            defaultSessionSize = values[SESSION_SIZE]?.toIntOrNull()?.coerceIn(MIN_SIZE, MAX_SIZE) ?: DEFAULT_SESSION_SIZE,
            theme = ThemePreference.entries.firstOrNull { it.name == values[THEME] } ?: ThemePreference.SYSTEM,
            onboardingSeen = values[ONBOARDING] == "true",
        )
    }

    fun setDailyGoal(goal: Int) = put(DAILY_GOAL, goal.coerceIn(MIN_GOAL, MAX_GOAL).toString())

    fun setDefaultSessionSize(size: Int) = put(SESSION_SIZE, size.coerceIn(MIN_SIZE, MAX_SIZE).toString())

    fun setTheme(theme: ThemePreference) = put(THEME, theme.name)

    fun setOnboardingSeen(seen: Boolean) = put(ONBOARDING, seen.toString())

    private fun put(
        key: String,
        value: String,
    ) {
        db.settingQueries.upsertSetting(key, value)
        env.changed()
    }

    companion object {
        const val DEFAULT_DAILY_GOAL = 20
        const val DEFAULT_SESSION_SIZE = 20
        private const val MIN_GOAL = 1
        private const val MAX_GOAL = 1000
        private const val MIN_SIZE = 1
        private const val MAX_SIZE = 500
        private const val DAILY_GOAL = "daily_goal"
        private const val SESSION_SIZE = "default_session_size"
        private const val THEME = "theme"
        private const val ONBOARDING = "onboarding_seen"
    }
}
