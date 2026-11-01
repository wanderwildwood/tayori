package net.thunderbird.components.ui.bolt.atom.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import androidx.compose.material3.ButtonColors as Material3ButtonColors
import androidx.compose.material3.ButtonDefaults as Material3ButtonDefaults

/**
 * Contains the default values used by buttons in the design system.
 */
data object ButtonDefaults {
    val ContentPadding = Material3ButtonDefaults.ContentPadding

    /** House buttons: black on white, a 2 dp line, 8 dp corners, at least 48 dp tall. */
    val HouseButtonShape: Shape = RoundedCornerShape(8.dp)
    val HouseButtonBorder: Dp = 2.dp
    val MinHeight: Dp = 48.dp
    private const val DisabledAlpha = 0.38f

    /**
     * Creates a [ButtonColors] that represents the default container and content
     * colours used in a text button.
     *
     * @param containerColor the container colour of this button when enabled.
     * @param contentColor the content colour of this button when enabled.
     * @param disabledContainerColor the container colour of this button when
     *  not enabled.
     * @param disabledContentColor the content colour of this button when not
     *  enabled.
     * @param iconColor the colour of the icon when enabled.
     * @param iconDisabledColor the colour of the icon when not enabled.
     */
    @Composable
    fun textButtonColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = BoltTheme.colors.onSurface,
        disabledContainerColor: Color = Color.Unspecified,
        disabledContentColor: Color = BoltTheme.colors.onSurface.copy(alpha = DisabledAlpha),
        iconColor: Color = contentColor,
        iconDisabledColor: Color = disabledContentColor,
    ): ButtonColors = Material3ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    ).toButtonColors(iconColor, iconDisabledColor)

    /**
     * Creates a [ButtonColors] instance representing the default colours used
     * in a filled button.
     *
     * @param containerColor the container colour of this button when enabled.
     * @param contentColor the content colour of this button when enabled.
     * @param disabledContainerColor the container colour of this button when
     *  not enabled.
     * @param disabledContentColor the content colour of this button when not
     *  enabled.
     * @param iconColor the colour of the icon when enabled.
     * @param iconDisabledColor the colour of the icon when not enabled.
     */
    @Composable
    fun filledButtonColors(
        containerColor: Color = BoltTheme.colors.onSurface,
        contentColor: Color = BoltTheme.colors.surface,
        disabledContainerColor: Color = BoltTheme.colors.onSurface.copy(alpha = DisabledAlpha),
        disabledContentColor: Color = BoltTheme.colors.surface,
        iconColor: Color = contentColor,
        iconDisabledColor: Color = disabledContentColor,
    ): ButtonColors = Material3ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    ).toButtonColors(iconColor, iconDisabledColor)

    /**
     * Creates a [ButtonColors] instance representing the default colours used
     * in an outlined button.
     *
     * @param containerColor the container colour of this button when enabled.
     * @param contentColor the content colour of this button when enabled.
     * @param disabledContainerColor the container colour of this button when
     *  not enabled.
     * @param disabledContentColor the content colour of this button when not
     *  enabled.
     * @param iconColor the colour of the icon when enabled.
     * @param iconDisabledColor the colour of the icon when not enabled.
     */
    @Composable
    fun outlinedButtonColors(
        containerColor: Color = BoltTheme.colors.surface,
        contentColor: Color = BoltTheme.colors.onSurface,
        disabledContainerColor: Color = BoltTheme.colors.surface,
        disabledContentColor: Color = BoltTheme.colors.onSurface.copy(alpha = DisabledAlpha),
        iconColor: Color = contentColor,
        iconDisabledColor: Color = disabledContentColor,
    ): ButtonColors = Material3ButtonDefaults.outlinedButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    ).toButtonColors(iconColor, iconDisabledColor)

    /**
     * Creates a [ButtonShape] that represents the default shape and border
     * used in a filled button.
     *
     * @param shape the [Shape] of the button.
     */
    @Composable
    fun filledShape(
        shape: Shape = HouseButtonShape,
    ): ButtonShape = ButtonShape(shape = shape)

    /**
     * Creates a [ButtonShape] that represents the default shape and border
     * used in an outlined button.
     *
     * @param shape the [Shape] of the button.
     * @param border the [ButtonBorderStroke] to be applied to the button.
     */
    @Composable
    fun outlinedShape(
        enabled: Boolean = true,
        shape: Shape = HouseButtonShape,
        border: ButtonBorderStroke = outlinedButtonBorder(enabled),
    ): ButtonShape = ButtonShape(shape = shape, borderStroke = border)

    /**
     * Creates a [ButtonBorderStroke] used for the border of an outlined button.
     *
     * @param enabled whether the button is enabled.
     * @param color the colour of the border when enabled. If [Color.Unspecified], the default
     * Material 3 colour will be used.
     * @param disabledColor the colour of the border when the button is not enabled.
     */
    @Composable
    fun outlinedButtonBorder(
        enabled: Boolean = true,
        color: Color = Color.Unspecified,
        disabledColor: Color = color.copy(alpha = 0.1f),
    ): ButtonBorderStroke {
        val line = if (color != Color.Unspecified) color else BoltTheme.colors.onSurface
        val off = if (color != Color.Unspecified) disabledColor else line.copy(alpha = DisabledAlpha)
        return ButtonBorderStroke(width = HouseButtonBorder, brush = SolidColor(if (enabled) line else off))
    }
}

/**
 * Represents the container, content, and icon colours used in a button in
 * different states.
 *
 * @param containerColor the background colour of the button when enabled
 * @param contentColor the colour of the button's text content when enabled
 * @param disabledContainerColor the background colour of the button when
 *  disabled
 * @param disabledContentColor the colour of the button's text content when
 *  disabled
 * @param iconColor the colour of icons within the button when enabled, defaults
 *  to contentColor
 * @param iconDisabledColor the colour of icons within the button when disabled,
 *  defaults to disabledContentColor
 * @constructor creates an instance with the specified colours for all button states
 */
data class ButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
    val iconColor: Color = contentColor,
    val iconDisabledColor: Color = disabledContentColor,
)

private fun Material3ButtonColors.toButtonColors(
    iconColor: Color = Color.Unspecified,
    iconDisabledColor: Color = Color.Unspecified,
): ButtonColors = ButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
    iconColor = iconColor,
    iconDisabledColor = iconDisabledColor,
)

internal fun ButtonColors.toMaterial3Colors(): Material3ButtonColors = Material3ButtonColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
)

/**
 * Represents the shape and optional border stroke of a button.
 *
 * @property shape the [Shape] used for the button.
 * @property borderStroke the [ButtonBorderStroke] applied to the button,
 *  or `null` if no border is used.
 */
data class ButtonShape(
    val shape: Shape,
    val borderStroke: ButtonBorderStroke? = null,
)

/**
 * Represents the border stroke applied to a button.
 *
 * @param width the thickness of the border.
 * @param brush the [Brush] used to paint the border.
 */
data class ButtonBorderStroke(
    val width: Dp,
    val brush: Brush,
)

private fun BorderStroke.toButtonBorderStroke(): ButtonBorderStroke = ButtonBorderStroke(
    width = width,
    brush = brush,
)

internal fun ButtonBorderStroke.toMaterial3BorderStroke(): BorderStroke = BorderStroke(
    width = width,
    brush = brush,
)
