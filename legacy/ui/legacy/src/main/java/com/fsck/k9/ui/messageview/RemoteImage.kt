package com.fsck.k9.ui.messageview

import android.content.ContentResolver
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * A picture that lives on a web server rather than inside the message. Saving one goes through
 * the system "save as" screen like any attachment, so nothing lands in a shared folder unasked.
 */
internal object RemoteImage {
    val scope by lazy { CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) }

    private const val MAX_BYTES = 50L * 1024 * 1024

    /** The last part of the address, with anything a file name cannot hold taken out. */
    fun fileName(lastPathSegment: String?): String {
        val cleaned = lastPathSegment.orEmpty().replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"), "_").trim().take(80)
        return if (cleaned.isBlank() || cleaned.all { it == '.' || it == '_' }) "image" else cleaned
    }

    /** Taken from the file name's ending; anything unknown is saved as plain bytes. */
    fun mimeType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    /** Fetches [source] (http or https only) into [destination]. Call off the main thread. */
    @Throws(IOException::class)
    fun copy(resolver: ContentResolver, source: Uri, destination: Uri) {
        val scheme = source.scheme?.lowercase()
        if (scheme != "http" && scheme != "https") throw IOException("unsupported address")
        val connection = URL(source.toString()).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            if (connection.responseCode !in 200..299) throw IOException("server said ${connection.responseCode}")
            val output = resolver.openOutputStream(destination) ?: throw IOException("cannot open destination")
            output.use { out ->
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                        if (total > MAX_BYTES) throw IOException("too large")
                        out.write(buffer, 0, read)
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
