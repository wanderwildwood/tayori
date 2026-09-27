package com.fsck.k9.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.fsck.k9.ui.R
import net.thunderbird.components.ui.bolt.atom.Surface
import net.thunderbird.components.ui.bolt.atom.button.ButtonOutlined
import net.thunderbird.components.ui.bolt.atom.text.TextBodyLarge
import net.thunderbird.components.ui.bolt.atom.text.TextLabelSmall
import net.thunderbird.components.ui.bolt.theme.BoltTheme

/**
 * What this is, what it sends where, whose work it is, and the llama.
 *
 * The line about the network is here because a stranger cannot guess it: besides the mail
 * servers you give it, setting up an account asks the address's own domain, and Mozilla's
 * database of providers, how to reach them.
 */
@Composable
internal fun AboutDialog(version: String, onDismiss: () -> Unit) {
    EInkDialog(onDismiss = onDismiss) {
        TextBodyLarge(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                    append(stringResource(R.string.tayori_about_title, version))
                }
            },
        )

        Spacer(Modifier.height(14.dp))
        TextLabelSmall(text = stringResource(R.string.tayori_about_privacy))

        Spacer(Modifier.height(14.dp))
        TextLabelSmall(text = stringResource(R.string.tayori_about_licence))
        TextLabelSmall(text = stringResource(R.string.tayori_about_after))

        Spacer(Modifier.height(14.dp))
        TextLabelSmall(text = stringResource(R.string.app_source_url))

        Spacer(Modifier.height(14.dp))
        Llama()

        Spacer(Modifier.height(18.dp))
        ButtonOutlined(
            text = stringResource(R.string.tayori_about_close),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
    }
}

/**
 * The llama opens the donation checkout. Only the drawing and its words open it; a phone with
 * nothing registered for a web address says so rather than doing nothing.
 */
@Composable
private fun Llama() {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            // The site's address opens the site, the way the llama beside it opens its page.
            modifier = Modifier
                .clickable {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://wanderthe.dev")),
                        )
                    }.onFailure {
                        Toast.makeText(context, R.string.tayori_about_no_browser, Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 4.dp),
        ) {
            TextLabelSmall(text = "wanderthe.dev")
        }
        Spacer(Modifier.width(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    runCatching {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.funding_url))),
                        )
                    }.onFailure {
                        Toast.makeText(context, R.string.tayori_about_no_browser, Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(vertical = 4.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.llama),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(6.dp))
            TextLabelSmall(text = stringResource(R.string.tayori_about_feed_the_llamas))
        }
    }
}

/**
 * A dialog with no dimmed backdrop, a 2dp rim and one width: the house dialog, drawn here with
 * this app's own theme rather than MMD's.
 */
@Composable
private fun EInkDialog(
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        }
        Surface(
            color = BoltTheme.colors.surface,
            contentColor = BoltTheme.colors.onSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .border(2.dp, BoltTheme.colors.onSurface, RoundedCornerShape(12.dp)),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                content = content,
            )
        }
    }
}
