package com.fsck.k9.ui.messageview

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.ImageDecoder
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.fsck.k9.ui.R
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An image attachment, full screen, handed to nobody: pinch to zoom, one finger to pan once
 * enlarged, double tap for one picture pixel per screen pixel and again to fit. See [ZoomImageView].
 *
 * Only a screen-sized copy is kept; past that, the part in view is decoded from the original.
 */
class ImageViewerActivity : ComponentActivity() {
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uri = intent.data ?: return finish()
        val view = ZoomImageView(this, lifecycleScope)
        setContentView(FrameLayout(this).apply { addView(view) })
        lifecycleScope.launch {
            val screen = DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }
            val side = max(screen.widthPixels, screen.heightPixels) * 2
            val loaded = withContext(Dispatchers.IO) { runCatching { decode(uri, side) }.getOrNull() }
            if (loaded == null) {
                Toast.makeText(this@ImageViewerActivity, R.string.image_viewer_failed, Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }
            view.detail = { area, scale -> withContext(Dispatchers.IO) { runCatching { region(area, scale) }.getOrNull() } }
            view.setPicture(loaded.bitmap, loaded.width, loaded.height)
        }
    }

    private class Loaded(val bitmap: Bitmap, val width: Int, val height: Int)

    private fun decode(uri: Uri, side: Int): Loaded {
        if (Build.VERSION.SDK_INT >= 28) {
            var w = 0
            var h = 0
            val bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, info, _ ->
                w = info.size.width
                h = info.size.height
                val longest = max(w, h)
                if (longest > side) {
                    val s = side.toFloat() / longest
                    decoder.setTargetSize((w * s).toInt().coerceAtLeast(1), (h * s).toInt().coerceAtLeast(1))
                }
                // Software: a hardware bitmap cannot be read back, and the panel has no GPU to gain from one.
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
            return Loaded(bitmap, w, h)
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= side) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = contentResolver.openInputStream(uri)!!.use { BitmapFactory.decodeStream(it, null, opts) }!!
        return Loaded(bitmap, bounds.outWidth, bounds.outHeight)
    }

    /** The window [area] of the original (its pixels), thinned to [scale] (1 = every pixel). */
    private fun region(area: Rect, scale: Float): Bitmap? {
        if (Build.VERSION.SDK_INT >= 28) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { decoder, info, _ ->
                val crop = Rect(area)
                if (scale < 1f) {
                    val w = (info.size.width * scale).toInt().coerceAtLeast(1)
                    val h = (info.size.height * scale).toInt().coerceAtLeast(1)
                    decoder.setTargetSize(w, h)
                    crop.set(
                        (area.left * scale).toInt().coerceIn(0, w - 1),
                        (area.top * scale).toInt().coerceIn(0, h - 1),
                        (area.right * scale).toInt().coerceIn(1, w),
                        (area.bottom * scale).toInt().coerceIn(1, h),
                    )
                }
                decoder.setCrop(crop)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val opts = BitmapFactory.Options().apply {
            var sample = 1
            while (scale * sample * 2 <= 1f) sample *= 2
            inSampleSize = sample
        }
        val decoder = contentResolver.openInputStream(uri)!!.use { BitmapRegionDecoder.newInstance(it, false) } ?: return null
        return try {
            decoder.decodeRegion(area, opts)
        } finally {
            decoder.recycle()
        }
    }

    companion object {
        /** Types this viewer can draw; anything else still goes to another app. */
        private val TYPES = setOf(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp", "image/bmp",
            "image/heic", "image/heif", "image/avif",
        )

        fun canShow(mimeType: String?): Boolean = mimeType?.lowercase() in TYPES

        fun intent(context: Context, uri: Uri): Intent =
            Intent(context, ImageViewerActivity::class.java).apply {
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}
