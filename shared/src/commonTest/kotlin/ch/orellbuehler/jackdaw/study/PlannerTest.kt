package ch.orellbuehler.jackdaw.study

import ch.orellbuehler.jackdaw.FakeClock
import ch.orellbuehler.jackdaw.card
import ch.orellbuehler.jackdaw.packJson
import ch.orellbuehler.jackdaw.readPack
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PlannerTest {
    private val clock = FakeClock()
    private val minute = 60_000L
    private val day = Scheduler.DAY_MS

    private fun cards(
        count: Int,
        extra: String = "",
    ) = (1..count).map { card("c$it", "front $it", "back $it", extra) }

    private fun pack(
        vararg items: String,
        study: String? = null,
        sections: String? = null,
    ) = loaded(readPack(packJson(*items, study = study, sections = sections)))

    private fun ids(plan: Plan) = plan.entries.map { it.item.id }

    @Test
    fun learnIntroducesNewItemsInOrderUpToNewPerSession() {
        val pack = pack(*cards(20).toTypedArray(), study = """{"newPerSession":4}""")
        val plan = plan(pack, SessionConfig.learn())
        assertEquals(listOf("c1", "c2", "c3", "c4"), ids(plan))
        assertEquals(0, plan.dueCount)
        assertEquals(4, plan.newCount)
        assertTrue(plan.entries.all { it.schedule.state == LearningState.NEW })
    }

    @Test
    fun newLimitAndSizeOverridePackDefaults() {
        val pack = pack(*cards(20).toTypedArray(), study = """{"newPerSession":4}""")
        assertEquals(7, plan(pack, SessionConfig.learn().withNewLimit(7)).entries.size)
        assertEquals(0, plan(pack, SessionConfig.learn().withNewLimit(0)).entries.size)
        assertEquals(2, plan(pack, SessionConfig.learn().withNewLimit(10).withSize(2)).entries.size)
        assertEquals(10, plan(pack, SessionConfig.learn().withNewLimit(100), defaultSize = 10).entries.size)
    }

    @Test
    fun randomOrderUsesTheInjectedRandom() {
        val pack = pack(*cards(30).toTypedArray(), study = """{"order":"random","newPerSession":10}""")
        val first = ids(plan(pack, SessionConfig.learn(), random = Random(7)))
        val again = ids(plan(pack, SessionConfig.learn(), random = Random(7)))
        val other = ids(plan(pack, SessionConfig.learn(), random = Random(8)))
        assertEquals(first, again)
        assertNotEquals(first, other)
        assertNotEquals((1..10).map { "c$it" }, first)
        assertEquals(10, first.toSet().size)
    }

    @Test
    fun dueItemsComeBeforeNewOnesOrderedByDueDate() {
        val schedules =
            mapOf(
                key("c5") to review(dueAt = clock.millis - 2 * day),
                key("c3") to review(dueAt = clock.millis - 5 * day),
                key("c9") to review(dueAt = clock.millis + 3 * day),
                key("c2") to Schedule.fresh().copy(state = LearningState.LEARNING, dueAt = clock.millis + 5 * minute, intervalDays = 0),
            )
        val pack = loaded(readPack(packJson(*cards(10).toTypedArray(), study = """{"newPerSession":2}""")), schedules)
        val plan = plan(pack, SessionConfig.learn(), clock)
        assertEquals(listOf("c3", "c5", "c2", "c1", "c4"), ids(plan))
        assertEquals(3, plan.dueCount)
        assertEquals(2, plan.newCount)
    }

    @Test
    fun reviewsDueLaterTodayAreIncludedButTomorrowsAreNot() {
        val schedules =
            mapOf(
                key("c1") to review(dueAt = clock.millis + day - 1),
                key("c2") to review(dueAt = clock.millis + day),
                key("c3") to Schedule.fresh().copy(state = LearningState.LEARNING, dueAt = clock.millis + 30 * minute),
            )
        val pack = loaded(readPack(packJson(*cards(3).toTypedArray())), schedules)
        assertEquals(listOf("c1"), ids(plan(pack, SessionConfig.learn(), clock)))
    }

    @Test
    fun sizeCapsDueItemsAndLeavesNoRoomForNewOnes() {
        val schedules = (1..8).associate { key("c$it") to review(dueAt = clock.millis - it * day) }
        val pack = loaded(readPack(packJson(*cards(12).toTypedArray())), schedules)
        val plan = plan(pack, SessionConfig.learn().withSize(5), clock)
        assertEquals(listOf("c8", "c7", "c6", "c5", "c4"), ids(plan))
        assertEquals(0, plan.newCount)
    }

    @Test
    fun reverseFacetIsOnlyIntroducedAfterTheForwardFacetWasLearned() {
        val items = (1..3).map { card("c$it", "f$it", "b$it", ""","reversible":true""") }.toTypedArray()
        val fresh = loaded(readPack(packJson(*items)))
        val first = plan(fresh, SessionConfig.learn())
        assertEquals(listOf(Facet.FORWARD, Facet.FORWARD, Facet.FORWARD), first.entries.map { it.facet })

        val schedules = mapOf(key("c1") to review(dueAt = clock.millis + 10 * day), key("c2") to review(dueAt = clock.millis + 10 * day))
        val later = plan(loaded(readPack(packJson(*items)), schedules), SessionConfig.learn(), clock)
        assertEquals(
            listOf("c1" to Facet.REVERSE, "c2" to Facet.REVERSE, "c3" to Facet.FORWARD),
            later.entries.map { it.item.id to it.facet },
        )
    }

    @Test
    fun onlyOneFacetPerItemIsPlanned() {
        val items = arrayOf(card("c1", "f", "b", ""","reversible":true"""))
        val schedules =
            mapOf(
                key("c1", Facet.FORWARD) to review(dueAt = clock.millis - day),
                key("c1", Facet.REVERSE) to review(dueAt = clock.millis - 2 * day),
            )
        val plan = plan(loaded(readPack(packJson(*items)), schedules), SessionConfig.learn(), clock)
        assertEquals(1, plan.entries.size)
        assertEquals(Facet.REVERSE, plan.entries.single().facet)
    }

    @Test
    fun sectionFilterRestrictsTheCandidates() {
        val items =
            arrayOf(
                card("a1", "f", "b", ""","section":"a""""),
                card("b1", "f", "b", ""","section":"b""""),
                card("b2", "f", "b", ""","section":"b""""),
                card("u1", "f", "b"),
            )
        val sections = """[{"id":"a","title":"A"},{"id":"b","title":"B"}]"""
        val pack = loaded(readPack(packJson(*items, sections = sections)))
        assertEquals(listOf("b1", "b2"), ids(plan(pack, SessionConfig.learn().withSection("b"))))
        assertEquals(listOf("u1"), ids(plan(pack, SessionConfig.learn().withSection(""))))
        assertEquals(4, plan(pack, SessionConfig.learn().withSection(null)).entries.size)
        assertEquals(emptyList(), ids(plan(pack, SessionConfig.learn().withSection("zzz"))))
    }

    @Test
    fun practiceShufflesAnyItemsAndIgnoresSchedule() {
        val pack = pack(*cards(30).toTypedArray())
        val plan = plan(pack, SessionConfig.practice().withSize(12), random = Random(3))
        assertEquals(12, plan.entries.size)
        assertEquals(12, ids(plan).toSet().size)
        assertNotEquals((1..12).map { "c$it" }, ids(plan))
        assertEquals(ids(plan), ids(plan(pack, SessionConfig.practice().withSize(12), random = Random(3))))
    }

    @Test
    fun practiceFiltersSelectExerciseFamilies() {
        val items =
            arrayOf(
                card("card1", "f1", "b1"),
                card("card2", "f2", "b2"),
                card("card3", "f3", "b3"),
                card("card4", "f4", "b4"),
                """{"id":"mc","type":"choice","question":"q","options":[{"text":"a","correct":true},{"text":"b"}]}""",
                """{"id":"tx","type":"text","question":"q","answers":["a"]}""",
                """{"id":"cz","type":"cloze","text":"a {{b}}"}""",
                """{"id":"tf","type":"truefalse","statement":"s","answer":true}""",
                """{"id":"or","type":"order","prompt":"p","sequence":["a","b"]}""",
                """{"id":"op","type":"open","question":"q"}""",
            )
        val pack = pack(*items)

        fun types(filter: PracticeFilter): Map<String, ExerciseType> =
            plan(pack, SessionConfig.practice().withFilter(filter).withSize(50)).entries.associate { it.item.id to it.type }
        assertEquals(10, types(PracticeFilter.ALL).size)
        val flash = types(PracticeFilter.FLASHCARDS)
        assertEquals(setOf("card1", "card2", "card3", "card4", "op"), flash.keys)
        assertTrue(flash.filterKeys { it.startsWith("card") }.values.all { it == ExerciseType.FLIP })
        val quiz = types(PracticeFilter.QUIZ)
        assertEquals(setOf("card1", "card2", "card3", "card4", "mc", "tf", "or"), quiz.keys)
        assertEquals(ExerciseType.CHOICE, quiz.getValue("card1"))
        val typing = types(PracticeFilter.TYPING)
        assertEquals(setOf("card1", "card2", "card3", "card4", "tx", "cz"), typing.keys)
        assertEquals(ExerciseType.TYPE, typing.getValue("card1"))
    }

    @Test
    fun practiceFilterRespectsAllowedCardKindsAndFeasibility() {
        val restricted = pack(*cards(5, ""","exercises":["choice","type"]""").toTypedArray())
        assertEquals(0, plan(restricted, SessionConfig.practice().withFilter(PracticeFilter.FLASHCARDS)).entries.size)
        assertEquals(5, plan(restricted, SessionConfig.practice().withFilter(PracticeFilter.QUIZ)).entries.size)

        val twoCards = pack(card("c1", "f1", "b1", ""","exercises":["choice"]"""), card("c2", "f2", "b2", ""","exercises":["choice"]"""))
        assertEquals(0, plan(twoCards, SessionConfig.practice().withFilter(PracticeFilter.QUIZ)).entries.size)
        val fallback = plan(twoCards, SessionConfig.learn())
        assertEquals(listOf(ExerciseType.FLIP, ExerciseType.FLIP), fallback.entries.map { it.type })
    }

    @Test
    fun practiceUsesOneFacetPerItem() {
        val items = (1..6).map { card("c$it", "f$it", "b$it", ""","reversible":true""") }.toTypedArray()
        val plan = plan(loaded(readPack(packJson(*items))), SessionConfig.practice().withSize(100), random = Random(5))
        assertEquals(6, plan.entries.size)
        assertEquals(6, ids(plan).toSet().size)
        assertTrue(plan.entries.any { it.facet == Facet.REVERSE })
    }

    @Test
    fun exerciseKindGrowsWithMaturity() {
        val pack = pack(*cards(6).toTypedArray())
        val item = pack.context.items.first()

        fun choose(schedule: Schedule) = ExerciseFactory.chooseType(pack.context, item, Facet.FORWARD, schedule, PracticeFilter.ALL)
        assertEquals(ExerciseType.FLIP, choose(Schedule.fresh()))
        assertEquals(ExerciseType.CHOICE, choose(Schedule.fresh().copy(state = LearningState.LEARNING)))
        assertEquals(ExerciseType.CHOICE, choose(review(interval = 3)))
        assertEquals(ExerciseType.TYPE, choose(review(interval = 7)))
        assertEquals(ExerciseType.TYPE, choose(review(interval = 60)))
    }

    @Test
    fun exerciseKindOnlyUsesAllowedKinds() {
        val pack = pack(card("c1", "f", "b", ""","exercises":["flip","type"]"""), *cards(5).toTypedArray().drop(1).toTypedArray())
        val item = pack.context.items.first()

        fun choose(schedule: Schedule) = ExerciseFactory.chooseType(pack.context, item, Facet.FORWARD, schedule, PracticeFilter.ALL)
        assertEquals(ExerciseType.FLIP, choose(Schedule.fresh()))
        assertEquals(ExerciseType.FLIP, choose(review(interval = 3)))
        assertEquals(ExerciseType.TYPE, choose(review(interval = 30)))
    }

    private fun review(
        dueAt: Long = clock.millis,
        interval: Int = 3,
    ) = Schedule(LearningState.REVIEW, 2500, interval, dueAt, 3, 0, clock.millis - day)
}
