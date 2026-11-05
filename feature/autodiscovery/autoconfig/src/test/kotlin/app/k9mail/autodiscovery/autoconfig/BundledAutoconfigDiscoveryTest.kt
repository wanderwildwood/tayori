package app.k9mail.autodiscovery.autoconfig

import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.ConnectionSecurity
import app.k9mail.autodiscovery.api.ImapServerSettings
import app.k9mail.autodiscovery.api.SmtpServerSettings
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import net.thunderbird.core.common.mail.toUserEmailAddress
import net.thunderbird.core.logging.testing.TestLogger
import net.thunderbird.legacy.logging.Log

class BundledAutoconfigDiscoveryTest {
    private val discovery = createBundledAutoconfigDiscovery()

    @BeforeTest
    fun setUp() {
        // The parser logs the elements it skips; without a logger the first log call throws.
        Log.logger = TestLogger()
    }

    private suspend fun discover(address: String): Pair<ImapServerSettings, SmtpServerSettings> {
        val runnables = discovery.initDiscovery(address.toUserEmailAddress())
        val result = runnables.single().run()
        assertThat(result).isInstanceOf<AutoDiscoveryResult.Settings>()
        result as AutoDiscoveryResult.Settings
        assertThat(result.isTrusted).isEqualTo(true)
        return result.incomingServerSettings as ImapServerSettings to
            result.outgoingServerSettings as SmtpServerSettings
    }

    private fun ImapServerSettings.summary() = "${hostname.value}:${port.value} $connectionSecurity $username"
    private fun SmtpServerSettings.summary() = "${hostname.value}:${port.value} $connectionSecurity $username"

    @Test
    fun gmail() = runTest {
        val (imap, smtp) = discover("ada@gmail.com")
        assertThat(imap.summary()).isEqualTo("imap.gmail.com:993 ${ConnectionSecurity.TLS} ada@gmail.com")
        assertThat(smtp.summary()).isEqualTo("smtp.gmail.com:465 ${ConnectionSecurity.TLS} ada@gmail.com")
    }

    @Test
    fun icloud() = runTest {
        val (imap, smtp) = discover("ada@icloud.com")
        assertThat(imap.summary()).isEqualTo("imap.mail.me.com:993 ${ConnectionSecurity.TLS} ada")
        assertThat(smtp.summary()).isEqualTo("smtp.mail.me.com:587 ${ConnectionSecurity.StartTLS} ada@icloud.com")
    }

    @Test
    fun `zoho by region`() = runTest {
        val cases = mapOf(
            "ada@zoho.com" to "zoho.com",
            "ada@zohomail.com" to "zoho.com",
            "ada@zoho.eu" to "zoho.eu",
            "ada@zohomail.eu" to "zoho.eu",
            "ada@zoho.in" to "zoho.in",
            "ada@zoho.com.au" to "zoho.com.au",
            "ada@zohomail.com.au" to "zoho.com.au",
            "ada@zoho.jp" to "zoho.jp",
        )
        for ((address, suffix) in cases) {
            val (imap, smtp) = discover(address)
            assertThat(imap.summary()).isEqualTo("imap.$suffix:993 ${ConnectionSecurity.TLS} $address")
            assertThat(smtp.summary()).isEqualTo("smtp.$suffix:465 ${ConnectionSecurity.TLS} $address")
        }
    }

    @Test
    fun `other domains are left to the network lookups`() {
        assertThat(discovery.initDiscovery("ada@example.com".toUserEmailAddress())).isEmpty()
        assertThat(bundledConfigFor("zoho.example")).isNull()
    }
}
