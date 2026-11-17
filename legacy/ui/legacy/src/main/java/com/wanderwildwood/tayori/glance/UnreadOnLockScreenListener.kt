package com.wanderwildwood.tayori.glance

import android.content.Context
import app.k9mail.legacy.message.controller.SimpleMessagingListener
import com.fsck.k9.mail.Message
import net.thunderbird.core.android.account.LegacyAccountDto

/**
 * Tells Glance the unread count may have changed, on the same events that refresh the unread
 * widget, so the lock screen does not wait for Glance to ask again by itself.
 */
class UnreadOnLockScreenListener(private val context: Context) : SimpleMessagingListener() {

    private fun changed() {
        if (LockScreen.on(context)) runCatching { GlanceProvider.changed(context) }
    }

    override fun synchronizeMailboxRemovedMessage(
        account: LegacyAccountDto,
        folderServerId: String,
        messageServerId: String,
    ) {
        changed()
    }

    override fun synchronizeMailboxNewMessage(account: LegacyAccountDto, folderServerId: String, message: Message) {
        changed()
    }

    override fun folderStatusChanged(account: LegacyAccountDto, folderId: Long) {
        changed()
    }
}
