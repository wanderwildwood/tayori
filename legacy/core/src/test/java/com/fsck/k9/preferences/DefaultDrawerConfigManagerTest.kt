package com.fsck.k9.preferences

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import net.thunderbird.core.preference.display.inboxSettings.DisplayInboxSettings
import net.thunderbird.core.preference.display.inboxSettings.DisplayInboxSettingsPreferenceManager
import net.thunderbird.core.preference.display.visualSettings.DisplayVisualSettings
import net.thunderbird.core.preference.display.visualSettings.DisplayVisualSettingsPreferenceManager
import org.junit.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DefaultDrawerConfigManagerTest {
    private val inboxSettings = MutableStateFlow(DisplayInboxSettings(isShowUnifiedInbox = false))
    private val visualSettings = MutableStateFlow(DisplayVisualSettings())

    private val inboxManager = mock<DisplayInboxSettingsPreferenceManager> {
        on { getConfigFlow() } doReturn inboxSettings
        on { getConfig() } doAnswer { inboxSettings.value }
    }
    private val visualManager = mock<DisplayVisualSettingsPreferenceManager> {
        on { getConfigFlow() } doReturn visualSettings
        on { getConfig() } doAnswer { visualSettings.value }
    }

    @Test
    fun `an open drawer hears the unified inbox being turned on and off`() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Default + Job())
        val manager = DefaultDrawerConfigManager(scope, inboxManager, visualManager)

        assertThat(manager.getConfigFlow().first().showUnifiedFolders).isEqualTo(false)

        inboxSettings.value = inboxSettings.value.copy(isShowUnifiedInbox = true)
        val turnedOn = withTimeout(5_000) { manager.getConfigFlow().first { it.showUnifiedFolders } }
        assertThat(turnedOn.showUnifiedFolders).isEqualTo(true)

        inboxSettings.value = inboxSettings.value.copy(isShowUnifiedInbox = false)
        val turnedOff = withTimeout(5_000) { manager.getConfigFlow().first { !it.showUnifiedFolders } }
        assertThat(turnedOff.showUnifiedFolders).isEqualTo(false)

        scope.cancel()
    }

    @Test
    fun `the config read before anyone listens is the saved one`() {
        inboxSettings.value = DisplayInboxSettings(isShowUnifiedInbox = true, isShowStarredCount = true)
        val scope = CoroutineScope(Dispatchers.Default + Job())

        val config = DefaultDrawerConfigManager(scope, inboxManager, visualManager).getConfig()

        assertThat(config.showUnifiedFolders).isEqualTo(true)
        assertThat(config.showStarredCount).isEqualTo(true)
        scope.cancel()
    }
}
