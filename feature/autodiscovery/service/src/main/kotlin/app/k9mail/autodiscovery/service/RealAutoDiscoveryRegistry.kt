package app.k9mail.autodiscovery.service

import app.k9mail.autodiscovery.api.AutoDiscovery
import app.k9mail.autodiscovery.api.AutoDiscoveryRegistry
import app.k9mail.autodiscovery.autoconfig.AutoconfigUrlConfig
import app.k9mail.autodiscovery.autoconfig.createIspDbAutoconfigDiscovery
import app.k9mail.autodiscovery.autoconfig.createMxLookupAutoconfigDiscovery
import app.k9mail.autodiscovery.autoconfig.createProviderAutoconfigDiscovery
import okhttp3.OkHttpClient

class RealAutoDiscoveryRegistry(
    private val autoDiscoveries: List<AutoDiscovery> = emptyList(),
) : AutoDiscoveryRegistry {

    override fun getAutoDiscoveries(): List<AutoDiscovery> = autoDiscoveries

    companion object {
        // HTTPS only: plain-HTTP lookups are refused by the network security config anyway, and a
        // setup answer anyone on the network could rewrite is not worth asking for.
        private val defaultAutoconfigUrlConfig = AutoconfigUrlConfig(
            httpsOnly = true,
            includeEmailAddress = false,
        )

        fun createDefaultAutoDiscoveries(
            okHttpClient: OkHttpClient,
            autoconfigUrlConfig: AutoconfigUrlConfig = defaultAutoconfigUrlConfig,
        ): List<AutoDiscovery> {
            return listOf(
                createProviderAutoconfigDiscovery(
                    okHttpClient = okHttpClient,
                    config = autoconfigUrlConfig,
                ),
                createIspDbAutoconfigDiscovery(
                    okHttpClient = okHttpClient,
                ),
                createMxLookupAutoconfigDiscovery(
                    okHttpClient = okHttpClient,
                    config = autoconfigUrlConfig,
                ),
            )
        }
    }
}
