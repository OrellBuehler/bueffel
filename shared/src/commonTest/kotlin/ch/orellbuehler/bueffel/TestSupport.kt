package ch.orellbuehler.bueffel

import ch.orellbuehler.bueffel.format.Pack
import ch.orellbuehler.bueffel.format.PackReader
import ch.orellbuehler.bueffel.format.ReadResult
import kotlin.test.fail
import kotlin.time.Clock
import kotlin.time.Instant

class FakeClock(
    var millis: Long = 1_700_000_000_000L,
) : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(millis)

    fun advanceMinutes(minutes: Long) {
        millis += minutes * 60_000L
    }

    fun advanceDays(days: Long) {
        millis += days * 86_400_000L
    }
}

internal fun packJson(
    vararg items: String,
    study: String? = null,
    sections: String? = null,
    extra: String = "",
): String =
    buildString {
        append("""{"format":"bueffel-pack","version":1,"id":"test.pack","title":"Test Pack",""")
        if (study != null) append(""""study":$study,""")
        if (sections != null) append(""""sections":$sections,""")
        append(extra)
        append(""""items":[${items.joinToString(",")}]}""")
    }

internal fun readPack(json: String): Pack {
    val result = PackReader.read(json)
    if (result !is ReadResult.Accepted) fail("pack rejected: " + (result as ReadResult.Rejected).error)
    return result.pack
}

internal fun card(
    id: String,
    front: String,
    back: String,
    extra: String = "",
): String = """{"id":"$id","type":"card","front":"$front","back":"$back"$extra}"""
