package com.fsck.k9.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * A swipe moves the list one page, and then stops, and every page opens on a row.
 *
 * The same rules as the Compose lists' `PagedColumn` (3 Nov 2026: pages end on whole rows),
 * for the screens that are still RecyclerViews -- settings, folder settings, the message source,
 * and the inbox:
 *
 * - a turn forward brings the first row that did not fit to the top of the page;
 * - a row that does not fit at the foot is not drawn, and cannot be pressed, until the next page;
 * - a blank tail after the last row lets that row come to the top, so the last page also starts
 *   on a row and is never pulled back to end flush, half a row showing at its top;
 * - a turn back goes one page back and lands on a row.
 *
 * [showsCutRow] is the inbox's exception (5 Nov 2026): the row the foot cuts through stays in
 * sight and can be pressed, so a page has no blank band at its foot, and the next page opens on
 * it, whole. There is no blank tail either: the last page ends flush, its bottom padding lifting
 * the last row clear of the compose button, and may open on the bottom of a row the page before
 * already showed.
 *
 * A row taller than the whole page is read a page at a time within it.
 *
 * MMD's own lists step rather than scroll, because a panel that redraws in full renders inertia
 * as a smear. Only plainly vertical gestures are claimed, so a sideways swipe still reaches the
 * swipe actions underneath. Once a page has turned the rest of the drag is swallowed: one swipe
 * is one page, however far the finger keeps travelling.
 */
fun RecyclerView.turnsAPageOnSwipe(showsCutRow: Boolean = false) {
    // Letting go stops the list rather than throwing it across four screens of history.
    onFlingListener = object : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int) = true
    }

    if (!showsCutRow) addItemDecoration(WholeRowPages(this))

    val slop = ViewConfiguration.get(context).scaledTouchSlop
    addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
        private var downX = 0f
        private var downY = 0f
        private var turned = false

        // A press that starts on the hidden row at the foot is ours: the row is not there yet.
        private var onHiddenRow = false

        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.x
                    downY = e.y
                    turned = false
                    val hiddenFrom = if (showsCutRow) null else rv.hiddenFrom()
                    onHiddenRow = hiddenFrom != null && e.y >= hiddenFrom
                    return onHiddenRow
                }
                MotionEvent.ACTION_MOVE -> return turnIfSwiped(rv, e)
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            if (e.actionMasked == MotionEvent.ACTION_MOVE) turnIfSwiped(rv, e)
        }

        private fun turnIfSwiped(rv: RecyclerView, e: MotionEvent): Boolean {
            if (turned) return true
            val dy = e.y - downY
            val dx = e.x - downX
            if (abs(dy) > slop && abs(dy) > abs(dx)) {
                turned = true
                rv.turnPage(forward = dy < 0)
                return true
            }
            return onHiddenRow
        }

        override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) = Unit
    })
}

/**
 * Where a page's rows may sit. A list that clips to its padding keeps its rows inside it. One
 * that does not is the inbox, whose rows run to the bottom of the screen, so its page is the
 * whole view: its bottom padding only shows after the last row, letting it scroll up past the
 * compose button.
 */
private val RecyclerView.page get() = if (clipToPadding) height - paddingTop - paddingBottom else height
private val RecyclerView.topEdge get() = if (clipToPadding) paddingTop else 0
private val RecyclerView.bottomEdge get() = topEdge + page

/** The children in the order they stand on the screen, with their dividers. */
private fun RecyclerView.rows(): List<View> =
    (0 until childCount).map { getChildAt(it) }.sortedBy { it.top }

private fun RecyclerView.rowTop(row: View) = layoutManager?.getDecoratedTop(row) ?: row.top

/** A row's foot, its divider included and the blank tail ([WholeRowPages]), if any, left out. */
private fun RecyclerView.rowBottom(row: View): Int {
    val decorated = layoutManager?.getDecoratedBottom(row) ?: row.bottom
    return if (isLastRow(row) && hasBlankTail) {
        row.bottom + (decorated - row.bottom - page).coerceAtLeast(0)
    } else {
        decorated
    }
}

private val RecyclerView.hasBlankTail
    get() = (0 until itemDecorationCount).any { getItemDecorationAt(it) is WholeRowPages }

