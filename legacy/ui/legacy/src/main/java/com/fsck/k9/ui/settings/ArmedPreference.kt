package com.fsck.k9.ui.settings

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.activity.ComponentActivity
import androidx.preference.Preference

/**
 * A settings row for something that cannot be undone. A tap changes its words to [armedTitle];
 * a second tap runs [onConfirmed]. There is no timer: it stays armed until the person does
 * something else (a tap anywhere else, Back, leaving the screen), and then goes back to
 * [restingTitle]. The rule itself is the shared ArmedWatch (ArmedViews.kt).
 */
class ArmedPreference(
    context: Context,
    private val restingTitle: CharSequence,
    private val armedTitle: CharSequence,
    private val onConfirmed: () -> Unit,
) : Preference(context) {
    private val disarmRow: () -> Unit = {
        armedHere = false
        showResting()
    }

    /** Only used if the row is ever shown outside an activity, where there is no ArmedWatch. */
    private var armedHere = false

    init {
        isIconSpaceReserved = false
        showResting()
        setOnPreferenceClickListener {
            val watch = watch()
            if (watch == null) {
                if (armedHere) {
                    disarmRow()
                    onConfirmed()
                } else {
                    showArmed()
                    armedHere = true
                }
            } else if (watch.isArmed(disarmRow)) {
                watch.done(disarmRow)
                showResting()
                onConfirmed()
            } else {
                showArmed()
                watch.arm(disarmRow)
            }
            true
        }
    }

    private fun showArmed() {
        title = SpannableString(armedTitle).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun showResting() {
        title = restingTitle
    }

    private fun watch(): ArmedWatch? {
        var c: Context? = context
        while (c is ContextWrapper) {
            if (c is ComponentActivity) return ArmedWatch.of(c)
            c = c.baseContext
        }
        return null
    }

    override fun onDetached() {
        watch()?.let { if (it.isArmed(disarmRow)) it.disarm() }
        armedHere = false
        super.onDetached()
    }
}
