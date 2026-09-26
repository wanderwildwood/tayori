package net.thunderbird.components.ui.bolt.theme.thunderbird

import androidx.compose.ui.graphics.Color
import net.thunderbird.components.ui.bolt.theme.ThemeColorScheme

/**
 * Mudita Mindful Design's black and white, for an E Ink panel: the same values as the shop's
 * Monochrome.kt. Every container tier is white, not grey, and the status colours (info, success,
 * warning) are black on white too, because sixteen greys cannot tell them apart anyway. It is
 * used for the light and the dark theme alike: a panel that is read outdoors has one theme.
 */
internal val monochromeThemeColorScheme = ThemeColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color.Black,
    onPrimaryContainer = Color.White,

    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color.White,
    onSecondaryContainer = Color.Black,

    tertiary = Color.Black,
    onTertiary = Color.White,
    tertiaryContainer = Color.White,
    onTertiaryContainer = Color.Black,

    error = Color.Black,
    onError = Color.White,
    errorContainer = Color.White,
    onErrorContainer = Color.Black,

    surfaceDim = Color.White,
    surface = Color.White,
    surfaceBright = Color.White,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerHighest = Color.White,

    inverseSurface = Color.Black,
    inverseOnSurface = Color.White,
    inversePrimary = Color.White,

    outline = Color.Black,
    outlineVariant = Color.Black,

    scrim = Color.Transparent,

    info = Color.Black,
    onInfo = Color.White,
    infoContainer = Color.White,
    onInfoContainer = Color.Black,

    success = Color.Black,
    onSuccess = Color.White,
    successContainer = Color.White,
    onSuccessContainer = Color.Black,

    warning = Color.Black,
    onWarning = Color.White,
    warningContainer = Color.White,
    onWarningContainer = Color.Black,
)