private fun RecyclerView.isLastRow(row: View): Boolean {
    val position = getChildAdapterPosition(row)
    val count = adapter?.itemCount ?: return false
    return position != RecyclerView.NO_POSITION && position == count - 1
}

/**
 * The top of the first row that does not fit on the page, which is hidden (with everything
 * below it) until the next one; null when the page ends on the last row. A row that starts at the top of the page and is taller than it
 * is never hidden: it is read a page at a time.
 */
private fun RecyclerView.hiddenFrom(): Int? {
    // The first row that does not fit, whether the foot cuts it or it starts below the foot (a
    // list that does not clip, like the inbox, would otherwise show it in its bottom padding).
    val cut = rows().firstOrNull { rowBottom(it) > bottomEdge } ?: return null
    return rowTop(cut).takeIf { it > topEdge }
}

/** One page on: the first row that did not fit comes to the top. */
private fun RecyclerView.turnPage(forward: Boolean) {
    val page = page
    if (page <= 0) return
    if (!forward) {
        turnPageBack(page)
        return
    }
    val top = topEdge
    val bottom = bottomEdge
    val rows = rows()
    val next = rows.firstOrNull { rowBottom(it) > bottom }
    when {
        // Every row showing fits. If they run on past the last one laid out, the next starts
        // right after it; if that was the last row of all, this is the last page.
        next == null -> {
            val last = rows.lastOrNull() ?: return
            if (isLastRow(last)) return
            scrollBy(0, rowBottom(last) - top)
        }
        // Inside a row taller than the page: read on through it a page at a time.
        rowTop(next) <= top -> scrollBy(0, minOf(page, rowBottom(next) - top))
        else -> scrollBy(0, rowTop(next) - top)
    }
}

/**
 * One page back, landing on a row. The row that opened this page goes to the foot, just out of
 * sight, and the page fills with the rows above it, starting on the first one that fits whole.
 * At the start of the list that is simply the first row.
 */
private fun RecyclerView.turnPageBack(page: Int) {
    val top = topEdge
    val opener = rows().firstOrNull { rowBottom(it) > top }
    val openerPosition = opener?.let { getChildAdapterPosition(it) } ?: RecyclerView.NO_POSITION
    // Mid-way through a tall row: back a page within it, or to its top. (The inbox's last page
    // may also open part-way down a row that is not tall; that row is simply turned back past.)
    if (opener != null && rowTop(opener) < top && rowBottom(opener) - rowTop(opener) > page) {
        scrollBy(0, -minOf(page, top - rowTop(opener)))
        return
    }
    scrollBy(0, -page)
    if (!canScrollVertically(-1)) return

    val openerNow = (0 until childCount).map { getChildAt(it) }
        .firstOrNull { getChildAdapterPosition(it) == openerPosition }
    val limit = openerNow?.let { rowTop(it) } ?: bottomEdge
    // The first row that now starts inside the page, above the opener. None means the row just
    // above the opener is taller than the page; it is read from its last page backwards.
    val first = rows().firstOrNull { rowTop(it) >= top && rowTop(it) < limit } ?: return
    scrollBy(0, rowTop(first) - top)
}

/**
 * Draws the page: a blank tail after the last row, so it can come to the top, and the row the
 * foot of the page cuts through painted out until the next page brings it whole.
 */
private class WholeRowPages(list: RecyclerView) : RecyclerView.ItemDecoration() {
    private val paint = Paint().apply { color = backgroundOf(list) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        if (position != RecyclerView.NO_POSITION && position == state.itemCount - 1) {
            outRect.bottom += parent.page.coerceAtLeast(0)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val from = parent.hiddenFrom() ?: return
        c.drawRect(0f, from.toFloat(), parent.width.toFloat(), parent.height.toFloat(), paint)
    }

    private companion object {
        fun backgroundOf(list: RecyclerView): Int {
            val value = TypedValue()
            return if (list.context.theme.resolveAttribute(android.R.attr.colorBackground, value, true) &&
                value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT
            ) {
                value.data
            } else {
                Color.WHITE
            }
        }
    }
}
