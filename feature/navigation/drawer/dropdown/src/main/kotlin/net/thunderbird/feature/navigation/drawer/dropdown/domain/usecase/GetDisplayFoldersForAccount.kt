package net.thunderbird.feature.navigation.drawer.dropdown.domain.usecase

import app.k9mail.legacy.ui.folder.DisplayFolderRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import net.thunderbird.core.android.account.LegacyAccountDto
import net.thunderbird.core.android.account.LegacyAccountDtoManager
import net.thunderbird.feature.navigation.drawer.dropdown.domain.DomainContract.UnifiedFolderRepository
import net.thunderbird.feature.navigation.drawer.dropdown.domain.DomainContract.UseCase
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.AccountHeaderDisplayFolder
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.DisplayFolder
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.MailDisplayFolder
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.UnifiedDisplayAccount
import net.thunderbird.feature.navigation.drawer.dropdown.domain.entity.UnifiedDisplayFolderType
import app.k9mail.legacy.ui.folder.DisplayFolder as LegacyDisplayFolder

internal class GetDisplayFoldersForAccount(
    private val displayFolderRepository: DisplayFolderRepository,
    private val unifiedFolderRepository: UnifiedFolderRepository,
    private val accountManager: LegacyAccountDtoManager,
) : UseCase.GetDisplayFoldersForAccount {
    override fun invoke(accountId: String): Flow<List<DisplayFolder>> {
        if (accountId == UnifiedDisplayAccount.UNIFIED_ACCOUNT_ID) {
            // The unified inbox, then every account's own folders under the account's name: with only the
            // unified inbox listed, Sent, Drafts and the rest were out of reach from here.
            return combine(
                unifiedFolderRepository.getUnifiedDisplayFolderFlow(UnifiedDisplayFolderType.INBOX),
                getAllAccountFoldersFlow(),
            ) { displayUnifiedFolder, accountFolders ->
                listOf(displayUnifiedFolder) + accountFolders
            }
        } else {
            return displayFolderRepository.getDisplayFoldersFlow(accountId).map { displayFolders ->
                displayFolders.map { it.toMailDisplayFolder(accountId) }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getAllAccountFoldersFlow(): Flow<List<DisplayFolder>> {
        return accountManager.getAccountsFlow().flatMapLatest { accounts ->
            if (accounts.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(accounts.map { getAccountSectionFlow(it) }) { sections -> sections.flatMap { it } }
            }
        }
    }

    private fun getAccountSectionFlow(account: LegacyAccountDto): Flow<List<DisplayFolder>> {
        val header = AccountHeaderDisplayFolder(accountId = account.uuid, name = account.displayName)
        return displayFolderRepository.getDisplayFoldersFlow(account, includeHiddenFolders = false)
            .map { displayFolders -> listOf(header) + displayFolders.map { it.toMailDisplayFolder(account.uuid) } }
    }

    private fun LegacyDisplayFolder.toMailDisplayFolder(accountId: String): MailDisplayFolder {
        return MailDisplayFolder(
            accountId = accountId,
            folder = folder,
            isInTopGroup = isInTopGroup,
            unreadMessageCount = unreadMessageCount,
            starredMessageCount = starredMessageCount,
            pathDelimiter = pathDelimiter,
        )
    }
}
