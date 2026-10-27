package net.thunderbird.core.preference.storage

/**
 * Gives access to the cached preferences snapshot and creates editors that keep it up to date.
 */
interface StorageProvider {

    /**
     * The current [Storage] snapshot.
     *
     * It is loaded from disk once and updated whenever an editor created by [createStorageEditor] commits.
     */
    val storage: Storage

    /**
     * Creates a [StorageEditor] for updating preferences in storage.
     */
    fun createStorageEditor(): StorageEditor
}
