package ch.orellbuehler.bueffel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.orellbuehler.bueffel.store.ThemePreference

val BueffelShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

val LocalBueffelColors = staticCompositionLocalOf { LightBueffelColors }
val LocalDarkTheme = staticCompositionLocalOf { false }

val MaterialTheme.bueffel: BueffelColors
    @Composable
    @ReadOnlyComposable
    get() = LocalBueffelColors.current

val MaterialTheme.isDark: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalDarkTheme.current

val MaterialTheme.cardSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (LocalDarkTheme.current) colorScheme.surfaceContainerHigh else colorScheme.surfaceContainerLowest

@Composable
fun BueffelTheme(
    preference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit,
) {
    val dark =
        when (preference) {
            ThemePreference.SYSTEM -> isSystemInDarkTheme()
            ThemePreference.LIGHT -> false
            ThemePreference.DARK -> true
        }
    CompositionLocalProvider(
        LocalBueffelColors provides if (dark) DarkBueffelColors else LightBueffelColors,
        LocalDarkTheme provides dark,
    ) {
        MaterialTheme(
            colorScheme = if (dark) DarkColors else LightColors,
            typography = BueffelTypography,
            shapes = BueffelShapes,
            content = content,
        )
    }
}
