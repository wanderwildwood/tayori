package net.thunderbird.components.ui.bolt.theme.thunderbird

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import net.thunderbird.components.ui.bolt.theme.ThemeColorSchemeVariants
import net.thunderbird.components.ui.bolt.theme.ThemeConfig
import net.thunderbird.components.ui.bolt.theme.ThemeImageVariants
import net.thunderbird.components.ui.bolt.theme.ThemeImages
import net.thunderbird.components.ui.bolt.theme.default.defaultThemeElevations
import net.thunderbird.components.ui.bolt.theme.default.defaultThemeShapes
import net.thunderbird.components.ui.bolt.theme.default.defaultThemeSizes
import net.thunderbird.components.ui.bolt.theme.default.defaultThemeSpacings
import net.thunderbird.components.ui.bolt.resources.Res
import net.thunderbird.components.ui.bolt.resources.bolt_thunderbird_logo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThunderbirdBoltTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val images = ThemeImages(
        logo = Res.drawable.bolt_thunderbird_logo,
    )

    val themeConfig = ThemeConfig(
        colors = ThemeColorSchemeVariants(
            dark = monochromeThemeColorScheme,
            light = monochromeThemeColorScheme,
        ),
        elevations = defaultThemeElevations,
        images = ThemeImageVariants(
            light = images,
            dark = images,
        ),
        sizes = defaultThemeSizes,
        spacings = defaultThemeSpacings,
        shapes = defaultThemeShapes,
        typography = monochromeTypography(),
    )

    BoltTheme(
        themeConfig = themeConfig,
        darkTheme = darkTheme,
    ) {
        // No ripple, as MMD: on an E Ink panel an animated press is a smear, not feedback.
        CompositionLocalProvider(LocalRippleConfiguration provides null, content = content)
    }
}
