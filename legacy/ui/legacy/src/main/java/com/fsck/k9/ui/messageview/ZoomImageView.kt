package com.fsck.k9.ui.messageview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.compose.ui.geometry.Offset
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A picture fitted to the view, with pinch-to-zoom, a one-finger pan once enlarged, and a double
 * tap that jumps to one picture pixel per screen pixel (and back to fitted on the next).
 *
 * The pan follows the finger and stops when the finger does: no fling. A single tap does
 * nothing. The bitmap is only screen-sized, so past that the part of the original in view is
 * decoded at full detail by [detail] and drawn over it, a moment after the picture holds still.
 */
class ZoomImageView(context: Context, private val scope: CoroutineScope) : View(context) {
    private class Sharp(val bitmap: Bitmap, val area: Rect, val scale: Float)

    private var bitmap: Bitmap? = null
    private var fullW = 0
    private var fullH = 0
    private var scale = 1f
    private var offset = Offset.Zero
    private var sharp: Sharp? = null
    private var detailJob: Job? = null
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    /** The part of the original at [Rect] (its pixels) at the given scale, or null. */
    var detail: (suspend (Rect, Float) -> Bitmap?)? = null

    private val slop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private val doubleTapMs = ViewConfiguration.getDoubleTapTimeout().toLong()
    private val near = 96f * resources.displayMetrics.density

    // The gesture in progress.
    private var travelled = 0f
    private var pinched = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastSpan = 0f
    private var lastFingers = 0
    private var lastTapAt = 0L
    private var lastTapX = 0f
    private var lastTapY = 0f
    private var secondTap = false

    /** [original] is the original's size in pixels when it is more than [bitmap] holds. */
    fun setPicture(bitmap: Bitmap, originalW: Int?, originalH: Int?) {
        this.bitmap = bitmap
        fullW = originalW ?: bitmap.width
        fullH = originalH ?: bitmap.height
        scale = 1f
        offset = Offset.Zero
        sharp = null
        invalidate()
    }

    private fun fitW(b: Bitmap) = b.width * min(width.toFloat() / b.width, height.toFloat() / b.height)
    private fun fitH(b: Bitmap) = b.height * min(width.toFloat() / b.width, height.toFloat() / b.height)

    private fun maxZoom() = max(MAX_ZOOM, oneToOneScale(width.toFloat(), height.toFloat(), fullW, fullH))

    private fun clamp(o: Offset, s: Float): Offset {
        val b = bitmap ?: return o
        return clampOffset(o, s, fitW(b), fitH(b), width.toFloat(), height.toFloat())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        scale = 1f
        offset = Offset.Zero
        sharp = null
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val b = bitmap ?: return
        val cx = width / 2f + offset.x
        val cy = height / 2f + offset.y
        val fit = min(width.toFloat() / b.width, height.toFloat() / b.height)
        val s = fit * scale
        val dst = RectF(cx - b.width * s / 2, cy - b.height * s / 2, cx + b.width * s / 2, cy + b.height * s / 2)
        canvas.drawBitmap(b, null, dst, paint)

        val sh = sharp
        if (sh != null) {
            val perPixel = scale * min(width.toFloat() / fullW, height.toFloat() / fullH)
            val r = RectF(
                cx + (sh.area.left - fullW / 2f) * perPixel,
                cy + (sh.area.top - fullH / 2f) * perPixel,
                cx + (sh.area.right - fullW / 2f) * perPixel,
                cy + (sh.area.bottom - fullH / 2f) * perPixel,
            )
            if (kotlin.math.abs(perPixel - 1f) < 1e-3f && sh.scale >= 1f) {
                // Exactly one picture pixel per screen pixel: land on whole pixels so none blurs.
                val l = r.left.roundToInt()
                val t = r.top.roundToInt()
                r.set(l.toFloat(), t.toFloat(), (l + sh.area.width()).toFloat(), (t + sh.area.height()).toFloat())
            }
            canvas.drawBitmap(sh.bitmap, null, r, paint)
        }
    }

    private fun changed() {
        invalidate()
        scheduleDetail()
    }

