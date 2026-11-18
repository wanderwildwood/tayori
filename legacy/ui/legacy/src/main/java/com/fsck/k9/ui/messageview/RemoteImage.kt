package com.fsck.k9.ui.messageview

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
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

    /**
     * Whether [document] holds nothing: one the save screen made just now. One picked there to be
     * written over has something in it, and a size that cannot be read counts as something.
     * So on a provider that reports no size, a document made just now is not known to be empty and
     * is left behind empty when the download fails; it is never taken for one with a picture in it.
     */
    fun isEmpty(resolver: ContentResolver, document: Uri): Boolean = runCatching {
        resolver.query(document, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            cursor.moveToFirst() && !cursor.isNull(0) && cursor.getLong(0) == 0L
        } ?: false
    }.getOrDefault(false)

    /**
     * Fetches [source] (https only: plain http is refused by the network settings) into [destination].
     * Call off the main thread.
     *
     * The whole picture is downloaded into a temporary file in [cacheDir] first, and [destination] is
     * opened only once that has finished. So a download that fails, is cut off half way or turns out
     * too large leaves a file picked to be written over exactly as it was.
     */
    @Throws(IOException::class)
    fun copy(resolver: ContentResolver, source: Uri, destination: Uri, cacheDir: File) {
        val scheme = source.scheme?.lowercase()
        if (scheme != "https") throw IOException("unsupported address")
        val temp = File.createTempFile("remote-image", null, cacheDir)
        try {
            val connection = URL(source.toString()).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                if (connection.responseCode !in 200..299) throw IOException("server said ${connection.responseCode}")
                connection.inputStream.use { input ->
                    FileOutputStream(temp).use { out -> copyCapped(input, out) }
                }
            } finally {
                connection.disconnect()
            }
            val output = openForWriting(resolver, destination) ?: throw IOException("cannot open destination")
            output.use { out ->
                FileInputStream(temp).use { input -> input.copyTo(out) }
            }
        } finally {
            temp.delete()
        }
    }

    /** Copies [input] to [output], giving up once more than [limit] bytes have come. */
    @Throws(IOException::class)
    fun copyCapped(input: InputStream, output: OutputStream, limit: Long = MAX_BYTES) {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            total += read
            if (total > limit) throw IOException("too large")
            output.write(buffer, 0, read)
        }
    }

    /**
     * "wt" so a longer picture written over is cut to the new length; a provider that does not know
     * "wt" gets the plain "w".
     */
    private fun openForWriting(resolver: ContentResolver, destination: Uri): OutputStream? =
        try {
            resolver.openOutputStream(destination, "wt")
        } catch (e: IllegalArgumentException) {
            resolver.openOutputStream(destination, "w")
        } catch (e: FileNotFoundException) {
            resolver.openOutputStream(destination, "w")
        }
}
