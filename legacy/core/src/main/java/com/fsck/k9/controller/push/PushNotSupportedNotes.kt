package com.fsck.k9.controller.push

import android.content.Context
import android.content.SharedPreferences

/**
 * Which accounts' servers said no to push. When an IMAP server does not offer IDLE, push is
 * switched off for every folder of the account (AccountBackendPusherCallback), and the folder's
 * Enable push switch would turn itself off without a word. This remembers that it happened, so
 * the folder's settings can say why, until push is switched on there again to try once more.
 */
class PushNotSupportedNotes(private val preferences: SharedPreferences) {
    constructor(context: Context) : this(context.getSharedPreferences(FILE, Context.MODE_PRIVATE))

    fun mark(accountUuid: String) {
        preferences.edit().putBoolean(accountUuid, true).apply()
    }

    fun clear(accountUuid: String) {
        preferences.edit().remove(accountUuid).apply()
    }

    fun isMarked(accountUuid: String): Boolean = preferences.getBoolean(accountUuid, false)

    private companion object {
        const val FILE = "push_not_supported"
    }
}
