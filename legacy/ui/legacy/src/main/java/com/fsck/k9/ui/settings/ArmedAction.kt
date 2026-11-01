package com.fsck.k9.ui.settings

/**
 * The state of an armed row: the first tap arms it, a second tap within [windowMillis] confirms,
 * and after that long without one it is disarmed again, so a stray tap leaves no live trigger.
 * Time comes from the caller, so the rule can be tested without waiting.
 */
class ArmedAction(private val windowMillis: Long = ARM_WINDOW_MILLIS) {
    private var armedAt: Long? = null

    fun isArmed(now: Long): Boolean {
        val at = armedAt ?: return false
        if (now - at > windowMillis) {
            armedAt = null
            return false
        }
        return true
    }

    /** A tap. True when it confirms an armed row; false when it only armed (or re-armed) it. */
    fun tap(now: Long): Boolean {
        if (isArmed(now)) {
            armedAt = null
            return true
        }
        armedAt = now
        return false
    }

    fun disarm() {
        armedAt = null
    }

    companion object {
        const val ARM_WINDOW_MILLIS = 6_000L
    }
}
