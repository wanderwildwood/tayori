package net.thunderbird.components.ui.bolt.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.material3.DropdownMenu as Material3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as Material3DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox as Material3ExposedDropdownMenuBox
import androidx.compose.material3.Text as Material3Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: ImmutableList<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    optionToString: (T) -> String = { it.toString() },
    anchorContent: @Composable (expanded: Boolean) -> Unit,
) {
    Material3ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (enabled) {
                onExpandedChange(expanded)
            }
        },
        modifier = modifier,
    ) {
        anchorContent(expanded)

        Material3DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            // A rim rather than a shadow: a shadow on E Ink is a band of dithered grey.
            shadowElevation = 0.dp,
            tonalElevation = 0.dp,
            border = BorderStroke(2.dp, Color.Black),
        ) {
            options.forEach { option ->
                Material3DropdownMenuItem(
                    text = {
                        Material3Text(text = optionToString(option))
                    },
                    onClick = {
                        onItemSelected(option)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}
