package net.thunderbird.components.ui.bolt.organism

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * The house dialog window: no dimmed backdrop (on E Ink the scrim is a screenful of dithered grey
 * repainted twice), one width with 14 dp at each side, and no animation (the rim is drawn by the content, see BasicDialogContent). Every Compose dialog in the app goes through here; the View-based ones get the same
 * look from Theme2.EInkDialog.
 */
@Composable
fun EInkDialogWindow(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            usePlatformDefaultWidth = false,
        ),
    ) {
        DialogWindowNoDim()
        Box(
            modifier = modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth(),
        ) {
            content()
        }
    }
}

/** Takes the dimmed backdrop and the window animation off the dialog window it is called in. */
@Composable
expect fun DialogWindowNoDim()
