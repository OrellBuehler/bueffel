package ch.orellbuehler.jackdaw.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

@Immutable
class PackPalette(
    val accent: Color,
    val onAccent: Color,
    val heroStart: Color,
    val heroEnd: Color,
    val onHero: Color,
    val container: Color,
    val onContainer: Color,
    val accentOnContainer: Color,
)

fun contrastRatio(
    a: Color,
    b: Color,
): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

fun bestOn(background: Color): Color = if (contrastRatio(Ink, background) >= contrastRatio(Color.White, background)) Ink else Color.White

fun bestOnBoth(
    a: Color,
    b: Color,
): Color {
    val ink = minOf(contrastRatio(Ink, a), contrastRatio(Ink, b))
    val white = minOf(contrastRatio(Color.White, a), contrastRatio(Color.White, b))
    return if (ink >= white) Ink else Color.White
}

fun ensureContrast(
    color: Color,
    background: Color,
    minimum: Float,
    towards: Color,
): Color {
    var result = color
    var step = 0
    while (contrastRatio(result, background) < minimum && step < 20) {
        step++
        result = lerp(color, towards, step / 20f)
    }
    return result
}

private val hexPattern = Regex("^#[0-9a-fA-F]{6}$")

fun parseHexColor(hex: String?): Color? {
    if (hex == null || !hexPattern.matches(hex)) return null
    return Color(0xFF000000L or hex.substring(1).toLong(16))
}

private fun shiftHue(
    color: Color,
    degrees: Float,
    lightnessDelta: Float,
): Color {
    val max = maxOf(color.red, color.green, color.blue)
    val min = minOf(color.red, color.green, color.blue)
    val delta = max - min
    var lightness = (max + min) / 2f
    val saturation = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * lightness - 1f))
    var hue =
        when {
            delta == 0f -> 0f
            max == color.red -> 60f * (((color.green - color.blue) / delta) % 6f)
            max == color.green -> 60f * ((color.blue - color.red) / delta + 2f)
            else -> 60f * ((color.red - color.green) / delta + 4f)
        }
    if (hue < 0f) hue += 360f
    hue = (hue + degrees) % 360f
    lightness = (lightness + lightnessDelta).coerceIn(0.12f, 0.9f)
    return Color.hsl(hue, saturation.coerceIn(0f, 1f), lightness)
}

fun packPalette(
    hex: String?,
    scheme: ColorScheme,
    dark: Boolean,
): PackPalette {
    val accent = parseHexColor(hex) ?: Violet
    val base = if (dark) scheme.surfaceContainerHigh else scheme.surfaceContainerLowest
    val container = lerp(base, accent, if (dark) 0.24f else 0.14f)
    val heroStart = if (dark) lerp(accent, scheme.surface, 0.2f) else accent
    val heroEnd = shiftHue(heroStart, 14f, -0.08f)
    return PackPalette(
        accent = accent,
        onAccent = bestOn(accent),
        heroStart = heroStart,
        heroEnd = heroEnd,
        onHero = bestOnBoth(heroStart, heroEnd),
        container = container,
        onContainer = scheme.onSurface,
        accentOnContainer = ensureContrast(accent, container, 3f, if (dark) Color.White else Ink),
    )
}
