package net.thunderbird.feature.navigation.drawer.dropdown.ui.folder

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.style.TextOverflow
import app.k9mail.legacy.ui.folder.FolderNameFormatter
import net.thunderbird.components.ui.bolt.atom.DividerHorizontal
import net.thunderbird.components.ui.bolt.atom.text.TextTitleSmall
import net.thunderbird.components.ui.bolt.theme.BoltTheme
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.AccountHeaderDisplayFolder
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.DisplayFolder
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.DisplayTreeFolder

@Composable
internal fun FolderList(
    rootFolder: DisplayTreeFolder,
    selectedFolder: DisplayFolder?,
    onFolderClick: (DisplayFolder) -> Unit,
    showStarredCount: Boolean,
    modifier: Modifier = Modifier,
    isExpandedInitial: Boolean = false,
) {
    val resources = LocalResources.current
    val folderNameFormatter = remember { FolderNameFormatter(resources) }
    val listState = rememberLazyListState()

    fun LazyListScope.folderItems(folders: List<DisplayTreeFolder>) {
        items(
            items = folders,
            key = { it.displayFolder?.id ?: '0' },
        ) { folder ->
            val currentDisplayFolder = folder.displayFolder
            FolderListItem(
                displayFolder = requireNotNull(currentDisplayFolder) {
                    "Null DisplayFolder for folder ${folder.displayName}"
                },
                treeFolder = folder,
                showStarredCount = showStarredCount,
                onClick = onFolderClick,
                folderNameFormatter = folderNameFormatter,
                selectedFolderId = selectedFolder?.id,
                isExpandInitial = isExpandedInitial,
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = BoltTheme.spacings.default),
    ) {
        for (child in rootFolder.children) {
            val header = child.displayFolder
            if (header is AccountHeaderDisplayFolder) {
                item(key = header.id) {
                    AccountHeader(name = header.name)
                }
                folderItems(child.children)
            } else {
                folderItems(listOf(child))
            }
        }
    }
}

/**
 * An account's name above its folders in the unified list, after a rule that closes off the section before it.
 */
@Composable
private fun AccountHeader(name: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DividerHorizontal(modifier = Modifier.padding(vertical = BoltTheme.spacings.default))
        TextTitleSmall(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = BoltTheme.spacings.oneHalf + BoltTheme.spacings.double,
                vertical = BoltTheme.spacings.default,
            ),
        )
    }
}
