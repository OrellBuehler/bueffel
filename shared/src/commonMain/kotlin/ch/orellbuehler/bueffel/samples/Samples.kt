package ch.orellbuehler.bueffel.samples

import ch.orellbuehler.bueffel.format.Pack
import ch.orellbuehler.bueffel.format.PackReader
import ch.orellbuehler.bueffel.format.ReadResult

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
