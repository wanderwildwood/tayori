package com.fsck.k9.ui.settings

import androidx.preference.PreferenceGroup

/**
 * Settings rows carry no icons, so no row keeps an empty column where one would have gone.
 */
internal fun PreferenceGroup.withoutIconSpace() {
    isIconSpaceReserved = false
    for (index in 0 until preferenceCount) {
        val preference = getPreference(index)
        preference.isIconSpaceReserved = false
        if (preference is PreferenceGroup) preference.withoutIconSpace()
    }
}
