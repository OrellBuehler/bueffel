package ch.orellbuehler.bueffel.store

import ch.orellbuehler.bueffel.format.CardItem
import ch.orellbuehler.bueffel.format.ChoiceItem
import ch.orellbuehler.bueffel.format.ClozeItem
import ch.orellbuehler.bueffel.format.ClozePart
import ch.orellbuehler.bueffel.format.Item
import ch.orellbuehler.bueffel.format.OpenItem
import ch.orellbuehler.bueffel.format.OrderItem
import ch.orellbuehler.bueffel.format.RichText
import ch.orellbuehler.bueffel.format.TextItem
import ch.orellbuehler.bueffel.format.TrueFalseItem
import ch.orellbuehler.bueffel.format.isSpecWhitespace
import ch.orellbuehler.bueffel.format.parseCloze

private const val MAX_DISPLAY_LENGTH = 200

internal fun displayTitle(item: Item): String =
    flatten(
        when (item) {
            is CardItem -> plain(item.front)
            is ChoiceItem -> plain(item.question)
            is TextItem -> plain(item.question)
            is ClozeItem -> clozeText(item, filled = false)
            is TrueFalseItem -> plain(item.statement)
            is OrderItem -> plain(item.prompt)
            is OpenItem -> plain(item.question)
        },
    )

internal fun displaySubtitle(item: Item): String =
    flatten(
        when (item) {
            is CardItem -> plain(item.back)
            is ChoiceItem -> item.options.filter { it.correct }.joinToString(", ") { plain(it.text) }
            is TextItem -> item.answers.first()
            is ClozeItem -> clozeText(item, filled = true)
            is TrueFalseItem -> if (item.answer) "\u2713" else "\u2717"
            is OrderItem -> item.sequence.joinToString(" > ") { plain(it) }
            is OpenItem -> item.answer?.let { plain(it) } ?: item.keyPoints.joinToString("; ") { plain(it) }
        },
    )

private fun plain(raw: String): String = RichText.parse(raw).plain

private fun clozeText(
    item: ClozeItem,
    filled: Boolean,
): String =
    parseCloze(item.text).joinToString("") { part ->
        when (part) {
            is ClozePart.Text -> plain(part.raw)
            is ClozePart.Gap -> if (filled) part.answers.first() else "\u2026"
        }
    }

private fun flatten(text: String): String {
    val out = StringBuilder()
    var pendingSpace = false
    for (c in text) {
        if (isSpecWhitespace(c)) {
            pendingSpace = out.isNotEmpty()
        } else {
            if (pendingSpace) out.append(' ')
            pendingSpace = false
            out.append(c)
        }
    }
    if (out.length <= MAX_DISPLAY_LENGTH) return out.toString()
    var end = MAX_DISPLAY_LENGTH - 1
    if (out[end - 1].isHighSurrogate()) end--
    return out.substring(0, end).trimEnd() + "\u2026"
}
