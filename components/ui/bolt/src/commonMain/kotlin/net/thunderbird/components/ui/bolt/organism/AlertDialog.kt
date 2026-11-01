package net.thunderbird.components.ui.bolt.organism

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import net.thunderbird.components.ui.bolt.PreviewWithTheme
import net.thunderbird.components.ui.bolt.atom.button.ButtonOutlined
import net.thunderbird.components.ui.bolt.atom.icon.Icon
import net.thunderbird.components.ui.bolt.atom.icon.Icons
import net.thunderbird.components.ui.bolt.atom.text.TextBodyMedium
import net.thunderbird.components.ui.bolt.atom.text.TextHeadlineSmall
import net.thunderbird.components.ui.bolt.theme.BoltTheme

@Composable
fun AlertDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissText: String? = null,
    onDismissClick: () -> Unit = {},
    confirmButtonEnabled: Boolean = true,
) {
    AlertDialog(
        title = title,
        icon = icon,
        confirmText = confirmText,
        onConfirmClick = onConfirmClick,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        dismissText = dismissText,
        onDismissClick = onDismissClick,
        confirmButtonEnabled = confirmButtonEnabled,
    ) {
        TextBodyMedium(text = text)
    }
}

@Composable
fun AlertDialog(
    title: String,
    confirmText: String,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    dismissText: String? = null,
    onDismissClick: () -> Unit = {},
    confirmButtonEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    BasicDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        headline = {
            if (icon != null) {
                Icon(imageVector = icon, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            TextHeadlineSmall(
                text = title,
                textAlign = if (icon == null) TextAlign.Start else TextAlign.Center,
            )
        },
        content = content,
        buttons = {
            if (dismissText != null) {
                ButtonOutlined(
                    text = dismissText,
                    onClick = onDismissClick,
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                Spacer(modifier = Modifier.width(BoltTheme.spacings.double))
            }
            ButtonOutlined(
                text = confirmText,
                onClick = onConfirmClick,
                enabled = confirmButtonEnabled,
                modifier = Modifier.heightIn(min = 48.dp),
            )
        },
    )
}

@Composable
@Preview(showBackground = true)
internal fun AlertDialogPreview() {
    PreviewWithTheme {
        AlertDialog(
            title = "Title",
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            confirmText = "Accept",
            onConfirmClick = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun AlertDialogWithIconPreview() {
    PreviewWithTheme {
        AlertDialog(
            icon = Icons.Outlined.Info,
            title = "Title",
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            confirmText = "Accept",
            onConfirmClick = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun AlertDialogWithCancelPreview() {
    PreviewWithTheme {
        AlertDialog(
            icon = Icons.Outlined.Info,
            title = "Title",
            text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            confirmText = "Accept",
            dismissText = "Cancel",
            onConfirmClick = {},
            onDismissRequest = {},
        )
    }
}

@Composable
@Preview(showBackground = true)
internal fun AlertDialogWithCustomContentPreview() {
    PreviewWithTheme {
        AlertDialog(
            icon = Icons.Outlined.Info,
            title = "Title",
            confirmText = "Accept",
            dismissText = "Cancel",
            onConfirmClick = {},
            onDismissRequest = {},
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(BoltTheme.spacings.double),
            ) {
                TextBodyMedium("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                TextBodyMedium("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
            }
        }
    }
}
