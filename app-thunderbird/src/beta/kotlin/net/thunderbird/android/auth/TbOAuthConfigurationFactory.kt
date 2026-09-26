package net.thunderbird.android.auth

import net.thunderbird.core.common.oauth.OAuthConfiguration
import net.thunderbird.core.common.oauth.OAuthConfigurationFactory

/**
 * No OAuth providers. Thunderbird's sign-ins through Google, Microsoft, Yahoo, AOL and Fastmail
 * use client ids registered to Mozilla's app, not to this one, so accounts sign in with a
 * password, or an app password where the provider requires one.
 */
class TbOAuthConfigurationFactory : OAuthConfigurationFactory {
    override fun createConfigurations(): Map<List<String>, OAuthConfiguration> = emptyMap()
}
