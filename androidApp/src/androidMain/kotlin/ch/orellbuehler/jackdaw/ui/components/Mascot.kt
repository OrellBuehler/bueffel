package ch.orellbuehler.jackdaw.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.orellbuehler.jackdaw.ui.theme.Amber
import ch.orellbuehler.jackdaw.ui.theme.Cream
import ch.orellbuehler.jackdaw.ui.theme.Ink
import ch.orellbuehler.jackdaw.ui.theme.Violet

enum class MascotMood {
    Idle,
    Happy,
    Sleepy,
}

private const val BILL = "M150 88C198 88 228 110 240 134C226 158 196 168 150 168Z"
private const val NAPE = "M74.4 65.7A76 76 0 0 0 74.4 190.3Q104 128 74.4 65.7Z"
private val NapeColor = Color(0xFFC9C4EC)

@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    mood: MascotMood = MascotMood.Idle,
    bodyColor: Color = Violet,
    animated: Boolean = true,
) {
    val animate = animated && !rememberReduceMotion()
    val bill = remember { PathParser().parsePathString(BILL).toPath() }
    val nape = remember { PathParser().parsePathString(NAPE).toPath() }
    val transition = rememberInfiniteTransition(label = "mascot")
    val blink =
        if (animate && mood == MascotMood.Idle) {
            transition
                .animateFloat(
                    initialValue = 1f,
                    targetValue = 1f,
                    animationSpec =
                        infiniteRepeatable(
                            animation =
                                keyframes {
                                    durationMillis = 4200
                                    1f at 0
                                    1f at 3900
                                    0.08f at 4020 using FastOutSlowInEasing
                                    1f at 4200
                                },
                            repeatMode = RepeatMode.Restart,
                        ),
                    label = "blink",
                ).value
        } else {
            1f
        }
    val bob =
        if (animate) {
            transition
                .animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "bob",
                ).value
        } else {
            0f
        }
    Canvas(modifier.size(size).clearAndSetSemantics { }) {
        val unit = this.size.minDimension / 256f
        translate(left = -13f * unit, top = (bob - 0.5f) * 8f * unit) {
            fun p(
                x: Float,
                y: Float,
            ) = Offset(x * unit, y * unit)
            scale(unit, pivot = Offset.Zero) { drawPath(bill, Amber) }
            drawCircle(bodyColor, radius = 76f * unit, center = p(118f, 128f))
            scale(unit, pivot = Offset.Zero) { drawPath(nape, NapeColor) }
            when (mood) {
                MascotMood.Happy ->
                    drawArc(
                        Cream,
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = p(142f - 15f, 112f - 6f),
                        size = Size(30f * unit, 30f * unit),
                        style = Stroke(width = 8f * unit, cap = StrokeCap.Round),
                    )
                MascotMood.Sleepy ->
                    drawLine(Cream, p(142f - 15f, 114f), p(142f + 15f, 114f), strokeWidth = 8f * unit, cap = StrokeCap.Round)
                MascotMood.Idle -> {
                    drawOval(
                        Cream,
                        topLeft = p(142f - 16f, 112f - 16f * blink),
                        size = Size(32f * unit, 32f * unit * blink),
                    )
                    drawOval(
                        Ink,
                        topLeft = p(145f - 7f, 112f - 7f * blink),
                        size = Size(14f * unit, 14f * unit * blink),
                    )
                }
            }
        }
    }
}
