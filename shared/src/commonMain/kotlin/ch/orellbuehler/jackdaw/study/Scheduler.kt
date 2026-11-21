package ch.orellbuehler.jackdaw.study

enum class Grade(
    val code: Int,
) {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4),
    ;

    companion object {
        internal fun fromCode(code: Int): Grade = entries.firstOrNull { it.code == code } ?: AGAIN

        internal fun fromCorrect(correct: Boolean): Grade = if (correct) GOOD else AGAIN
    }
}

enum class Facet(
    val code: Int,
) {
    FORWARD(0),
    REVERSE(1),
    ;

    companion object {
        internal fun fromCode(code: Int): Facet = if (code == 1) REVERSE else FORWARD
    }
}

internal enum class LearningState(
    val code: Int,
) {
    NEW(0),
    LEARNING(1),
    REVIEW(2),
    ;

    companion object {
        fun fromCode(code: Int): LearningState = entries.firstOrNull { it.code == code } ?: NEW
    }
}

internal data class Schedule(
    val state: LearningState,
    val ease: Int,
    val intervalDays: Int,
    val dueAt: Long,
    val reps: Int,
    val lapses: Int,
    val lastReviewedAt: Long,
) {
    val mastered: Boolean
        get() = state == LearningState.REVIEW && intervalDays >= Scheduler.MASTERED_INTERVAL_DAYS

    companion object {
        fun fresh(): Schedule = Schedule(LearningState.NEW, Scheduler.START_EASE, 0, 0L, 0, 0, 0L)
    }
}

internal class DueCutoffs(
    val learnUntil: Long,
    val reviewUntil: Long,
) {
    fun isDue(schedule: Schedule): Boolean =
        when (schedule.state) {
            LearningState.NEW -> false
            LearningState.LEARNING -> schedule.dueAt <= learnUntil
            LearningState.REVIEW -> schedule.dueAt < reviewUntil
        }
}

internal object Scheduler {
    const val DAY_MS = 86_400_000L
    const val LEARN_AHEAD_MS = 20 * 60_000L
    const val START_EASE = 2500
    const val MIN_EASE = 1300
    const val MASTERED_INTERVAL_DAYS = 21
    const val MAX_INTERVAL_DAYS = 36_500
    private const val GRADUATING_INTERVAL_DAYS = 1
    private const val EASY_INTERVAL_DAYS = 4
    private const val HARD_FACTOR_PERCENT = 120
    private const val EASY_BONUS_PERCENT = 130
    private const val LAPSE_KEEP_PERCENT = 25
    private const val LEARNING_STEP_MS = 10 * 60_000L

    fun review(
        schedule: Schedule,
        grade: Grade,
        now: Long,
    ): Schedule =
        when (schedule.state) {
            LearningState.NEW, LearningState.LEARNING -> learn(schedule, grade, now)
            LearningState.REVIEW -> reviewed(schedule, grade, now)
        }

    private fun learn(
        s: Schedule,
        grade: Grade,
        now: Long,
    ): Schedule {
        val base = s.copy(reps = s.reps + 1, lastReviewedAt = now)
        return when (grade) {
            Grade.AGAIN, Grade.HARD -> base.copy(state = LearningState.LEARNING, dueAt = now + LEARNING_STEP_MS)
            Grade.GOOD -> graduate(base, if (s.intervalDays > 0) s.intervalDays else GRADUATING_INTERVAL_DAYS, now)
            Grade.EASY -> graduate(base, if (s.intervalDays > 0) s.intervalDays + 1 else EASY_INTERVAL_DAYS, now)
        }
    }

    private fun graduate(
        s: Schedule,
        intervalDays: Int,
        now: Long,
    ): Schedule =
        s.copy(
            state = LearningState.REVIEW,
            intervalDays = intervalDays,
            dueAt = now + intervalDays * DAY_MS,
        )

    private fun reviewed(
        s: Schedule,
        grade: Grade,
        now: Long,
    ): Schedule {
        val base = s.copy(reps = s.reps + 1, lastReviewedAt = now)
        val interval = s.intervalDays
        val overdueDays = maxOf(0L, (now - s.dueAt) / DAY_MS).toInt()
        if (grade == Grade.AGAIN) {
            return base.copy(
                state = LearningState.LEARNING,
                lapses = s.lapses + 1,
                ease = maxOf(MIN_EASE, s.ease - 200),
                intervalDays = maxOf(1, interval * LAPSE_KEEP_PERCENT / 100),
                dueAt = now + LEARNING_STEP_MS,
            )
        }
        val hard = minOf(MAX_INTERVAL_DAYS, maxOf(interval + 1, percent(interval, HARD_FACTOR_PERCENT)))
        val good = minOf(MAX_INTERVAL_DAYS, maxOf(hard, interval + 1, (interval + overdueDays / 2) * s.ease / 1000))
        val easy =
            minOf(
                MAX_INTERVAL_DAYS,
                maxOf(good + 1, percent((interval + overdueDays) * s.ease / 1000, EASY_BONUS_PERCENT)),
            )
        return when (grade) {
            Grade.HARD -> graduate(base.copy(ease = maxOf(MIN_EASE, s.ease - 150)), hard, now)
            Grade.GOOD -> graduate(base, good, now)
            else -> graduate(base.copy(ease = s.ease + 150), easy, now)
        }
    }

    private fun percent(
        value: Int,
        percent: Int,
    ): Int = (value * percent + 50) / 100
}
