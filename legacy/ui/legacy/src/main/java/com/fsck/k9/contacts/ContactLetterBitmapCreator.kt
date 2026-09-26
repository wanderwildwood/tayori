package com.fsck.k9.contacts

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import com.fsck.k9.mail.Address

/**
 * Draw a `Bitmap` containing the "contact letter" obtained by [ContactLetterExtractor].
 */
class ContactLetterBitmapCreator(
    private val letterExtractor: ContactLetterExtractor,
    val config: ContactLetterBitmapConfig,
) {
    fun drawBitmap(bitmap: Bitmap, pictureSizeInPx: Int, address: Address): Bitmap {
        val canvas = Canvas(bitmap)

        val backgroundColor = calcUnknownContactColor(address)
        bitmap.eraseColor(backgroundColor)

        val letter = letterExtractor.extractContactLetter(address)

        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            setARGB(255, 255, 255, 255)
            textSize = pictureSizeInPx.toFloat() * 0.65f
        }

        val rect = Rect()
        paint.getTextBounds(letter, 0, 1, rect)

        val width = paint.measureText(letter)
        canvas.drawText(
            letter,
            pictureSizeInPx / 2f - width / 2f,
            pictureSizeInPx / 2f + rect.height() / 2f,
            paint,
        )

        return bitmap
    }

    // One ink for every sender. Upstream hashes the address into a palette of pastels, which an
    // E Ink panel shows as a set of near-identical greys.
    @Suppress("UNUSED_PARAMETER")
    fun calcUnknownContactColor(address: Address): Int = 0xFF000000.toInt()

    fun signatureOf(address: Address): String {
        return calcUnknownContactColor(address).toString()
    }
}
