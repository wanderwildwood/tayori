package com.wanderwildwood.tayori

import android.content.Context
import app.k9mail.feature.account.common.ui.house.DarkMode
import com.fsck.k9.preferences.ForkSettings
import com.wanderwildwood.tayori.glance.LockScreen

/**
 * Hands the settings export Mailbox's own settings, which live in files of their own: dark mode,
 * turning with the phone, and the unread count on the lock screen. Exported with the general
 * settings, and set again from a file that carries them.
 */
object OwnSettings {
    fun install(context: Context) {
        val app = context.applicationContext
        ForkSettings.read = {
            mapOf(
                ForkSettings.DARK_MODE to DarkMode.on.toString(),
                ForkSettings.TURN_WITH_PHONE to TurnSideways.on(app).toString(),
                ForkSettings.LOCK_SCREEN_UNREAD to LockScreen.on(app).toString(),
            )
        }
        ForkSettings.write = { values ->
            values[ForkSettings.DARK_MODE]?.toBooleanStrictOrNull()?.let { if (it != DarkMode.on) DarkMode.set(app, it) }
            values[ForkSettings.TURN_WITH_PHONE]?.toBooleanStrictOrNull()?.let { TurnSideways.set(app, it) }
            values[ForkSettings.LOCK_SCREEN_UNREAD]?.toBooleanStrictOrNull()?.let { LockScreen.set(app, it) }
        }
    }
}
