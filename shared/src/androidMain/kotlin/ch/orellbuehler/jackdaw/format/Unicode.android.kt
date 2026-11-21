package ch.orellbuehler.jackdaw.format

import java.text.Normalizer

internal actual fun toNfc(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFC)

internal actual fun toNfd(text: String): String = Normalizer.normalize(text, Normalizer.Form.NFD)
