@file:Suppress("CAST_NEVER_SUCCEEDS")

package ch.orellbuehler.bueffel.format

import platform.Foundation.NSString
import platform.Foundation.decomposedStringWithCanonicalMapping
import platform.Foundation.precomposedStringWithCanonicalMapping

internal actual fun toNfc(text: String): String =
    runCatching {
        (text as NSString).precomposedStringWithCanonicalMapping
    }.getOrDefault(text)

internal actual fun toNfd(text: String): String = runCatching { (text as NSString).decomposedStringWithCanonicalMapping }.getOrDefault(text)
