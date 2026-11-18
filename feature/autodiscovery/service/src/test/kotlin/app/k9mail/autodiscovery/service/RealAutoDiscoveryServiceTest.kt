package app.k9mail.autodiscovery.service

import app.k9mail.autodiscovery.api.AutoDiscovery
import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.AutoDiscoveryResult.NoUsableSettingsFound
import app.k9mail.autodiscovery.api.AutoDiscoveryRunnable
import app.k9mail.autodiscovery.autoconfig.createBundledAutoconfigDiscovery
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import net.thunderbird.core.common.mail.EmailAddress
import net.thunderbird.core.common.mail.toUserEmailAddress
import net.thunderbird.core.logging.testing.TestLogger
import net.thunderbird.legacy.logging.Log

class RealAutoDiscoveryServiceTest {
    private val lookup = RecordingLookup()
    private val testSubject = RealAutoDiscoveryService(
        RealAutoDiscoveryRegistry(listOf(createBundledAutoconfigDiscovery(), lookup)),
    )

    @BeforeTest
    fun setUp() {
        Log.logger = TestLogger()
    }

    @Test
    fun `an address the app knows is set up without any lookup`() = runTest {
        val result = testSubject.discover("ada@gmail.com".toUserEmailAddress())

        assertThat(result).isInstanceOf<AutoDiscoveryResult.Settings>()
        assertThat(lookup.asked).isFalse()
    }

    @Test
    fun `any other address is looked up`() = runTest {
        val result = testSubject.discover("tomas@example.org".toUserEmailAddress())

        assertThat(result).isEqualTo(NoUsableSettingsFound)
        assertThat(lookup.asked).isTrue()
    }

    private class RecordingLookup : AutoDiscovery {
        var asked = false

        override fun initDiscovery(email: EmailAddress): List<AutoDiscoveryRunnable> {
            asked = true
            return listOf(AutoDiscoveryRunnable { NoUsableSettingsFound })
        }
    }
}
