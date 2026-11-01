package com.fsck.k9.ui.settings

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.preference.Preference

/**
 * A settings row for something that cannot be undone. A tap changes its words to [armedTitle];
 * a second tap within six seconds runs [onConfirmed]; otherwise it goes back to [restingTitle].
 */
class ArmedPreference(
    context: Context,
    private val restingTitle: CharSequence,
    private val armedTitle: CharSequence,
    private val onConfirmed: () -> Unit,
) : Preference(context) {
    private val action = ArmedAction()
    private val handler = Handler(Looper.getMainLooper())
    private val disarm = Runnable {
        action.disarm()
        showResting()
    }

    init {
        isIconSpaceReserved = false
        showResting()
        setOnPreferenceClickListener {
            handler.removeCallbacks(disarm)
            if (action.tap(SystemClock.elapsedRealtime())) {
                showResting()
                onConfirmed()
            } else {
                title = SpannableString(armedTitle).apply {
                    setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                handler.postDelayed(disarm, ArmedAction.ARM_WINDOW_MILLIS)
            }
            true
        }
    }

    private fun showResting() {
        title = restingTitle
    }

    override fun onDetached() {
        handler.removeCallbacks(disarm)
        action.disarm()
        super.onDetached()
    }
}
