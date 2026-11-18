package com.fsck.k9.preferences

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.fsck.k9.K9RobolectricTest
import com.fsck.k9.Preferences
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest
import net.thunderbird.feature.mail.folder.api.data.repository.FolderQueryRepository
import kotlinx.coroutines.runBlocking
import org.jdom2.Document
import org.jdom2.input.SAXBuilder
import org.junit.Test
import org.koin.core.component.inject
import org.mockito.kotlin.mock
import org.robolectric.RuntimeEnvironment

class SettingsExporterTest : K9RobolectricTest() {
    private val contentResolver = RuntimeEnvironment.getApplication().contentResolver
    private val preferences: Preferences by inject()
    private val folderSettingsProvider: FolderSettingsProvider by inject()
    private val folderQueryRepository: FolderQueryRepository by inject()
    private val settingsExporter = SettingsExporter(
        contentResolver,
        preferences,
        folderSettingsProvider,
        folderQueryRepository,
        notificationSettingsUpdater = mock(),
        filePrefixProvider = mock(),
    )

    @Test
    fun exportPreferences_producesXML() = runTest {
        val document = exportPreferences(false, emptySet())

        assertThat(document.rootElement.name).isEqualTo("k9settings")
    }

    @Test
    fun exportPreferences_setsVersionToLatest() = runTest {
        val document = exportPreferences(false, emptySet())

        assertThat(document.rootElement.getAttributeValue("version")).isEqualTo(Settings.VERSION.toString())
    }

    @Test
    fun exportPreferences_setsFormatTo1() = runTest {
        val document = exportPreferences(false, emptySet())

        assertThat(document.rootElement.getAttributeValue("format")).isEqualTo("1")
    }

    @Test
    fun exportPreferences_exportsGlobalSettingsWhenRequested() = runTest {
        val document = exportPreferences(true, emptySet())

        assertThat(document.rootElement.getChild("global")).isNotNull()
    }

    @Test
    fun exportPreferences_ignoresGlobalSettingsWhenRequested() = runTest {
        val document = exportPreferences(false, emptySet())

        assertThat(document.rootElement.getChild("global")).isNull()
    }

    @Test
    fun exportPreferences_carriesMailboxsOwnSettings() = runTest {
        val before = ForkSettings.read
        ForkSettings.read = { mapOf(ForkSettings.DARK_MODE to "true", ForkSettings.TURN_WITH_PHONE to "false") }
        try {
            val global = exportPreferences(true, emptySet()).rootElement.getChild("global")
            val values = global.children.associate { it.getAttributeValue("key") to it.text }
            assertThat(values[ForkSettings.DARK_MODE]).isEqualTo("true")
            assertThat(values[ForkSettings.TURN_WITH_PHONE]).isEqualTo("false")
            // Not handed over: written as its default.
            assertThat(values[ForkSettings.LOCK_SCREEN_UNREAD]).isEqualTo("true")
        } finally {
            ForkSettings.read = before
        }
    }

    private suspend fun exportPreferences(globalSettings: Boolean, accounts: Set<String>): Document = runBlocking {
        ByteArrayOutputStream().use { outputStream ->
            settingsExporter.exportPreferences(outputStream, globalSettings, accounts, includePasswords = false)
            parseXml(outputStream.toByteArray())
        }
    }

    private fun parseXml(xml: ByteArray): Document {
        return SAXBuilder().build(xml.inputStream())
    }
}
