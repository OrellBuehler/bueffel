package ch.orellbuehler.bueffel.data

import android.content.ContentResolver
import android.net.Uri
import ch.orellbuehler.bueffel.ui.addpack.ImportProblem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

sealed interface ReadOutcome {
    data class Text(
        val text: String,
    ) : ReadOutcome

    data class Failed(
        val problem: ImportProblem,
    ) : ReadOutcome
}

fun interface PackTextReader {
    suspend fun read(uri: Uri): ReadOutcome
}

class ContentResolverTextReader(
    private val resolver: ContentResolver,
) : PackTextReader {
    override suspend fun read(uri: Uri): ReadOutcome =
        withContext(Dispatchers.IO) {
            try {
                val stream = resolver.openInputStream(uri) ?: return@withContext ReadOutcome.Failed(ImportProblem.ReadFailed)
                stream.use { decodePackText(it) }
            } catch (_: IOException) {
                ReadOutcome.Failed(ImportProblem.ReadFailed)
            } catch (_: SecurityException) {
                ReadOutcome.Failed(ImportProblem.ReadFailed)
            }
        }
}

const val MAX_PACK_BYTES = 28 * 1024 * 1024

fun decodePackText(stream: InputStream): ReadOutcome {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(64 * 1024)
    while (true) {
        val read = stream.read(buffer)
        if (read < 0) break
        out.write(buffer, 0, read)
        if (out.size() > MAX_PACK_BYTES) return ReadOutcome.Failed(ImportProblem.TooLarge)
    }
    return decodeUtf8(out.toByteArray())
}

fun decodeUtf8(bytes: ByteArray): ReadOutcome {
    val decoder =
        Charsets.UTF_8
            .newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
    val text =
        try {
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            return ReadOutcome.Failed(ImportProblem.NotUtf8)
        }
    return ReadOutcome.Text(text)
}
