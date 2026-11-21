package ch.orellbuehler.jackdaw.format

internal expect fun toNfc(text: String): String

internal expect fun toNfd(text: String): String

internal fun isSpecWhitespace(c: Char): Boolean =
    when (c.code) {
        in 0x09..0x0D, 0x20, 0x85, 0xA0, 0x1680, in 0x2000..0x200A, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000 -> true
        else -> false
    }

internal fun String.trimSpec(): String = trim { isSpecWhitespace(it) }

internal fun String.isBlankSpec(): Boolean = all { isSpecWhitespace(it) }
