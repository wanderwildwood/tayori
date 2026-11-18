package com.fsck.k9.preferences

/**
 * Mailbox's own settings that live outside the preference storage, each in a file of its own
 * (dark mode, turn with the phone, the unread count on the lock screen), carried in a settings
 * export as general settings. The app hands over how to read and write them when it starts;
 * until then, and in tests, there are none.
 */
object ForkSettings {
    const val DARK_MODE = "tayoriDarkMode"
    const val TURN_WITH_PHONE = "tayoriTurnWithPhone"
    const val LOCK_SCREEN_UNREAD = "tayoriLockScreenUnread"

    val KEYS = setOf(DARK_MODE, TURN_WITH_PHONE, LOCK_SCREEN_UNREAD)

    /** The text size, Mailbox's own too but kept in the preference storage. */
    const val TEXT_SCALE = "textScalePercent"

    /**
     * Every general setting Mailbox added. They came in under upstream's version 111, so a file
     * from before (an older Mailbox, K-9 or Thunderbird) carries none of them, and upgrading it
     * must not fill them in with their defaults over what is set now.
     */
    val ADDED = KEYS + TEXT_SCALE

    /** Their values now, as "true" or "false" by key. */
    @Volatile
    var read: () -> Map<String, String> = { emptyMap() }

    /** Sets those of them an imported file carries. */
    @Volatile
    var write: (Map<String, String>) -> Unit = {}
}
