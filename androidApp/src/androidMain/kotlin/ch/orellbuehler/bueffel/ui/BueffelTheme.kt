package ch.orellbuehler.bueffel.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Violet = Color(0xFF5B47E0)

private val LightColors =
    lightColorScheme(
        primary = Violet,
        background = Color(0xFFFBF9FF),
        surface = Color(0xFFFBF9FF),
    )

private val DarkColors =
    darkColorScheme(
        primary = Color(0xFFC6BFFF),
        background = Color(0xFF131018),
        surface = Color(0xFF131018),
    )

@Composable
fun BueffelTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
