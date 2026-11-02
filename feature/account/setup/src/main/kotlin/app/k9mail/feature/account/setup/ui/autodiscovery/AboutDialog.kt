package app.k9mail.feature.account.setup.ui.autodiscovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.k9mail.feature.account.common.ui.house.MailboxAboutDialog

/**
 * About, reachable before any account exists: the same shared dialog as the About inside
 * Settings (see MailboxAboutDialog), so it opens with no account and no sign-in.
 */
@Composable
internal fun AboutDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val version = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("")
    }
    MailboxAboutDialog(version = version, onDismiss = onDismiss)
}
