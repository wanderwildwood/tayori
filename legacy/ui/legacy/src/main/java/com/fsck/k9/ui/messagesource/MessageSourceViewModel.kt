package com.fsck.k9.ui.messagesource

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.provider.RawMessageProvider
import com.fsck.k9.ui.base.loader.LoaderState
import com.fsck.k9.ui.base.loader.liveDataLoader
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * A message as it was received: headers, a blank line, then the body. [text] is what Copy puts on the clipboard;
 * [rows] is the same text cut into rows the list can lay out.
 */
data class MessageSource(
    val text: String,
    val rows: List<String>,
    val shownKilobytes: Int,
    val totalKilobytes: Int,
    val isShortened: Boolean,
)

private typealias MessageSourceState = LoaderState<MessageSource>

class MessageSourceViewModel(private val context: Context) : ViewModel() {
    private var sourceLiveData: LiveData<MessageSourceState>? = null

    fun loadSource(messageReference: MessageReference): LiveData<MessageSourceState> {
        return sourceLiveData ?: liveDataLoader { readSource(messageReference) }.also { sourceLiveData = it }
    }

    private fun readSource(messageReference: MessageReference): MessageSource {
        val uri = RawMessageProvider.getRawMessageUri(messageReference)
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Message not available")
        val output = ByteArrayOutputStream()
        var total = 0L
        inputStream.use { input ->
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                val room = (MAX_BYTES - output.size()).coerceAtLeast(0)
                output.write(buffer, 0, minOf(count, room))
                total += count
            }
        }

        val text = output.toString(Charsets.UTF_8.name())
        return MessageSource(
            text = text,
            rows = splitIntoRows(text),
            shownKilobytes = output.size().toKilobytes(),
            totalKilobytes = total.toKilobytes(),
            isShortened = total > output.size(),
        )
    }

    private fun Number.toKilobytes() = ((toLong() + 1023) / 1024).toInt()

    companion object {
        private const val BUFFER_SIZE = 16 * 1024
        internal const val MAX_BYTES = 300 * 1024
        internal const val MAX_ROW_LENGTH = 400

        /**
         * One row per line of the source; a line longer than [MAX_ROW_LENGTH] continues on the next row so one
         * row never becomes a whole screen of text.
         */
        internal fun splitIntoRows(text: String): List<String> {
            val rows = mutableListOf<String>()
            for (line in text.split("\r\n", "\n")) {
                if (line.length <= MAX_ROW_LENGTH) {
                    rows.add(line)
                } else {
                    line.chunked(MAX_ROW_LENGTH).forEach { rows.add(it) }
                }
            }
            return rows
        }
    }
}
