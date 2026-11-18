package app.k9mail.autodiscovery.service

import app.k9mail.autodiscovery.api.AutoDiscoveryRegistry
import app.k9mail.autodiscovery.api.AutoDiscoveryResult
import app.k9mail.autodiscovery.api.AutoDiscoveryService
import app.k9mail.autodiscovery.autoconfig.BundledAutoconfigDiscovery
import net.thunderbird.core.common.mail.EmailAddress

/**
 * Uses Thunderbird's Autoconfig mechanism to find mail server settings for a given email address.
 */
class RealAutoDiscoveryService(
    private val autoDiscoveryRegistry: AutoDiscoveryRegistry,
) : AutoDiscoveryService {

    override suspend fun discover(email: EmailAddress): AutoDiscoveryResult {
        val (bundled, lookups) = autoDiscoveryRegistry.getAutoDiscoveries()
            .partition { it is BundledAutoconfigDiscovery }

        // An address at a service the app carries the settings for is set up from them alone:
        // no lookup goes out for it, not even one cancelled a moment later.
        val known = bundled.flatMap { it.initDiscovery(email) }
        if (known.isNotEmpty()) {
            val result = PriorityParallelRunner(runnables = known).run()
            if (result is AutoDiscoveryResult.Settings) return result
        }

        val runner = PriorityParallelRunner(
            runnables = lookups.flatMap { it.initDiscovery(email) },
        )
        return runner.run()
    }
}
