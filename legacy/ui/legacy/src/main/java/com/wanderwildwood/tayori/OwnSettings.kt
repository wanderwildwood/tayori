package com.wanderwildwood.tayori

import android.content.Context
import app.k9mail.feature.account.common.ui.house.DarkMode
import com.fsck.k9.preferences.ForkSettings
import com.wanderwildwood.tayori.glance.LockScreen

/**
 * Hands the settings export Mailbox's own settings, which live in files of their own: dark mode,
 * turning with the phone, and the unread count on the lock screen. Exported with the general
 * settings, and set again from a file that carries them. Dark mode goes out only while the app's
 * own switch holds: a dark that follows the phone or Home is theirs, and a file without it leaves
 * the app following them wherever it is brought back.
 */
object OwnSettings {
    fun install(context: Context) {
        val app = context.applicationContext
        ForkSettings.read = {
            buildMap {
                if (ownDarkSwitchHolds(app)) put(ForkSettings.DARK_MODE, DarkMode.on.toString())
                put(ForkSettings.TURN_WITH_PHONE, TurnSideways.on(app).toString())
                put(ForkSettings.LOCK_SCREEN_UNREAD, LockScreen.on(app).toString())
            }
        }
        ForkSettings.write = { values ->
            values[ForkSettings.DARK_MODE]?.toBooleanStrictOrNull()?.let { if (it != DarkMode.on) DarkMode.set(app, it) }
            values[ForkSettings.TURN_WITH_PHONE]?.toBooleanStrictOrNull()?.let { TurnSideways.set(app, it) }
            values[ForkSettings.LOCK_SCREEN_UNREAD]?.toBooleanStrictOrNull()?.let { LockScreen.set(app, it) }
        }
    }

    /** DarkMode's own flag ("own" in its "dark_mode" file), true while the app's switch holds. */
    private fun ownDarkSwitchHolds(context: Context): Boolean =
        context.getSharedPreferences("dark_mode", Context.MODE_PRIVATE).getBoolean("own", false)
}
