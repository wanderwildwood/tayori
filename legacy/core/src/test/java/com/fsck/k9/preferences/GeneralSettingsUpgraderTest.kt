package com.fsck.k9.preferences

import assertk.assertThat
import assertk.assertions.containsKey
import assertk.assertions.doesNotContainKey
import assertk.assertions.isEqualTo
import com.fsck.k9.K9RobolectricTest
import org.junit.Test

class GeneralSettingsUpgraderTest : K9RobolectricTest() {
    private val upgrader = GeneralSettingsUpgrader()

    @Test
    fun `a file from before Mailbox's own settings does not set them to their defaults`() {
        val settings = mapOf<String, Any?>("confirmDelete" to true)

        val upgraded = upgrader.upgrade(110, settings)

        ForkSettings.ADDED.forEach { assertThat(upgraded).doesNotContainKey(it) }
        assertThat(upgraded["confirmDelete"]).isEqualTo(true)
    }

    @Test
    fun `upstream settings added since the file was written still get their defaults`() {
        val upgraded = upgrader.upgrade(109, emptyMap())

        assertThat(upgraded).containsKey("messageListDateTimeFormat")
    }

    @Test
    fun `a file that carries Mailbox's own settings keeps them`() {
        val settings = mapOf<String, Any?>(ForkSettings.TEXT_SCALE to 120, ForkSettings.DARK_MODE to true)

        val upgraded = upgrader.upgrade(Settings.VERSION, settings)

        assertThat(upgraded[ForkSettings.TEXT_SCALE]).isEqualTo(120)
        assertThat(upgraded[ForkSettings.DARK_MODE]).isEqualTo(true)
    }
}
