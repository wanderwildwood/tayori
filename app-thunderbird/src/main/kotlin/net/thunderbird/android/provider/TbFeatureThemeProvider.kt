package net.thunderbird.android.provider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalCursorBlinkEnabled
import net.thunderbird.components.ui.bolt.theme.thunderbird.ThunderbirdBoltTheme
import net.thunderbird.core.ui.theme.api.FeatureThemeProvider

internal class TbFeatureThemeProvider : FeatureThemeProvider {
    @Composable
    override fun WithTheme(content: @Composable () -> Unit) {
        // A steady cursor: a blinking one repaints the e-ink panel twice a second.
        CompositionLocalProvider(LocalCursorBlinkEnabled provides false) {
            ThunderbirdBoltTheme {
                content()
            }
        }
    }

    @Composable
    override fun WithTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalCursorBlinkEnabled provides false) {
            ThunderbirdBoltTheme(darkTheme = darkTheme) {
                content()
            }
        }
    }
}
