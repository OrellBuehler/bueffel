package ch.orellbuehler.bueffel.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

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

fun parseHexColor(hex: String?): Color? {
    if (hex == null) return null
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
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
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(heroStart.toArgb(), hsl)
    hsl[0] = (hsl[0] + 14f) % 360f
    hsl[2] = (hsl[2] - 0.08f).coerceIn(0.12f, 0.9f)
    val heroEnd = Color(ColorUtils.HSLToColor(hsl))
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
