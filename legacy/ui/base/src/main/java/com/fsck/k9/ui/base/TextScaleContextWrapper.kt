package com.fsck.k9.ui.base

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration

/**
 * In combination with `AppCompatActivity` this scales the font size in the configuration, on top of the system's own
 * font size. Views and Compose both read sp sizes through it, so every screen grows together.
 */
internal class TextScaleContextWrapper(
    baseContext: Context,
    private val textScale: Float,
) : ContextWrapper(baseContext) {
    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        // From the base context each time, so a configuration that passes through here twice is not scaled twice.
        overrideConfiguration.fontScale = baseContext.resources.configuration.fontScale * textScale
        return super.createConfigurationContext(overrideConfiguration)
    }
}
