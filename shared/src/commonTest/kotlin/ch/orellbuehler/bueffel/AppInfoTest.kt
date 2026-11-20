package ch.orellbuehler.bueffel

import kotlin.test.Test
import kotlin.test.assertEquals

class AppInfoTest {
    @Test
    fun appNameIsBueffel() {
        assertEquals("Büffel", appName())
    }
}
