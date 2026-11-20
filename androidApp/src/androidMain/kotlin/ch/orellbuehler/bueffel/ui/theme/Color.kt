package ch.orellbuehler.bueffel.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

val Violet = Color(0xFF5B47E0)
val VioletDeep = Color(0xFF3E2FB3)
val Amber = Color(0xFFFFB238)
val Ink = Color(0xFF17142A)
val Cream = Color(0xFFFAF7F2)
val SuccessGreen = Color(0xFF2FB67C)
val ErrorRed = Color(0xFFE5484D)

val LightColors: ColorScheme =
    lightColorScheme(
        primary = Violet,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE4E0FF),
        onPrimaryContainer = Color(0xFF1B0F6B),
        inversePrimary = Color(0xFFC4BBFF),
        secondary = Color(0xFF5E5A7E),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE5E1FA),
        onSecondaryContainer = Color(0xFF1B1832),
        tertiary = Amber,
        onTertiary = Ink,
        tertiaryContainer = Color(0xFFFFE6BC),
        onTertiaryContainer = Color(0xFF3A2500),
        background = Cream,
        onBackground = Ink,
        surface = Cream,
        onSurface = Ink,
        surfaceVariant = Color(0xFFEAE4DA),
        onSurfaceVariant = Color(0xFF5A566B),
        surfaceTint = Violet,
        inverseSurface = Ink,
        inverseOnSurface = Cream,
        error = ErrorRed,
        onError = Color.White,
        errorContainer = Color(0xFFFFE0DF),
        onErrorContainer = Color(0xFF5C0A0D),
        outline = Color(0xFF7B778C),
        outlineVariant = Color(0xFFD8D1C6),
        scrim = Color.Black,
        surfaceBright = Cream,
        surfaceDim = Color(0xFFE9E4DB),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF6F2EB),
        surfaceContainer = Color(0xFFF1ECE4),
        surfaceContainerHigh = Color(0xFFECE6DD),
        surfaceContainerHighest = Color(0xFFE6E0D6),
    )

val DarkColors: ColorScheme =
    darkColorScheme(
        primary = Color(0xFFBFB5FF),
        onPrimary = Color(0xFF2B1B9A),
        primaryContainer = Color(0xFF4536C4),
        onPrimaryContainer = Color(0xFFE4E0FF),
        inversePrimary = Violet,
        secondary = Color(0xFFC9C4E6),
        onSecondary = Color(0xFF302C4A),
        secondaryContainer = Color(0xFF46425F),
        onSecondaryContainer = Color(0xFFE5E1FA),
        tertiary = Amber,
        onTertiary = Color(0xFF3A2500),
        tertiaryContainer = Color(0xFF5A3A00),
        onTertiaryContainer = Color(0xFFFFE6BC),
        background = Color(0xFF14111F),
        onBackground = Color(0xFFECE9F7),
        surface = Color(0xFF14111F),
        onSurface = Color(0xFFECE9F7),
        surfaceVariant = Color(0xFF2B2740),
        onSurfaceVariant = Color(0xFFC6C1D9),
        surfaceTint = Color(0xFFBFB5FF),
        inverseSurface = Color(0xFFECE9F7),
        inverseOnSurface = Color(0xFF14111F),
        error = Color(0xFFFF8A8D),
        onError = Color(0xFF5C0A0D),
        errorContainer = Color(0xFF6B1A1D),
        onErrorContainer = Color(0xFFFFDAD9),
        outline = Color(0xFF918DA6),
        outlineVariant = Color(0xFF3D3952),
        scrim = Color.Black,
        surfaceBright = Color(0xFF332F45),
        surfaceDim = Color(0xFF14111F),
        surfaceContainerLowest = Color(0xFF0F0C18),
        surfaceContainerLow = Color(0xFF1A1727),
        surfaceContainer = Color(0xFF1E1B2D),
        surfaceContainerHigh = Color(0xFF282538),
        surfaceContainerHighest = Color(0xFF332F45),
    )

@Immutable
class BueffelColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val streak: Color,
)

val LightBueffelColors =
    BueffelColors(
        success = SuccessGreen,
        onSuccess = Ink,
        successContainer = Color(0xFFD7F4E6),
        onSuccessContainer = Color(0xFF07391F),
        streak = Amber,
    )

val DarkBueffelColors =
    BueffelColors(
        success = Color(0xFF4CD39B),
        onSuccess = Ink,
        successContainer = Color(0xFF0F4A32),
        onSuccessContainer = Color(0xFFC8F5E0),
        streak = Amber,
    )
