package ch.orellbuehler.bueffel.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.orellbuehler.bueffel.R
import ch.orellbuehler.bueffel.store.DayActivity
import ch.orellbuehler.bueffel.store.Stats
import ch.orellbuehler.bueffel.ui.components.LocalBottomBarInset
import ch.orellbuehler.bueffel.ui.components.ProgressRing
import ch.orellbuehler.bueffel.ui.components.bottomContentPadding
import ch.orellbuehler.bueffel.ui.theme.Amber
import ch.orellbuehler.bueffel.ui.theme.bueffel
import ch.orellbuehler.bueffel.ui.theme.cardSurface
import java.time.LocalDate
import kotlin.math.roundToInt
import java.time.format.TextStyle as DateTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_stats), style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        val stats = state.stats
        if (stats != null) {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = bottomContentPadding(LocalBottomBarInset.current),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { StreakCard(stats) }
                item { GoalCard(stats) }
                item { MetricGrid(stats) }
                item { ActivityCard(state) }
            }
        }
    }
}

@Composable
private fun StreakCard(stats: Stats) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(64.dp).clip(CircleShape).background(Amber), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.LocalFireDepartment, null, Modifier.size(38.dp), tint = Color(0xFF17142A))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.stats_streak), style = MaterialTheme.typography.labelLarge)
                Text(
                    pluralStringResource(R.plurals.days_count, stats.currentStreak, stats.currentStreak),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    stringResource(
                        R.string.stats_longest,
                        pluralStringResource(R.plurals.days_count, stats.longestStreak, stats.longestStreak),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun GoalCard(stats: Stats) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.cardSurface),
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            ProgressRing(
                progress = stats.reviewsToday.toFloat() / stats.dailyGoal.coerceAtLeast(1),
                size = 76.dp,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                color = if (stats.goalReached) MaterialTheme.bueffel.success else MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    "${(stats.reviewsToday * 100f / stats.dailyGoal.coerceAtLeast(1)).roundToInt().coerceAtMost(999)}%",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    stringResource(R.string.stats_today),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.stats_today_value, stats.reviewsToday, stats.dailyGoal),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    if (stats.goalReached) {
                        stringResource(R.string.today_goal_reached)
                    } else {
                        stringResource(R.string.stats_due_now, stats.dueToday)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetricGrid(stats: Stats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                "${(stats.accuracy * 100).roundToInt()}%",
                stringResource(R.string.stats_accuracy),
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
                Modifier.weight(1f),
            )
            MetricTile(
                "${stats.totalReviews}",
                stringResource(R.string.stats_reviews),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
                Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                "${stats.facetsLearned}",
                stringResource(R.string.stats_learned),
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.colorScheme.onSurface,
                Modifier.weight(1f),
            )
            MetricTile(
                "${stats.facetsMastered}",
                stringResource(R.string.stats_mastered),
                MaterialTheme.bueffel.successContainer,
                MaterialTheme.bueffel.onSuccessContainer,
                Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricTile(
    value: String,
    label: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(container)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = MaterialTheme.typography.headlineMedium, color = content)
        Text(label, style = MaterialTheme.typography.labelMedium, color = content.copy(alpha = 0.85f), maxLines = 1)
    }
}

@Composable
private fun ActivityCard(state: StatsState) {
    val stats = state.stats ?: return
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.cardSurface),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(stringResource(R.string.stats_activity), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.stats_this_week),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            WeekChart(state.week, stats.dailyGoal)
            Text(
                stringResource(R.string.stats_last_weeks, HEATMAP_WEEKS),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            HeatmapChart(state.heatmap)
            if (stats.totalReviews == 0) {
                Text(
                    stringResource(R.string.stats_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WeekChart(
    week: List<DayActivity>,
    goal: Int,
) {
    if (week.isEmpty()) return
    val measurer = rememberTextMeasurer()
    val scheme = MaterialTheme.colorScheme
    val success = MaterialTheme.bueffel.success
    val labelColor = scheme.onSurfaceVariant
    val locale = LocalLocale.current.platformLocale
    val labels =
        remember(week, locale) {
            week.map { LocalDate.ofEpochDay(it.epochDay.toLong()).dayOfWeek.getDisplayName(DateTextStyle.SHORT, locale) }
        }
    val description = week.indices.joinToString(", ") { "${labels[it]} ${week[it].reviews}" }
    val chartDescription = stringResource(R.string.stats_chart_description, description)
    val goalLabel = stringResource(R.string.stats_goal_line, goal)
    val max = maxOf(week.maxOf { it.reviews }, goal, 1)
    val valueStyle = TextStyle(fontSize = 11.sp, color = labelColor)
    val dayStyle = TextStyle(fontSize = 11.sp, color = labelColor, textAlign = TextAlign.Center)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(168.dp)
            .semantics { contentDescription = chartDescription },
    ) {
        val labelHeight = 20.dp.toPx()
        val topPad = 18.dp.toPx()
        val chartHeight = size.height - labelHeight - topPad
        val slot = size.width / week.size
        val barWidth = slot * 0.5f
        val baseY = topPad + chartHeight
        val goalY = baseY - chartHeight * (goal.toFloat() / max)
        drawLine(
            scheme.outline.copy(alpha = 0.5f),
            Offset(0f, goalY),
            Offset(size.width, goalY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
        )
        val goalLayout = measurer.measure(goalLabel, valueStyle)
        drawText(goalLayout, topLeft = Offset(size.width - goalLayout.size.width, goalY - goalLayout.size.height - 2.dp.toPx()))
        week.forEachIndexed { index, day ->
            val left = slot * index + (slot - barWidth) / 2
            val height = if (day.reviews == 0) 4.dp.toPx() else (chartHeight * day.reviews / max).coerceAtLeast(6.dp.toPx())
            val color =
                when {
                    day.reviews == 0 -> scheme.surfaceContainerHigh
                    day.reviews >= goal -> success
                    else -> scheme.primary
                }
            drawRoundRect(color, Offset(left, baseY - height), Size(barWidth, height), CornerRadius(8.dp.toPx()))
            if (day.reviews > 0) {
                val layout = measurer.measure(day.reviews.toString(), valueStyle)
                drawText(
                    layout,
                    topLeft =
                        Offset(
                            left + (barWidth - layout.size.width) / 2,
                            baseY - height - layout.size.height - 2.dp.toPx(),
                        ),
                )
            }
            val dayLayout = measurer.measure(labels[index], dayStyle)
            drawText(dayLayout, topLeft = Offset(slot * index + (slot - dayLayout.size.width) / 2, baseY + 4.dp.toPx()))
        }
    }
}

@Composable
private fun HeatmapChart(heatmap: Heatmap) {
    if (heatmap.columns.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val colors =
        remember(scheme) {
            listOf(
                scheme.surfaceContainerHigh,
                lerp(scheme.primaryContainer, scheme.primary, 0.15f),
                lerp(scheme.primaryContainer, scheme.primary, 0.45f),
                lerp(scheme.primaryContainer, scheme.primary, 0.75f),
                scheme.primary,
            )
        }
    val total = heatmap.columns.sumOf { column -> column.sumOf { it?.day?.reviews ?: 0 } }
    val description = stringResource(R.string.stats_heatmap_description, heatmap.columns.size, total)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = heatmap.columns.size
            val gap = 4.dp
            val cell = (maxWidth - gap * (columns - 1)) / columns
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(cell * 7 + gap * 6)
                    .semantics { contentDescription = description },
            ) {
                val gapPx = gap.toPx()
                val cellPx = cell.toPx()
                heatmap.columns.forEachIndexed { x, column ->
                    column.forEachIndexed { y, cellData ->
                        if (cellData != null) {
                            drawRoundRect(
                                colors[cellData.level],
                                Offset(x * (cellPx + gapPx), y * (cellPx + gapPx)),
                                Size(cellPx, cellPx),
                                CornerRadius(cellPx * 0.28f),
                            )
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.stats_less), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            colors.forEach { Box(Modifier.size(12.dp).clip(RoundedCornerShape(4.dp)).background(it)) }
            Text(stringResource(R.string.stats_more), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}
