package ch.orellbuehler.jackdaw.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.orellbuehler.jackdaw.ui.theme.PackPalette

val LocalBottomBarInset = compositionLocalOf { 0.dp }

val ScreenPadding = 16.dp

fun bottomContentPadding(
    inset: Dp,
    extra: Dp = 24.dp,
) = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = inset + extra)

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    strokeWidth: Dp = 9.dp,
    trackColor: Color,
    color: Color,
    content: @Composable () -> Unit = {},
) {
    val animated by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        spring(Spring.DampingRatioLowBouncy, Spring.StiffnessLow),
        label = "ring",
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val stroke = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2
            val arcSize = Size(this.size.width - inset * 2, this.size.height - inset * 2)
            drawArc(trackColor, 0f, 360f, false, Offset(inset, inset), arcSize, style = stroke)
            if (animated > 0f) {
                drawArc(color, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = stroke)
            }
        }
        content()
    }
}

@Composable
fun SlimProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    trackColor: Color,
    color: Color,
    animate: Boolean = true,
) {
    val value by animateFloatAsState(
        progress.coerceIn(0f, 1f),
        if (animate) spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow) else spring(stiffness = Spring.StiffnessHigh),
        label = "bar",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(trackColor),
    ) {
        Box(
            Modifier
                .fillMaxWidth(value)
                .height(height)
                .clip(CircleShape)
                .background(color),
        )
    }
}

@Composable
fun Pill(
    text: String,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    textSize: TextUnit = 12.sp,
) {
    Row(
        modifier
            .clip(CircleShape)
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.invoke()
        Text(
            text,
            color = content,
            fontSize = textSize,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PackIconTile(
    icon: String?,
    palette: PackPalette,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(size * 0.3f),
) {
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(palette.accent),
        contentAlignment = Alignment.Center,
    ) {
        if (icon.isNullOrBlank()) {
            Icon(Icons.Rounded.MenuBook, null, Modifier.size(size * 0.5f), tint = palette.onAccent)
        } else {
            Text(icon, fontSize = (size.value * 0.5f).sp, modifier = Modifier.clearAndSetSemantics { })
        }
    }
}

fun Modifier.heroGradient(
    start: Color,
    end: Color,
): Modifier =
    drawBehind {
        drawGradient(start, end)
    }

private fun DrawScope.drawGradient(
    start: Color,
    end: Color,
) {
    drawRect(
        androidx.compose.ui.graphics.Brush.linearGradient(
            listOf(start, end),
            Offset.Zero,
            Offset(size.width, size.height),
        ),
    )
}
