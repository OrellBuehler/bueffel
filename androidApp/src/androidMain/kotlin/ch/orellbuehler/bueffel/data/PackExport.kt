package ch.orellbuehler.bueffel.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val PACK_MIME_TYPE = "application/vnd.bueffel.pack+json"

fun packFileName(title: String): String {
    val slug =
        title
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(48)
            .trim('-')
    return "${slug.ifEmpty { "pack" }}.bueffel"
}

suspend fun writePackForSharing(
    context: Context,
    title: String,
    json: String,
): Uri =
    withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "exports")
        dir.deleteRecursively()
        dir.mkdirs()
        val file = File(dir, packFileName(title))
        file.writeText(json)
        FileProvider.getUriForFile(context, "${context.packageName}.files", file)
    }

suspend fun writePackToUri(
    context: Context,
    uri: Uri,
    json: String,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openOutputStream(uri, "wt") ?: return@withContext false
            stream.use { it.write(json.toByteArray(Charsets.UTF_8)) }
            true
        } catch (_: java.io.IOException) {
            false
        }
    }
