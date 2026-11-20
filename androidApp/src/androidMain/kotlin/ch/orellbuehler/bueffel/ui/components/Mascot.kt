package ch.orellbuehler.bueffel.ui.components

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
import androidx.compose.ui.geometry.CornerRadius
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
import ch.orellbuehler.bueffel.ui.theme.Amber
import ch.orellbuehler.bueffel.ui.theme.Cream
import ch.orellbuehler.bueffel.ui.theme.Violet

enum class MascotMood {
    Idle,
    Happy,
    Sleepy,
}

private const val HORNS = "M46 30C22 80 34 126 74 142L182 142C222 126 234 80 210 30C194 82 160 98 128 98C96 98 62 82 46 30Z"

@Composable
fun Mascot(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    mood: MascotMood = MascotMood.Idle,
    bodyColor: Color = Violet,
    animated: Boolean = true,
) {
    val animate = animated && !rememberReduceMotion()
    val horns = remember { PathParser().parsePathString(HORNS).toPath() }
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
        translate(top = (bob - 0.5f) * 8f * unit) {
            fun p(
                x: Float,
                y: Float,
            ) = Offset(x * unit, y * unit)
            scale(unit, pivot = Offset.Zero) { drawPath(horns, Amber) }
            drawRoundRect(
                bodyColor,
                topLeft = p(66f, 100f),
                size = Size(124f * unit, 132f * unit),
                cornerRadius = CornerRadius(30f * unit),
            )
            val eyeY = 170f
            val eyes = listOf(105f, 151f)
            when (mood) {
                MascotMood.Happy ->
                    eyes.forEach { x ->
                        drawArc(
                            Cream,
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = p(x - 12f, eyeY - 8f),
                            size = Size(24f * unit, 24f * unit),
                            style = Stroke(width = 7f * unit, cap = StrokeCap.Round),
                        )
                    }
                MascotMood.Sleepy ->
                    eyes.forEach { x ->
                        drawLine(Cream, p(x - 11f, eyeY), p(x + 11f, eyeY), strokeWidth = 7f * unit, cap = StrokeCap.Round)
                    }
                MascotMood.Idle ->
                    eyes.forEach { x ->
                        drawOval(
                            Cream,
                            topLeft = p(x - 11f, eyeY - 11f * blink),
                            size = Size(22f * unit, 22f * unit * blink),
                        )
                    }
            }
            if (mood == MascotMood.Happy) {
                drawArc(
                    Cream,
                    startAngle = 30f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = p(116f, 186f),
                    size = Size(24f * unit, 20f * unit),
                    style = Stroke(width = 5f * unit, cap = StrokeCap.Round),
                )
            }
        }
    }
}
