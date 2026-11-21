package ch.orellbuehler.jackdaw.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.orellbuehler.jackdaw.JackdawSdk
import ch.orellbuehler.jackdaw.store.DayActivity
import ch.orellbuehler.jackdaw.store.Stats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.DayOfWeek

const val HEATMAP_WEEKS = 14

data class HeatCell(
    val day: DayActivity,
    val level: Int,
)

data class Heatmap(
    val columns: List<List<HeatCell?>>,
)

fun heatLevel(
    reviews: Int,
    goal: Int,
): Int {
    if (reviews <= 0) return 0
    val ratio = reviews.toFloat() / goal.coerceAtLeast(1)
    return when {
        ratio < 0.34f -> 1
        ratio < 0.67f -> 2
        ratio < 1f -> 3
        else -> 4
    }
}

fun buildHeatmap(
    activity: List<DayActivity>,
    goal: Int,
    firstDayOfWeek: DayOfWeek,
    weeks: Int = HEATMAP_WEEKS,
): Heatmap {
    val today = activity.lastOrNull() ?: return Heatmap(emptyList())
    val todayDow =
        java.time.LocalDate
            .ofEpochDay(today.epochDay.toLong())
            .dayOfWeek
    val offsetInWeek = (todayDow.value - firstDayOfWeek.value + 7) % 7
    val lastColumnStart = today.epochDay - offsetInWeek
    val byDay = activity.associateBy { it.epochDay }
    val columns =
        (weeks - 1 downTo 0).map { weeksAgo ->
            val start = lastColumnStart - weeksAgo * 7
            (0 until 7).map { row ->
                val day = byDay[start + row]
                day?.let { HeatCell(it, heatLevel(it.reviews, goal)) }
            }
        }
    return Heatmap(columns)
}

data class StatsState(
    val loaded: Boolean = false,
    val stats: Stats? = null,
    val week: List<DayActivity> = emptyList(),
    val heatmap: Heatmap = Heatmap(emptyList()),
)

class StatsViewModel(
    sdk: JackdawSdk,
    private val firstDayOfWeek: DayOfWeek,
) : ViewModel() {
    val state: StateFlow<StatsState> =
        sdk
            .observeStats(HEATMAP_WEEKS * 7)
            .map { stats ->
                StatsState(
                    loaded = true,
                    stats = stats,
                    week = stats.activity.takeLast(7),
                    heatmap = buildHeatmap(stats.activity, stats.dailyGoal, firstDayOfWeek),
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsState())
}
