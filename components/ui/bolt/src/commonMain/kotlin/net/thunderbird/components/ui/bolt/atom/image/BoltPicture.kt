package net.thunderbird.components.ui.bolt.atom.image

import androidx.compose.ui.Modifier

/**
 * A picture the design system shows ([RemoteImage]: attachment thumbnails, avatars). The app tells
 * it once, at start, what to add to each picture (the app's dark mode keeps a picture's own colors
 * from there); by default nothing.
 */
object BoltPicture {
    var kept: Modifier = Modifier
}
