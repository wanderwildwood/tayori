package net.thunderbird.android.auth

import net.thunderbird.core.common.oauth.OAuthConfiguration
import net.thunderbird.core.common.oauth.OAuthConfigurationFactory

/**
 * Microsoft only, through a client id registered to this app. Thunderbird's sign-ins through
 * Google, Yahoo, AOL and Fastmail use client ids registered to Mozilla's app, not to this one, so
 * those accounts sign in with a password, or an app password where the provider requires one.
 *
 * Microsoft stopped taking passwords from mail apps, so without this Outlook.com and Microsoft 365
 * could not be used at all. The redirect carries the hash of the release signing certificate, which
 * the registration names; a build signed with any other key cannot finish a sign-in.
 */
class TbOAuthConfigurationFactory : OAuthConfigurationFactory {
    override fun createConfigurations(): Map<List<String>, OAuthConfiguration> {
        if (MICROSOFT_CLIENT_ID.isEmpty()) return emptyMap()
        return mapOf(createMicrosoftConfiguration())
    }

    private fun createMicrosoftConfiguration(): Pair<List<String>, OAuthConfiguration> {
        return listOf(
            "outlook.office365.com",
            "smtp.office365.com",
            "smtp-mail.outlook.com",
        ) to OAuthConfiguration(
            clientId = MICROSOFT_CLIENT_ID,
            scopes = listOf(
                "profile",
                "openid",
                "email",
                "https://outlook.office.com/IMAP.AccessAsUser.All",
                "https://outlook.office.com/SMTP.Send",
                "offline_access",
            ),
            authorizationEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
            tokenEndpoint = "https://login.microsoftonline.com/common/oauth2/v2.0/token",
            redirectUri = "msauth://com.wanderwildwood.tayori/4sCCOZY%2F%2Bqua9Gc42aT3D5zAACg%3D",
        )
    }

    private companion object {
        /** The Application (client) id of the "Email" registration at entra.microsoft.com. */
        const val MICROSOFT_CLIENT_ID = ""
    }
}
