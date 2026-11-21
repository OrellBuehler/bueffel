package ch.orellbuehler.jackdaw

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun appNameIsJackdaw() {
        assertEquals("Jackdaw", appName())
    }
}
