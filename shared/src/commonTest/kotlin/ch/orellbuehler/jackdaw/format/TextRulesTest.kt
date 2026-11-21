package ch.orellbuehler.jackdaw.format

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TextRulesTest {
    private fun spans(text: String) = parseMarkup(text).map { Triple(it.text, it.bold || it.italic, it.code) }

    @Test
    fun richTextExposesFlagsAndPlainText() {
        val rich = RichText.parse("a **b** *c* `d`")
        assertEquals(listOf("a ", "b", " ", "c", " ", "d"), rich.spans.map { it.text })
        assertEquals(listOf(false, true, false, false, false, false), rich.spans.map { it.bold })
        assertEquals(listOf(false, false, false, true, false, false), rich.spans.map { it.italic })
        assertEquals(listOf(false, false, false, false, false, true), rich.spans.map { it.code })
        assertEquals("a b c d", rich.plain)
        assertTrue(RichText.parse("").isEmpty)
        assertEquals(emptyList(), RichText.fromPlain("").spans)
        assertEquals("**x**", RichText.fromPlain("**x**").plain)
    }

    @Test
    fun lineEndingsAreNormalised() {
        assertEquals("a\nb\nc", RichText.parse("a\r\nb\rc").plain)
        assertEquals(listOf("*a\nb*"), parseMarkup("*a\r\nb*").map { it.text })
    }

    @Test
    fun escapesInsideBoldAndItalic() {
        assertEquals(listOf(Triple("a*b", true, false)), spans("**a\\*b**"))
        assertEquals(listOf(Triple("a|b", true, false)), spans("*a\\|b*"))
        assertEquals(listOf(Triple("a\\b", false, false)), spans("a\\b"))
    }

    @Test
    fun whitespaceDefinitionFollowsTheSpecification() {
        assertTrue(isSpecWhitespace(' '))
        assertTrue(isSpecWhitespace('\u3000'))
        assertTrue(isSpecWhitespace('\u0085'))
        assertTrue(isSpecWhitespace('\u00A0'))
        assertTrue(isSpecWhitespace('\u202F'))
        assertFalse(isSpecWhitespace('\u200B'))
        assertFalse(isSpecWhitespace('\u001C'))
        assertTrue("  \t".isBlankSpec())
        assertEquals("x", " x ".trimSpec())
    }

    @Test
    fun italicClosingWithNoBreakSpaceIsLiteral() {
        assertEquals(listOf(Triple("*a\u00A0*", false, false)), spans("*a\u00A0*"))
    }

    @Test
    fun clozeGapDetails() {
        val parts = parseCloze("A {{b|c::d}} e {{f}}")
        assertEquals(4, parts.size)
        val first = parts[1] as ClozePart.Gap
        assertEquals(listOf("b", "c"), first.answers)
        assertEquals("d", first.hint)
        assertNull((parts[3] as ClozePart.Gap).hint)
        assertEquals("A ", (parts[0] as ClozePart.Text).raw)
    }

    @Test
    fun clozeKeepsEscapesForTheMarkupStep() {
        val parts = parseCloze("a \\* {{b}} \\{{c}}")
        assertEquals("a \\* ", (parts[0] as ClozePart.Text).raw)
        assertEquals(" \\{{c}}", (parts[2] as ClozePart.Text).raw)
        assertEquals("a * ", RichText.parse((parts[0] as ClozePart.Text).raw).plain)
    }

    @Test
    fun normalizeWithMultipleAcceptedAnswers() {
        assertTrue(matchesAnswer("COLOR!", listOf("colour", "color"), false, false))
        assertFalse(matchesAnswer("", listOf("", "a"), false, false))
        assertFalse(matchesAnswer("  ", listOf("  "), false, false))
        assertTrue(matchesAnswer("Cafe", listOf("Café"), false, true))
        assertFalse(matchesAnswer("Cafe", listOf("Café"), false, false))
        assertTrue(matchesAnswer("café", listOf("Café"), false, false))
        assertEquals("a b", normalize("a   b", false, false))
        assertEquals("¿¡", normalize("¿¡", false, false))
        assertEquals("ßa", normalize("ßA", false, false))
    }
}
