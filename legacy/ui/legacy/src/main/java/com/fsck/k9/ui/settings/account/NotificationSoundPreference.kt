package com.fsck.k9.ui.settings.account

import android.annotation.SuppressLint
import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.Preference
import com.takisoft.preferencex.PreferenceActivityResultListener
import com.takisoft.preferencex.PreferenceFragmentCompat

@SuppressLint("RestrictedApi")
class NotificationSoundPreference
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = TypedArrayUtils.getAttr(
        context,
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle,
    ),
    defStyleRes: Int = 0,
) : Preference(context, attrs, defStyleAttr, defStyleRes), PreferenceActivityResultListener {
    var receivedActivityResultJustNow = false

    // The sound is chosen in SoundChooser, not in an activity, so there is no result to take.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) = Unit

    fun setNotificationSound(sound: Uri?) {
        persistRingtone(sound)
        showName(sound)
    }

    /** The row names the sound it has, so the choice can be seen from the settings page. */
    private fun showName(sound: Uri?) {
        summary = if (sound == null || sound.toString().isEmpty()) {
            context.getString(com.fsck.k9.ui.R.string.sound_chooser_silent)
        } else {
            runCatching { RingtoneManager.getRingtone(context, sound)?.getTitle(context) }.getOrNull()
        }
    }

    override fun onPreferenceClick(fragment: PreferenceFragmentCompat, preference: Preference) {
        SoundChooser.show(context, RingtoneManager.TYPE_NOTIFICATION, title, getPersistedRingtone()) { uri ->
            if (callChangeListener(uri?.toString().orEmpty())) {
                receivedActivityResultJustNow = true
                persistRingtone(uri)
                showName(uri)
            }
        }
    }

    private fun getPersistedRingtone(): Uri? {
        val uriString = getPersistedString(null)?.takeIf { it.isNotEmpty() }
        return uriString?.let { Uri.parse(it) }
    }

    private fun persistRingtone(ringtoneUri: Uri?) {
        persistString(ringtoneUri?.toString().orEmpty())
    }
}
