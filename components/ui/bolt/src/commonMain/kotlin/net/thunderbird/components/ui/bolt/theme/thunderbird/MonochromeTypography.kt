package net.thunderbird.components.ui.bolt.theme.thunderbird

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import net.thunderbird.components.ui.bolt.resources.Res
import net.thunderbird.components.ui.bolt.resources.lato_bold
import net.thunderbird.components.ui.bolt.resources.lato_bold_italic
import net.thunderbird.components.ui.bolt.resources.lato_italic
import net.thunderbird.components.ui.bolt.resources.lato_regular
import net.thunderbird.components.ui.bolt.theme.ThemeTypography
import org.jetbrains.compose.resources.Font

/**
 * Mudita Mindful Design's type: Lato, on MMD's own scale (28 / 24 / 20 / 18 / 16 / 15 / 14),
 * with 14sp as the floor. MMD chose Lato because the E Ink panel loses the thin end of every
 * stroke, and Lato's open apertures survive that at arm's length.
 *
 * A composable rather than a value, because a Compose Multiplatform resource font can only be
 * loaded inside composition.
 */
@Suppress("MagicNumber")
@Composable
internal fun monochromeTypography(): ThemeTypography {
    val lato = FontFamily(
        Font(Res.font.lato_regular, FontWeight.Normal),
        Font(Res.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.lato_bold, FontWeight.Bold),
        Font(Res.font.lato_bold_italic, FontWeight.Bold, FontStyle.Italic),
    )
    fun style(size: Int, line: Int, weight: FontWeight = FontWeight.Medium): TextStyle = TextStyle(
        fontFamily = lato,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = TextUnit.Unspecified,
    )
    return ThemeTypography(
        displayLarge = style(40, 48),
        displayMedium = style(34, 42),
        displaySmall = style(30, 38),
        headlineLarge = style(28, 36),
        headlineMedium = style(26, 34),
        headlineSmall = style(24, 32),
        titleLarge = style(24, 30),
        titleMedium = style(20, 26),
        titleSmall = style(16, 22),
        bodyLarge = style(20, 26),
        bodyMedium = style(18, 24),
        bodySmall = style(15, 20),
        labelLarge = style(18, 24),
        labelMedium = style(15, 20),
        labelSmall = style(14, 18),
    )
}
