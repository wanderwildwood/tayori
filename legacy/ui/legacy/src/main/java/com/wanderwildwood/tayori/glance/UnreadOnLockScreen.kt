package com.wanderwildwood.tayori.glance

import android.content.Context
import app.k9mail.legacy.message.controller.MessageCountsProvider
import com.fsck.k9.CoreResourceProvider
import com.fsck.k9.ui.R
import net.thunderbird.feature.search.legacy.SearchAccount
import org.koin.core.context.GlobalContext

/**
 * How much mail is unread, for the lock screen: the unified inbox's count, the same one the
 * unread widget shows. Nothing when there is none.
 */
class UnreadOnLockScreen : GlanceProvider() {

    override fun enabled(context: Context): Boolean = LockScreen.on(context)

    override fun lines(context: Context): List<Line> {
        val koin = GlobalContext.get()
        val resources = koin.get<CoreResourceProvider>()
        val unified = SearchAccount.createUnifiedFoldersSearch(
            title = resources.searchUnifiedFoldersTitle(),
            detail = resources.searchUnifiedFoldersDetail(),
        )
        val unread = koin.get<MessageCountsProvider>().getMessageCounts(unified).unread
        if (unread <= 0) return emptyList()
        return listOf(Line(context.resources.getQuantityString(R.plurals.lock_screen_unread_email, unread, unread)))
    }
}

/** Email's own switch for the lock screen. */
object LockScreen {
    private const val FILE = "lock_screen"
    private const val ON = "on"

    fun on(context: Context): Boolean =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(ON, true)

    fun set(context: Context, on: Boolean) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(ON, on).apply()
        GlanceProvider.changed(context)
    }
}
