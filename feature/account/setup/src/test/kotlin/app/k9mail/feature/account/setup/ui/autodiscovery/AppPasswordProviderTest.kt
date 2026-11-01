package app.k9mail.feature.account.setup.ui.autodiscovery

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class AppPasswordProviderTest {

    @Test
    fun `maps known domains to their provider`() {
        val expected = mapOf(
            "ada@gmail.com" to AppPasswordProvider.GMAIL,
            "ada@googlemail.com" to AppPasswordProvider.GMAIL,
            "ada@icloud.com" to AppPasswordProvider.ICLOUD,
            "ada@me.com" to AppPasswordProvider.ICLOUD,
            "ada@mac.com" to AppPasswordProvider.ICLOUD,
            "ada@zoho.com" to AppPasswordProvider.ZOHO,
            "ada@zohomail.eu" to AppPasswordProvider.ZOHO,
            "ada@yahoo.com" to AppPasswordProvider.YAHOO,
            "ada@aol.com" to AppPasswordProvider.AOL,
        )
        expected.forEach { (address, provider) -> assertThat(appPasswordProviderFor(address)).isEqualTo(provider) }
    }

    @Test
    fun `ignores case and spaces`() {
        assertThat(appPasswordProviderFor("  Ada@GMail.com ")).isEqualTo(AppPasswordProvider.GMAIL)
    }

    @Test
    fun `other domains and incomplete addresses have no provider`() {
        assertThat(appPasswordProviderFor("ada@example.org")).isNull()
        assertThat(appPasswordProviderFor("ada@notgmail.com")).isNull()
        assertThat(appPasswordProviderFor("ada@evil.com.zoho.com.example")).isNull()
        assertThat(appPasswordProviderFor("ada")).isNull()
        assertThat(appPasswordProviderFor("")).isNull()
    }
}
