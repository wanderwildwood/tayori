package net.thunderbird.feature.changelog.internal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.flowOf
import net.thunderbird.core.preference.GeneralSettingsManager

@Suppress("UnusedPrivateProperty")
class RecentChangesViewModel(
    private val generalSettingsManager: GeneralSettingsManager,
    private val changeLogManager: ChangeLogManager,
) : ViewModel() {
    // tayori ships no changelog, so the "what's new" hint never shows.
    val shouldShowRecentChangesHint = flowOf(false).asLiveData()

    fun onRecentChangesHintDismissed() {
        changeLogManager.writeCurrentVersion()
    }
}
