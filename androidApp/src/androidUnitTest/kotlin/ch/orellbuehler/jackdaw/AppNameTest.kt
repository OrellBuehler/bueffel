package ch.orellbuehler.jackdaw

import kotlin.test.Test
import kotlin.test.assertEquals

class AppNameTest {
    @Test
    fun sharedModuleIsLinked() {
        assertEquals("Jackdaw", appName())
    }
}