    /**
     * Full detail for what is in view, once the picture has been enlarged past what the
     * screen-sized bitmap holds. Waits for the picture to hold still, and takes a window half a
     * screen bigger on every side so a small pan stays covered.
     */
    private fun scheduleDetail() {
        detailJob?.cancel()
        val b = bitmap ?: return
        val provider = detail ?: return
        val boxW = width.toFloat()
        val boxH = height.toFloat()
        val perPixel = scale * min(boxW / fullW, boxH / fullH)
        if (scale <= 1.02f || perPixel <= b.width.toFloat() / fullW) {
            sharp = null
            return
        }
        val visW = boxW / perPixel
        val visH = boxH / perPixel
        val left = fullW / 2f + (-boxW / 2 - offset.x) / perPixel
        val top = fullH / 2f + (-boxH / 2 - offset.y) / perPixel
        val want = min(1f, perPixel)
        val have = sharp
        if (have != null && have.scale >= want * 0.99f &&
            have.area.left <= left && have.area.top <= top &&
            have.area.right >= left + visW && have.area.bottom >= top + visH
        ) return
        detailJob = scope.launch {
            delay(150)
            val area = Rect(
                (left - visW / 2).toInt().coerceAtLeast(0),
                (top - visH / 2).toInt().coerceAtLeast(0),
                (left + visW * 1.5f).toInt().coerceAtMost(fullW),
                (top + visH * 1.5f).toInt().coerceAtMost(fullH),
            )
            if (area.width() < 1 || area.height() < 1) return@launch
            provider(area, want)?.let {
                sharp = Sharp(it, area, want)
                invalidate()
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (bitmap == null) return true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                travelled = 0f
                pinched = false
                secondTap = e.eventTime - lastTapAt <= doubleTapMs &&
                    hypot(e.x - lastTapX, e.y - lastTapY) <= near
                startTracking(e, -1)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                pinched = true
                startTracking(e, -1)
            }
            MotionEvent.ACTION_POINTER_UP -> startTracking(e, e.actionIndex)
            MotionEvent.ACTION_MOVE -> move(e)
            MotionEvent.ACTION_UP -> {
                if (!pinched && travelled < slop) {
                    if (secondTap) {
                        lastTapAt = 0L
                        val z = doubleTapZoom(
                            ZoomState(scale, offset),
                            Offset(e.x - width / 2f, e.y - height / 2f),
                            width.toFloat(), height.toFloat(), fullW, fullH,
                        )
                        scale = z.scale
                        offset = z.offset
                        changed()
                    } else {
                        lastTapAt = e.eventTime
                        lastTapX = e.x
                        lastTapY = e.y
                    }
                } else {
                    lastTapAt = 0L
                }
                if (scale < 1.02f) {
                    scale = 1f
                    offset = Offset.Zero
                    changed()
                }
            }
            MotionEvent.ACTION_CANCEL -> lastTapAt = 0L
        }
        return true
    }

    /** Re-reads the fingers' centre and spread; [skip] is a finger just lifted. */
    private fun startTracking(e: MotionEvent, skip: Int) {
        var n = 0
        var sx = 0f
        var sy = 0f
        for (i in 0 until e.pointerCount) {
            if (i == skip) continue
            sx += e.getX(i)
            sy += e.getY(i)
            n++
        }
        lastFingers = n
        if (n == 0) return
        lastX = sx / n
        lastY = sy / n
        var span = 0f
        for (i in 0 until e.pointerCount) {
            if (i == skip) continue
            span += hypot(e.getX(i) - lastX, e.getY(i) - lastY)
        }
        lastSpan = span / n
    }

    private fun move(e: MotionEvent) {
        var sx = 0f
        var sy = 0f
        val n = e.pointerCount
        for (i in 0 until n) {
            sx += e.getX(i)
            sy += e.getY(i)
        }
        val x = sx / n
        val y = sy / n
        if (n != lastFingers) {
            startTracking(e, -1)
            return
        }
        val dx = x - lastX
        val dy = y - lastY
        if (n >= 2) {
            var span = 0f
            for (i in 0 until n) span += hypot(e.getX(i) - x, e.getY(i) - y)
            span /= n
            val zoom = if (lastSpan > 0f) span / lastSpan else 1f
            val focus = Offset(x - width / 2f, y - height / 2f)
            val newScale = (scale * zoom).coerceIn(1f, maxZoom())
            // Keep the point between the fingers where it is while the scale changes.
            val moved = (offset - focus) * (newScale / scale) + focus + Offset(dx, dy)
            scale = newScale
            offset = clamp(moved, newScale)
            lastSpan = span
            changed()
        } else if (!pinched) {
            travelled += hypot(dx, dy)
            if (scale > 1f) {
                offset = clamp(offset + Offset(dx, dy), scale)
                changed()
            }
        }
        lastX = x
        lastY = y
    }

    override fun onDetachedFromWindow() {
        detailJob?.cancel()
        super.onDetachedFromWindow()
    }

    private companion object {
        const val MAX_ZOOM = 4f
    }
}
