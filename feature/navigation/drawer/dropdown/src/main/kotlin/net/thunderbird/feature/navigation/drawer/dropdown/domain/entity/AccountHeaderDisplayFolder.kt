package net.thunderbird.feature.navigation.drawer.dropdown.domain.entity

import net.thunderbird.feature.mail.folder.api.FOLDER_DEFAULT_PATH_DELIMITER
import net.thunderbird.feature.mail.folder.api.FolderPathDelimiter

/**
 * An account's name, heading that account's folders in the unified folder list. It is a label, not a folder:
 * nothing opens when it is tapped.
 */
internal data class AccountHeaderDisplayFolder(
    val accountId: String,
    val name: String,
) : DisplayFolder {
    override val id: String = "account_header_$accountId"
    override val unreadMessageCount: Int = 0
    override val starredMessageCount: Int = 0
    override val pathDelimiter: FolderPathDelimiter = FOLDER_DEFAULT_PATH_DELIMITER
}
