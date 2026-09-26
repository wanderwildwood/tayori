package net.thunderbird.core.ui.animation.manager

import net.thunderbird.core.preference.display.visualSettings.DisplayVisualSettingsPreferenceManager

interface AnimationManager {
    fun shouldShowAnimations(): Boolean
}

class DefaultAnimationManager(
    private val visualSettingsPreferenceManager: DisplayVisualSettingsPreferenceManager,
) : AnimationManager {
    // Nothing animates on an E Ink panel: motion is a smear and a battery cost.
    override fun shouldShowAnimations(): Boolean = false
}
