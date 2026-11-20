package ch.orellbuehler.bueffel.ui.nav

import ch.orellbuehler.bueffel.study.PracticeFilter
import ch.orellbuehler.bueffel.study.SessionMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutesTest {
    @Test
    fun learnRouteMapsToTheWholePack() {
        val config = StudyRoute("p").toConfig()
        assertEquals(SessionMode.LEARN, config.mode)
        assertNull(config.sectionId)
        assertEquals(0, config.size)
        assertEquals(-1, config.newLimit)
        assertEquals(PracticeFilter.ALL, config.filter)
    }

    @Test
    fun practiceRouteCarriesSectionAndFilter() {
        val config = StudyRoute("p", SessionMode.PRACTICE.name, "chapter-1", PracticeFilter.QUIZ.name).toConfig()
        assertEquals(SessionMode.PRACTICE, config.mode)
        assertEquals("chapter-1", config.sectionId)
        assertEquals(PracticeFilter.QUIZ, config.filter)
    }

    @Test
    fun unknownValuesFallBackToDefaults() {
        val config = StudyRoute("p", "NOPE", null, "NOPE").toConfig()
        assertEquals(SessionMode.LEARN, config.mode)
        assertEquals(PracticeFilter.ALL, config.filter)
    }

    @Test
    fun ungroupedSectionIsDistinctFromTheWholePack() {
        assertEquals("", StudyRoute("p", sectionId = "").toConfig().sectionId)
    }
}
