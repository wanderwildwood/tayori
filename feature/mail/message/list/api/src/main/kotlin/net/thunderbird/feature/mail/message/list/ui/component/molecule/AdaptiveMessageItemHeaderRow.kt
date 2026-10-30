package net.thunderbird.feature.mail.message.list.ui.component.molecule

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.thunderbird.components.ui.bolt.atom.text.TextTitleSmall
import net.thunderbird.components.ui.bolt.common.window.WindowWidthSizeClass
import net.thunderbird.components.ui.bolt.common.window.calculateWindowSizeInfo
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import net.thunderbird.feature.mail.message.list.ui.component.config.MessageItemAccountIndicator
import net.thunderbird.feature.mail.message.list.ui.component.config.MessageItemConfiguration

/**
 * Renders an adaptive header row for a message item that adjusts its layout based
 * on screen size.
 *
 * The function determines the appropriate layout by checking the current window
 * size class and renders either a [HeaderRowSmall] or [HeaderRow] accordingly.
 *
 * @param configuration The message item configuration containing display settings,
 *  including the account indicator to be shown in the header.
 * @param receivedAt The timestamp string indicating when the message was received,
 *  displayed at the end of the header row.
 * @param firstLine A composable lambda that provides the custom content to be displayed
 *  as the first line of the header, typically containing sender information.
 * @param modifier Optional modifier to be applied to the header row container.
 */
@Composable
internal fun AdaptiveMessageItemHeaderRow(
    configuration: MessageItemConfiguration,
    receivedAt: String,
    firstLine: @Composable (() -> Unit),
    modifier: Modifier = Modifier,
) {
    val windowSizeInfo = calculateWindowSizeInfo()
    val isSmallScreen = windowSizeInfo.sizeClass.widthSizeClass == WindowWidthSizeClass.Small

    val headerRowContent: @Composable ((RowScope) -> Unit) = rememberAdaptiveHeaderRowContent(
        isSmallScreen = isSmallScreen,
        accountIndicator = configuration.accountIndicator,
        receivedAt = receivedAt,
        firstLine = firstLine,
    )
    if (isSmallScreen) {
        HeaderRowSmall(modifier, headerRowContent = headerRowContent)
    } else {
        HeaderRow(modifier, headerRowContent = headerRowContent)
    }
}

@Composable
private fun HeaderRow(
    modifier: Modifier = Modifier,
    headerRowContent: @Composable ((RowScope) -> Unit),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(BoltTheme.spacings.default),
        modifier = modifier
            .defaultMinSize(minHeight = AccountIndicatorIcon.ACCOUNT_INDICATOR_DEFAULT_HEIGHT)
            .fillMaxWidth()
            .width(intrinsicSize = IntrinsicSize.Max),
    ) {
        headerRowContent(this)
    }
}

@Composable
private fun HeaderRowSmall(
    modifier: Modifier = Modifier,
    headerRowContent: @Composable ((RowScope) -> Unit),
) {
    FlowRow(
        verticalArrangement = Arrangement.spacedBy(BoltTheme.spacings.quarter),
        horizontalArrangement = Arrangement.Start,
        maxLines = 2,
        modifier = modifier.defaultMinSize(minHeight = AccountIndicatorIcon.ACCOUNT_INDICATOR_DEFAULT_HEIGHT),
    ) {
        headerRowContent(this)
    }
}

@Composable
private fun rememberAdaptiveHeaderRowContent(
    isSmallScreen: Boolean,
    accountIndicator: MessageItemAccountIndicator?,
    receivedAt: String,
    firstLine: @Composable (() -> Unit),
): @Composable ((RowScope) -> Unit) = remember(isSmallScreen, accountIndicator, receivedAt, firstLine) {
    movableContentOf { scope ->
        with(scope) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(if (isSmallScreen) Modifier.fillMaxWidth() else Modifier.weight(1f))
                    .defaultMinSize(minHeight = AccountIndicatorIcon.ACCOUNT_INDICATOR_DEFAULT_HEIGHT),
            ) {
                firstLine()
            }
            // The account's name in words, in ink, beside the date: only shown when there is more than one.
            if (accountIndicator != null) {
                AccountIndicatorInk(accountIndicator, Modifier.align(Alignment.CenterVertically))
            }
            TextTitleSmall(
                text = receivedAt,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

/**
 * Which account a row belongs to, in ink: the panel shows every account colour as the same grey,
 * so the account's name is printed in a thin black box instead, or a plain black bar without one.
 */
@Composable
private fun AccountIndicatorInk(accountIndicator: MessageItemAccountIndicator, modifier: Modifier = Modifier) {
    val label = accountIndicator.label
    if (label.isNullOrEmpty()) {
        AccountIndicatorIcon(BoltTheme.colors.onSurface)
        return
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(end = 0.dp)
            .border(width = 1.dp, color = BoltTheme.colors.onSurface)
            .padding(horizontal = 4.dp),
    ) {
        TextTitleSmall(
            text = label,
            maxLines = 1,
            color = BoltTheme.colors.onSurface,
        )
    }
}
