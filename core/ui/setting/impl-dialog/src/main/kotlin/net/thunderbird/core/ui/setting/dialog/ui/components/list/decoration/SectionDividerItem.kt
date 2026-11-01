package net.thunderbird.core.ui.setting.dialog.ui.components.list.decoration

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A gap between sections of a settings page. House style: settings pages draw no separator lines. */
@Composable
internal fun SectionDividerItem(
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.height(16.dp))
}
