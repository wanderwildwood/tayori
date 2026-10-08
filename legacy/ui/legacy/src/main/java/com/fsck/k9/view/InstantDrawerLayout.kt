package com.fsck.k9.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

/**
 * A DrawerLayout whose drawer appears and disappears at once.
 *
 * On E Ink a slide is a smear of half-drawn frames, so every way of opening and closing the
 * drawer is instant: the menu button, Back, picking a folder, a tap on the page beside the open
 * drawer, and letting go of a swipe. (The swipe itself still follows the finger.)
 */
class InstantDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : DrawerLayout(context, attrs, defStyleAttr) {

    private var state = STATE_IDLE
    private var releasedFromDrag = false
    private var slidingDrawer: View? = null
    private var slideOffset = 0f
    private var settlingOpen: Boolean? = null

    init {
        addDrawerListener(
            object : SimpleDrawerListener() {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    slidingDrawer = drawerView
                    this@InstantDrawerLayout.slideOffset = slideOffset
                }

                override fun onDrawerStateChanged(newState: Int) {
                    releasedFromDrag = state == STATE_DRAGGING && newState == STATE_SETTLING
                    state = newState
                    settlingOpen = null
                }
            },
        )
    }

    override fun openDrawer(drawerView: View, animate: Boolean) = super.openDrawer(drawerView, false)

    override fun closeDrawer(drawerView: View, animate: Boolean) = super.closeDrawer(drawerView, false)

    /**
     * DrawerLayout still slides on its own after a swipe is let go of and after a tap beside the
     * open drawer. Its scroller can't be stopped from outside, so it runs its course unseen: on
     * each of its frames the drawer is put straight at the end before anything is drawn.
     */
    override fun computeScroll() {
        val before = slideOffset
        super.computeScroll()
        if (state != STATE_SETTLING) return
        val drawer = slidingDrawer ?: return
        val open = settlingOpen ?: when {
            slideOffset < before -> false
            // Opening on its own without a drag is the peek a held edge touch gives; leave that be.
            slideOffset > before && releasedFromDrag -> true
            else -> return
        }
        settlingOpen = open

        if (open) super.openDrawer(drawer, false) else super.closeDrawer(drawer, false)
        // DrawerLayout places the drawer from a rounded offset, which can leave it a pixel out.
        val gravity = (drawer.layoutParams as LayoutParams).gravity
        val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
        val onLeft = (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT
        val left = when {
            open && onLeft -> 0
            open -> width - drawer.width
            onLeft -> -drawer.width
            else -> width
        }
        drawer.offsetLeftAndRight(left - drawer.left)
    }
}
