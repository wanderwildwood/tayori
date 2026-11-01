package app.k9mail.feature.account.setup.ui.autodiscovery

/**
 * Providers that refuse the normal account password for mail apps and want an app password
 * instead. Pages checked 2026-11-01.
 */
internal enum class AppPasswordProvider(val helpUrl: String) {
    GMAIL("https://myaccount.google.com/apppasswords"),
    ICLOUD("https://support.apple.com/en-us/102654"),
    ZOHO("https://accounts.zoho.com/home#security/app_password"),
    YAHOO("https://help.yahoo.com/kb/SLN15241.html"),
    AOL("https://help.aol.com/articles/create-and-manage-app-password"),
}

/** The provider an email address belongs to, if it needs an app password; otherwise null. */
internal fun appPasswordProviderFor(emailAddress: String): AppPasswordProvider? {
    val domain = emailAddress.substringAfterLast("@", "").trim().lowercase()
    return when {
        domain == "gmail.com" || domain == "googlemail.com" -> AppPasswordProvider.GMAIL
        domain == "icloud.com" || domain == "me.com" || domain == "mac.com" -> AppPasswordProvider.ICLOUD
        domain == "zoho.com" || domain.startsWith("zohomail.") -> AppPasswordProvider.ZOHO
        domain == "yahoo.com" || domain == "ymail.com" || domain == "rocketmail.com" -> AppPasswordProvider.YAHOO
        domain == "aol.com" -> AppPasswordProvider.AOL
        else -> null
    }
}
