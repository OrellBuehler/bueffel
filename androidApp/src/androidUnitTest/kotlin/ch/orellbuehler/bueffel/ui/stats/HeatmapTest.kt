package ch.orellbuehler.bueffel.ui.stats

import ch.orellbuehler.bueffel.store.DayActivity
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class HeatmapTest {
    private fun days(
        endIso: String,
        count: Int,
        reviews: (Int) -> Int = { 0 },
    ): List<DayActivity> {
        val end = LocalDate.parse(endIso).toEpochDay().toInt()
        return (0 until count).map { i ->
            val epoch = end - (count - 1 - i)
            DayActivity(LocalDate.ofEpochDay(epoch.toLong()).toString(), epoch, reviews(i), reviews(i))
        }
    }

    @Test
    fun levelsScaleWithTheGoal() {
        assertEquals(0, heatLevel(0, 20))
        assertEquals(1, heatLevel(5, 20))
        assertEquals(2, heatLevel(10, 20))
        assertEquals(3, heatLevel(15, 20))
        assertEquals(4, heatLevel(20, 20))
        assertEquals(4, heatLevel(80, 20))
        assertEquals(4, heatLevel(1, 0))
    }

    @Test
    fun lastColumnEndsOnTodaysWeekday() {
        // 2026-11-20 is a Friday
        val heatmap = buildHeatmap(days("2026-11-20", 98), 20, DayOfWeek.MONDAY)
        assertEquals(HEATMAP_WEEKS, heatmap.columns.size)
        val last = heatmap.columns.last()
        assertEquals(7, last.size)
        assertEquals("2026-11-20", last[4]?.day?.date)
        assertNull(last[5])
        assertNull(last[6])
        assertEquals("2026-11-16", last[0]?.day?.date)
    }

    @Test
    fun weekStartChangesTheRowOfToday() {
        val sunday = buildHeatmap(days("2026-11-20", 98), 20, DayOfWeek.SUNDAY)
        assertEquals(
            "2026-11-20",
            sunday.columns
                .last()[5]
                ?.day
                ?.date,
        )
        assertEquals(
            "2026-11-15",
            sunday.columns
                .last()[0]
                ?.day
                ?.date,
        )
    }

    @Test
    fun daysBeforeTheWindowAreEmpty() {
        val heatmap = buildHeatmap(days("2026-11-20", 10), 20, DayOfWeek.MONDAY)
        assertNull(heatmap.columns.first().first())
        assertNotNull(heatmap.columns.last().first())
    }

    @Test
    fun cellsCarryTheirLevel() {
        val heatmap = buildHeatmap(days("2026-11-20", 98) { if (it == 97) 20 else 0 }, 20, DayOfWeek.MONDAY)
        assertEquals(4, heatmap.columns.last()[4]?.level)
        assertEquals(0, heatmap.columns.last()[3]?.level)
    }

    @Test
    fun noActivityMeansNoColumns() {
        assertEquals(0, buildHeatmap(emptyList(), 20, DayOfWeek.MONDAY).columns.size)
    }
}
