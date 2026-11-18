package com.fsck.k9.preferences

internal class GeneralSettingsUpgrader {
    fun upgrade(contentVersion: Int, internalSettings: InternalSettingsMap): InternalSettingsMap {
        if (contentVersion == Settings.VERSION) {
            return internalSettings
        }

        // Mailbox's own settings are set only from a file that carries them, never from the
        // defaults the upgrade fills in.
        return GeneralSettingsDescriptions.upgrade(contentVersion, internalSettings)
            .filterKeys { it !in ForkSettings.ADDED || it in internalSettings }
    }
}
