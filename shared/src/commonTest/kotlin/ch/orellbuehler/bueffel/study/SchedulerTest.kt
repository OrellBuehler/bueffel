package ch.orellbuehler.bueffel.study

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SchedulerTest {
    private val now = 1_700_000_000_000L
    private val day = Scheduler.DAY_MS
    private val minute = 60_000L

    private fun review(
        interval: Int,
        ease: Int = 2500,
        dueAt: Long = now,
        lapses: Int = 0,
    ) = Schedule(LearningState.REVIEW, ease, interval, dueAt, 5, lapses, now - interval * day)

    @Test
    fun newFacetGradedGoodGraduatesToOneDay() {
        val next = Scheduler.review(Schedule.fresh(), Grade.GOOD, now)
        assertEquals(LearningState.REVIEW, next.state)
        assertEquals(1, next.intervalDays)
        assertEquals(now + day, next.dueAt)
        assertEquals(1, next.reps)
        assertEquals(now, next.lastReviewedAt)
        assertEquals(Scheduler.START_EASE, next.ease)
    }

    @Test
    fun newFacetGradedEasyGraduatesToFourDays() {
        val next = Scheduler.review(Schedule.fresh(), Grade.EASY, now)
        assertEquals(LearningState.REVIEW, next.state)
        assertEquals(4, next.intervalDays)
        assertEquals(now + 4 * day, next.dueAt)
    }

    @Test
    fun newFacetGradedAgainOrHardEntersLearning() {
        for (grade in listOf(Grade.AGAIN, Grade.HARD)) {
            val next = Scheduler.review(Schedule.fresh(), grade, now)
            assertEquals(LearningState.LEARNING, next.state)
            assertEquals(0, next.intervalDays)
            assertEquals(now + 10 * minute, next.dueAt)
            assertEquals(0, next.lapses)
        }
    }

    @Test
    fun learningFacetGraduatesWithGood() {
        val learning = Scheduler.review(Schedule.fresh(), Grade.AGAIN, now)
        val later = now + 15 * minute
        val next = Scheduler.review(learning, Grade.GOOD, later)
        assertEquals(LearningState.REVIEW, next.state)
        assertEquals(1, next.intervalDays)
        assertEquals(later + day, next.dueAt)
        assertEquals(2, next.reps)
        assertEquals(LearningState.LEARNING, Scheduler.review(learning, Grade.HARD, later).state)
        assertEquals(4, Scheduler.review(learning, Grade.EASY, later).intervalDays)
    }

    @Test
    fun goodAnswersGrowTheIntervalAndEventuallyMasterTheFacet() {
        var schedule = Schedule.fresh()
        var time = now
        val intervals = ArrayList<Int>()
        repeat(7) {
            schedule = Scheduler.review(schedule, Grade.GOOD, time)
            intervals += schedule.intervalDays
            time = schedule.dueAt
        }
        assertEquals(listOf(1, 2, 5, 12, 30, 75, 187), intervals)
        assertTrue(schedule.mastered)
        assertEquals(Scheduler.START_EASE, schedule.ease)
    }

    @Test
    fun masteredNeedsReviewStateAndTwentyOneDays() {
        assertFalse(review(20).mastered)
        assertTrue(review(21).mastered)
        assertFalse(review(40).copy(state = LearningState.LEARNING).mastered)
        assertFalse(Schedule.fresh().mastered)
    }

    @Test
    fun hardGoodEasyAreOrderedAndEaseMoves() {
        val base = review(10)
        val hard = Scheduler.review(base, Grade.HARD, now)
        val good = Scheduler.review(base, Grade.GOOD, now)
        val easy = Scheduler.review(base, Grade.EASY, now)
        assertEquals(12, hard.intervalDays)
        assertEquals(25, good.intervalDays)
        assertEquals(33, easy.intervalDays)
        assertEquals(2350, hard.ease)
        assertEquals(2500, good.ease)
        assertEquals(2650, easy.ease)
        assertEquals(now + 25 * day, good.dueAt)
        assertTrue(hard.intervalDays < good.intervalDays && good.intervalDays < easy.intervalDays)
    }

    @Test
    fun intervalAlwaysGrowsByAtLeastOneDay() {
        val tiny = review(1, ease = Scheduler.MIN_EASE)
        assertEquals(2, Scheduler.review(tiny, Grade.HARD, now).intervalDays)
        assertEquals(2, Scheduler.review(tiny, Grade.GOOD, now).intervalDays)
        assertEquals(3, Scheduler.review(tiny, Grade.EASY, now).intervalDays)
    }

    @Test
    fun lapseGoesBackToLearningAndRelearnsAtAShorterInterval() {
        val lapsed = Scheduler.review(review(20), Grade.AGAIN, now)
        assertEquals(LearningState.LEARNING, lapsed.state)
        assertEquals(1, lapsed.lapses)
        assertEquals(2300, lapsed.ease)
        assertEquals(5, lapsed.intervalDays)
        assertEquals(now + 10 * minute, lapsed.dueAt)
        assertFalse(lapsed.mastered)

        val back = Scheduler.review(lapsed, Grade.GOOD, now + 11 * minute)
        assertEquals(LearningState.REVIEW, back.state)
        assertEquals(5, back.intervalDays)
        assertEquals(6, Scheduler.review(lapsed, Grade.EASY, now + 11 * minute).intervalDays)
        assertEquals(LearningState.LEARNING, Scheduler.review(lapsed, Grade.AGAIN, now + 11 * minute).state)
    }

    @Test
    fun lapseOfAShortIntervalKeepsAtLeastOneDay() {
        assertEquals(1, Scheduler.review(review(2), Grade.AGAIN, now).intervalDays)
    }

    @Test
    fun easeNeverDropsBelowTheFloor() {
        var schedule = review(10, ease = 1400)
        repeat(5) { schedule = Scheduler.review(schedule.copy(state = LearningState.REVIEW), Grade.AGAIN, now) }
        assertEquals(Scheduler.MIN_EASE, schedule.ease)
        assertEquals(Scheduler.MIN_EASE, Scheduler.review(review(10, ease = 1350), Grade.HARD, now).ease)
        assertEquals(5, schedule.lapses)
    }

    @Test
    fun overdueReviewsGetPartialCredit() {
        val overdue = review(10, dueAt = now - 10 * day)
        assertEquals(37, Scheduler.review(overdue, Grade.GOOD, now).intervalDays)
        assertEquals(65, Scheduler.review(overdue, Grade.EASY, now).intervalDays)
        val early = review(10, dueAt = now + 3 * day)
        assertEquals(25, Scheduler.review(early, Grade.GOOD, now).intervalDays)
    }

    @Test
    fun intervalIsCapped() {
        val huge = review(Scheduler.MAX_INTERVAL_DAYS)
        assertEquals(Scheduler.MAX_INTERVAL_DAYS, Scheduler.review(huge, Grade.EASY, now).intervalDays)
    }

    @Test
    fun reviewIsDeterministic() {
        val start = review(7)
        val grades = listOf(Grade.GOOD, Grade.HARD, Grade.AGAIN, Grade.GOOD, Grade.GOOD, Grade.EASY, Grade.GOOD)

        fun run(): List<Schedule> {
            var current = start
            var time = now
            return grades.map { grade ->
                current = Scheduler.review(current, grade, time)
                time = current.dueAt
                current
            }
        }
        assertEquals(run(), run())
    }

    @Test
    fun gradesMapFromCorrectness() {
        assertEquals(Grade.GOOD, Grade.fromCorrect(true))
        assertEquals(Grade.AGAIN, Grade.fromCorrect(false))
        assertEquals(Grade.EASY, Grade.fromCode(4))
        assertEquals(Grade.AGAIN, Grade.fromCode(99))
    }

    @Test
    fun dueCutoffs() {
        val cutoffs = DueCutoffs(learnUntil = now + 20 * minute, reviewUntil = now + day)
        assertFalse(cutoffs.isDue(Schedule.fresh()))
        assertTrue(cutoffs.isDue(review(3, dueAt = now - day)))
        assertTrue(cutoffs.isDue(review(3, dueAt = now + day - 1)))
        assertFalse(cutoffs.isDue(review(3, dueAt = now + day)))
        assertTrue(cutoffs.isDue(review(3).copy(state = LearningState.LEARNING, dueAt = now + 20 * minute)))
        assertFalse(cutoffs.isDue(review(3).copy(state = LearningState.LEARNING, dueAt = now + 21 * minute)))
    }
}
