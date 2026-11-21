package ch.orellbuehler.jackdaw.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackPaletteTest {
    private val samples = listOf("#7C3AED", "#0F766E", "#C60B1E", "#D52B1E", "#1D4ED8", "#FFFF00", "#FFFFFF", "#000000", "#FFB238")

    @Test
    fun parsesSixDigitHexOnly() {
        assertEquals(Color(0xFF7C3AED), parseHexColor("#7C3AED"))
        assertEquals(Color(0xFF0F766E), parseHexColor("#0f766e"))
        assertNull(parseHexColor("7C3AED"))
        assertNull(parseHexColor("#FFF"))
        assertNull(parseHexColor("red"))
        assertNull(parseHexColor(null))
    }

    @Test
    fun textOnAnyPackColourIsReadable() {
        samples.forEach { hex ->
            val accent = parseHexColor(hex)!!
            assertTrue(contrastRatio(bestOn(accent), accent) >= 4.5f, "$hex needs readable text")
        }
    }

    @Test
    fun heroTextIsReadableOnBothGradientEnds() {
        samples.forEach { hex ->
            listOf(false, true).forEach { dark ->
                val palette = packPalette(hex, if (dark) DarkColors else LightColors, dark)
                assertTrue(contrastRatio(palette.onHero, palette.heroStart) >= 4.5f, "$hex dark=$dark start")
                assertTrue(contrastRatio(palette.onHero, palette.heroEnd) >= 4.5f, "$hex dark=$dark end")
            }
        }
    }

    @Test
    fun accentOnContainerReachesGraphicContrast() {
        samples.forEach { hex ->
            listOf(false, true).forEach { dark ->
                val palette = packPalette(hex, if (dark) DarkColors else LightColors, dark)
                assertTrue(contrastRatio(palette.accentOnContainer, palette.container) >= 3f, "$hex dark=$dark")
            }
        }
    }

    @Test
    fun missingColourFallsBackToTheBrand() {
        assertEquals(Violet, packPalette(null, LightColors, false).accent)
        assertNotNull(packPalette("nonsense", DarkColors, true).container)
    }
}
