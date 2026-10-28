package net.thunderbird.feature.navigation.drawer.dropdown.ui.setting

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.thunderbird.components.ui.bolt.atom.icon.Icon
import net.thunderbird.components.ui.bolt.common.window.WindowHeightSizeClass
import net.thunderbird.components.ui.bolt.common.window.WindowWidthSizeClass
import net.thunderbird.components.ui.bolt.common.window.calculateWindowSizeInfo
import net.thunderbird.components.ui.bolt.organism.drawer.NavigationDrawerItem

@Composable
internal fun SettingListItem(
    label: String,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
) {
    // Determine whether we should hide text for settings items (phone in landscape)
    val windowSizeInfo = calculateWindowSizeInfo()
    val isLandscape = windowSizeInfo.size.width > windowSizeInfo.size.height
    // On phones in landscape, the height size class is typically Compact even if width is Medium.
    // Use height size class to better detect phone-in-landscape and hide labels accordingly.
    val isCompactHeight = windowSizeInfo.sizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val isSmallWidth = windowSizeInfo.sizeClass.widthSizeClass == WindowWidthSizeClass.Small
    val hideText = isLandscape && isCompactHeight

    val rotation: Float = if (isLoading) {
        val infinite = rememberInfiniteTransition(label = "SyncIconRotation")
        val angle by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "SyncIconAngle",
        )
        angle
    } else {
        0f
    }

    if (isSmallWidth || hideText) {
        IconTarget(
            onClick = onClick,
            icon = icon,
            label = label,
            rotation = rotation,
            modifier = modifier,
        )
    } else {
        NavigationDrawerItem(
            label = label,
            onClick = onClick,
            modifier = modifier,
            selected = false,
            icon = {
                Icon(
                    imageVector = icon,
                    modifier = Modifier
                        .size(ICON_SIZE)
                        .then(if (rotation != 0f) Modifier.rotate(rotation) else Modifier),
                    contentDescription = null,
                )
            },
        )
    }
}

// The icons as the other apps draw theirs (the cog among them): 22 dp inside a 48 dp target.
private val ICON_SIZE = 22.dp
private val TARGET_SIZE = 48.dp

/** An icon alone, with its name kept for TalkBack, where there is no room for the words. */
@Composable
private fun IconTarget(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(TARGET_SIZE)
            .clickable(onClick = onClick, role = Role.Button, onClickLabel = null)
            .semantics { contentDescription = label },
    ) {
        Icon(
            imageVector = icon,
            modifier = Modifier
                .size(ICON_SIZE)
                .then(if (rotation != 0f) Modifier.rotate(rotation) else Modifier),
            contentDescription = null,
        )
    }
}
