package com.fsck.k9.ui.settings

import androidx.compose.runtime.Composable
import app.k9mail.feature.account.common.ui.house.MailboxAboutDialog

/**
 * What this is, what it sends where, whose work it is, and the llama: the one shared About
 * dialog (see MailboxAboutDialog), the same as the one account setup shows before any account.
 */
@Composable
internal fun AboutDialog(version: String, onDismiss: () -> Unit) {
    MailboxAboutDialog(version = version, onDismiss = onDismiss)
}
