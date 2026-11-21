package ch.orellbuehler.jackdaw.samples

import ch.orellbuehler.jackdaw.format.Pack
import ch.orellbuehler.jackdaw.format.PackReader
import ch.orellbuehler.jackdaw.format.ReadResult

internal class Sample(
    val pack: Pack,
    val json: String,
)

internal object Samples {
    val all: List<Sample> by lazy {
        GENERATED_SAMPLE_PACKS.mapNotNull { (_, json) ->
            (PackReader.read(json) as? ReadResult.Accepted)?.let { Sample(it.pack, json) }
        }
    }
}
