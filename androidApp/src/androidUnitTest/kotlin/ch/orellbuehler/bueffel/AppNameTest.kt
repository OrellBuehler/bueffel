package ch.orellbuehler.bueffel

import kotlin.test.Test
import kotlin.test.assertEquals

class AppNameTest {
    @Test
    fun sharedModuleIsLinked() {
        assertEquals("Büffel", appName())
    }
}
