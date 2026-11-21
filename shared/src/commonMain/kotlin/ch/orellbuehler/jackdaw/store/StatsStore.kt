package ch.orellbuehler.jackdaw.store

import ch.orellbuehler.jackdaw.db.JackdawDatabase
import kotlinx.datetime.LocalDate

internal class StatsStore(
    private val db: JackdawDatabase,
    private val env: Env,
    private val packs: PackStore,
    private val settings: SettingsStore,
) {
    fun stats(days: Int): Stats {
        val now = env.now()
        val today = env.epochDay(now)
        val rows = db.reviewQueries.logByDay().executeAsList()
        val byDay = rows.associate { it.day.toInt() to it }
        val activeDays = rows.map { it.day.toInt() }

        var longest = 0
        var run = 0
        var previous = Int.MIN_VALUE
        for (day in activeDays) {
            run = if (day == previous + 1) run + 1 else 1
            if (run > longest) longest = run
            previous = day
        }
        var current = 0
        var cursor = if (today in byDay) today else today - 1
        while (cursor in byDay) {
            current++
            cursor--
        }

        val totalReviews = rows.sumOf { it.reviews }
        val totalCorrect = rows.sumOf { it.correct_count }
        val window = days.coerceIn(1, MAX_DAYS)
        val activity =
            (today - window + 1..today).map { day ->
                val row = byDay[day]
                DayActivity(LocalDate.fromEpochDays(day).toString(), day, row?.reviews?.toInt() ?: 0, row?.correct_count?.toInt() ?: 0)
            }
        val (learned, mastered, due) = packs.dueTotals(env.cutoffs(now))
        val goal = settings.get().dailyGoal
        val reviewsToday = byDay[today]?.reviews?.toInt() ?: 0
        return Stats(
            currentStreak = current,
            longestStreak = longest,
            reviewsToday = reviewsToday,
            correctToday = byDay[today]?.correct_count?.toInt() ?: 0,
            totalReviews = totalReviews.toInt(),
            accuracy = if (totalReviews == 0L) 0.0 else totalCorrect.toDouble() / totalReviews,
            facetsLearned = learned,
            facetsMastered = mastered,
            dueToday = due,
            dailyGoal = goal,
            goalReached = reviewsToday >= goal,
            activity = activity,
        )
    }

    companion object {
        const val DEFAULT_DAYS = 91
        private const val MAX_DAYS = 730
    }
}
