package app.k9mail.feature.account.setup.ui.autodiscovery

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.k9mail.feature.account.common.ui.licences.LicencesDialog
import app.k9mail.feature.account.common.ui.link.openLink
import app.k9mail.feature.account.setup.R
import net.thunderbird.components.ui.bolt.atom.Surface
import net.thunderbird.components.ui.bolt.atom.button.ButtonOutlined
import net.thunderbird.components.ui.bolt.atom.text.TextBodyLarge
import net.thunderbird.components.ui.bolt.atom.text.TextLabelSmall
import net.thunderbird.components.ui.bolt.theme.BoltTheme

/**
 * About, reachable before any account exists: what the app is, its licence, where the source
 * is, what it sends where, and the terms. The same facts as the About inside Settings.
 */
@Composable
internal fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
    }
    val sourceUrl = stringResource(R.string.account_setup_about_source_url)
    var showLicences by remember { mutableStateOf(false) }
    if (showLicences) {
        LicencesDialog(onDismiss = { showLicences = false })
        return
    }

    EInkDialog(onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
        ) {
            TextBodyLarge(
                text = stringResource(R.string.account_setup_about_title, version),
                modifier = Modifier.padding(bottom = 14.dp),
            )

            Heading(R.string.account_setup_about_licence_heading)
            TextLabelSmall(text = stringResource(R.string.account_setup_about_licence))
            Spacer(Modifier.height(14.dp))

            Heading(R.string.account_setup_about_source_heading)
            TextLabelSmall(
                text = sourceUrl,
                modifier = Modifier
                    .clickable {
                        openLink(context, sourceUrl, context.getString(R.string.account_setup_about_no_browser_address, sourceUrl))
                    }
                    .heightIn(min = 48.dp),
            )

            TextLabelSmall(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(stringResource(R.string.account_setup_about_licences))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLicences = true }
                    .heightIn(min = 48.dp),
            )

            Heading(R.string.account_setup_about_privacy_heading)
            TextLabelSmall(text = stringResource(R.string.account_setup_about_privacy))
            Spacer(Modifier.height(14.dp))

            Heading(R.string.account_setup_about_terms_heading)
            TextLabelSmall(text = stringResource(R.string.account_setup_about_terms))
        }

        Spacer(Modifier.height(18.dp))
        ButtonOutlined(
            text = stringResource(R.string.account_setup_about_close),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        )
    }
}

@Composable
private fun Heading(resId: Int) {
    TextLabelSmall(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(stringResource(resId)) }
        },
        modifier = Modifier.padding(bottom = 2.dp),
    )
}

/** A dialog with no dimmed backdrop and a 2dp rim: the house dialog. */
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
