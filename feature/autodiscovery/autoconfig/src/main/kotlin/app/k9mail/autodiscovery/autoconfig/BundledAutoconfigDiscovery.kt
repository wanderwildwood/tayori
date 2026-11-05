package app.k9mail.autodiscovery.autoconfig

import app.k9mail.autodiscovery.api.AutoDiscovery
import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.AutoDiscoveryRunnable
import net.thunderbird.core.common.mail.EmailAddress
import net.thunderbird.legacy.logging.Log

/**
 * Autoconfig files carried inside the app for a few big mail services, so an address at one of them
 * gets its servers at once, offline too, and with the right regional Zoho host.
 *
 * The files are trimmed copies (IMAP and SMTP only) of the Thunderbird ISPDB entries at
 * https://autoconfig.thunderbird.net/v1.1/{domain}, checked 2026-11-04 against the providers' own
 * pages:
 * - Gmail: https://support.google.com/mail/answer/7126229
 * - iCloud Mail: https://support.apple.com/en-us/102525 (IMAP user name is the part before the @,
 *   SMTP user name the full address)
 * - Zoho Mail: https://www.zoho.com/mail/help/imap-access.html
 *
 * They are read by the same parser as a downloaded file. Any other domain falls through to the
 * provider, ISPDB and MX lookups.
 */
class BundledAutoconfigDiscovery internal constructor(
    private val parser: SuspendableAutoconfigParser,
) : AutoDiscovery {

    override fun initDiscovery(email: EmailAddress): List<AutoDiscoveryRunnable> {
        val domain = email.domain.normalized
        val config = bundledConfigFor(domain) ?: return emptyList()

        return listOf(
            AutoDiscoveryRunnable {
                parse(config, email, domain)
            },
        )
    }

    private suspend fun parse(config: String, email: EmailAddress, domain: String): AutoDiscoveryResult {
        return try {
            when (val result = parser.parseSettings(config.byteInputStream(), email)) {
                is AutoconfigParserResult.Settings -> AutoDiscoveryResult.Settings(
                    incomingServerSettings = result.incomingServerSettings.first(),
                    outgoingServerSettings = result.outgoingServerSettings.first(),
                    isTrusted = true,
                    source = "bundled:$domain",
                )

                is AutoconfigParserResult.ParserError -> AutoDiscoveryResult.NoUsableSettingsFound
            }
        } catch (e: AutoconfigParserException) {
            Log.d(e, "Failed to parse bundled config for %s", domain)
            AutoDiscoveryResult.NoUsableSettingsFound
        }
    }
}

fun createBundledAutoconfigDiscovery(): BundledAutoconfigDiscovery {
    return BundledAutoconfigDiscovery(SuspendableAutoconfigParser(RealAutoconfigParser()))
}

/** Zoho's regional data centres, as (mail domain, server suffix). */
private val ZOHO_REGIONS = listOf(
    "zoho.com" to "zoho.com",
    "zoho.eu" to "zoho.eu",
    "zoho.in" to "zoho.in",
    "zoho.com.au" to "zoho.com.au",
    "zoho.jp" to "zoho.jp",
)

internal fun bundledConfigFor(domain: String): String? {
    return when (domain) {
        "gmail.com", "googlemail.com" -> GMAIL
        "icloud.com", "me.com", "mac.com" -> ICLOUD
        else -> ZOHO_REGIONS.firstOrNull { (mailDomain, _) ->
            domain == mailDomain || domain == "zohomail." + mailDomain.removePrefix("zoho.")
        }?.let { (mailDomain, suffix) -> zohoConfig(mailDomain, suffix) }
    }
}

private const val GMAIL = """<?xml version="1.0" encoding="UTF-8"?>
<clientConfig version="1.1">
  <emailProvider id="googlemail.com">
    <domain>gmail.com</domain>
    <domain>googlemail.com</domain>
    <displayName>Google Mail</displayName>
    <incomingServer type="imap">
      <hostname>imap.gmail.com</hostname>
      <port>993</port>
      <socketType>SSL</socketType>
      <username>%EMAILADDRESS%</username>
      <authentication>OAuth2</authentication>
      <authentication>password-cleartext</authentication>
    </incomingServer>
    <outgoingServer type="smtp">
      <hostname>smtp.gmail.com</hostname>
      <port>465</port>
      <socketType>SSL</socketType>
      <username>%EMAILADDRESS%</username>
      <authentication>OAuth2</authentication>
      <authentication>password-cleartext</authentication>
    </outgoingServer>
  </emailProvider>
</clientConfig>
"""

private const val ICLOUD = """<?xml version="1.0" encoding="UTF-8"?>
<clientConfig version="1.1">
  <emailProvider id="icloud.com">
    <domain>icloud.com</domain>
    <domain>me.com</domain>
    <domain>mac.com</domain>
    <displayName>iCloud Mail</displayName>
    <incomingServer type="imap">
      <hostname>imap.mail.me.com</hostname>
      <port>993</port>
      <socketType>SSL</socketType>
      <username>%EMAILLOCALPART%</username>
      <authentication>password-cleartext</authentication>
    </incomingServer>
    <outgoingServer type="smtp">
      <hostname>smtp.mail.me.com</hostname>
      <port>587</port>
      <socketType>STARTTLS</socketType>
      <username>%EMAILADDRESS%</username>
      <authentication>password-cleartext</authentication>
    </outgoingServer>
  </emailProvider>
</clientConfig>
"""

private fun zohoConfig(mailDomain: String, suffix: String) = """<?xml version="1.0" encoding="UTF-8"?>
<clientConfig version="1.1">
  <emailProvider id="$mailDomain">
    <domain>$mailDomain</domain>
    <displayName>Zoho Mail</displayName>
    <incomingServer type="imap">
      <hostname>imap.$suffix</hostname>
      <port>993</port>
      <socketType>SSL</socketType>
      <username>%EMAILADDRESS%</username>
      <authentication>password-cleartext</authentication>
    </incomingServer>
    <outgoingServer type="smtp">
      <hostname>smtp.$suffix</hostname>
      <port>465</port>
      <socketType>SSL</socketType>
      <username>%EMAILADDRESS%</username>
      <authentication>password-cleartext</authentication>
    </outgoingServer>
  </emailProvider>
</clientConfig>
"""
