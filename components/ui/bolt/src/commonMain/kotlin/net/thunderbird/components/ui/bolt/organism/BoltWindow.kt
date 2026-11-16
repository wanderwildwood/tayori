package net.thunderbird.components.ui.bolt.organism

import androidx.compose.runtime.Composable

/**
 * A window the design system opens on top of a page: a dialog, a drop-down menu, a bottom sheet.
 * The app tells it once, at start, what to run at the top of each such window's content (the
 * app's dark mode draws the window dark from there); by default nothing.
 */
object BoltWindow {
    var opened: @Composable () -> Unit = {}

    /** Called first thing inside each window's content. */
    @Composable
    fun Opened() = opened()
}
