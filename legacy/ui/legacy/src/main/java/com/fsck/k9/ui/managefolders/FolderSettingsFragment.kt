package com.fsck.k9.ui.managefolders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.k9mail.legacy.ui.folder.FolderNameFormatter
import com.fsck.k9.ui.settings.ArmedPreference
import com.fsck.k9.fragment.ConfirmationDialogFragment.ConfirmationDialogFragmentListener
import com.fsck.k9.ui.R
import com.fsck.k9.ui.base.livedata.observeNotNull
import com.fsck.k9.ui.settings.withoutIconSpace
import com.fsck.k9.view.turnsAPageOnSwipe
import com.takisoft.preferencex.PreferenceFragmentCompat
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.fsck.k9.ui.base.R as BaseR

class FolderSettingsFragment : PreferenceFragmentCompat(), ConfirmationDialogFragmentListener {
    private val viewModel: FolderSettingsViewModel by viewModel()
    private val folderNameFormatter: FolderNameFormatter by inject()

    override fun onCreateRecyclerView(
        inflater: LayoutInflater,
        parent: ViewGroup,
        savedInstanceState: Bundle?,
    ): RecyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState).apply {
        itemAnimator = null
        turnsAPageOnSwipe()
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        preferenceScreen.withoutIconSpace()
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        // Set empty preferences resource while data is being loaded
        setPreferencesFromResource(R.xml.empty_preferences, null)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments ?: error("Arguments missing")
        val accountUuid = arguments.getString(EXTRA_ACCOUNT) ?: error("Missing argument '$EXTRA_ACCOUNT'")
        val folderId = arguments.getLong(EXTRA_FOLDER_ID)

        viewModel.getFolderSettingsLiveData(accountUuid, folderId)
            .observeNotNull(viewLifecycleOwner) { folderSettingsResult ->
                when (folderSettingsResult) {
                    is FolderNotFound -> navigateBack()
                    is FolderSettingsData -> initPreferences(folderSettingsResult)
                }
            }

        viewModel.getActionEvents().observeNotNull(this) { handleActionEvents(it) }
    }

    private fun navigateBack() {
        findNavController().popBackStack()
    }

    private fun initPreferences(folderSettings: FolderSettingsData) {
        preferenceManager.preferenceDataStore = folderSettings.dataStore
        setPreferencesFromResource(R.xml.folder_settings_preferences, null)

        setCategoryTitle(folderSettings)
        if (viewModel.showClearFolderInMenu) {
            preferenceScreen.addPreference(
                ArmedPreference(
                    context = requireContext(),
                    restingTitle = getString(R.string.folder_settings_clear_local_folder_action),
                    armedTitle = getString(R.string.folder_settings_clear_local_folder_armed_action),
                    onConfirmed = { viewModel.onClearFolderConfirmation() },
                ).apply { order = Int.MAX_VALUE },
            )
        }
        setPreferenceVisibility(folderSettings)
    }

    private fun setCategoryTitle(folderSettings: FolderSettingsData) {
        val folderDisplayName = folderNameFormatter.displayName(folderSettings.folder)
        findPreference<Preference>(PREFERENCE_TOP_CATEGORY)!!.title = folderDisplayName
    }

    private fun handleActionEvents(action: Action) {
        when (action) {
            is Action.ShowClearFolderConfirmationDialog -> viewModel.onClearFolderConfirmation()
        }
    }

    private fun setPreferenceVisibility(folderSettings: FolderSettingsData) {
        if (folderSettings.folder.isLocalOnly) {
            requirePreference<Preference>(PREFERENCE_SYNC).isVisible = false
            requirePreference<Preference>(PREFERENCE_PUSH).isVisible = false
            requirePreference<Preference>(PREFERENCE_NOTIFICATIONS).isVisible = false
        }
    }

    override fun doPositiveClick(dialogId: Int) {
        when (dialogId) {
            DIALOG_CLEAR_FOLDER -> {
                viewModel.onClearFolderConfirmation()
            }
        }
    }

    override fun doNegativeClick(dialogId: Int) = Unit

    override fun dialogCancelled(dialogId: Int) = Unit

    private fun <T : Preference> requirePreference(key: String): T {
        return findPreference(key) ?: error("Preference '$key' not found")
    }

    companion object {
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_FOLDER_ID = "folderId"

        private const val DIALOG_CLEAR_FOLDER = 1
        private const val REQUEST_CLEAR_FOLDER = 1
        private const val TAG_CLEAR_FOLDER_CONFIRMATION = "clear_folder_confirmation"

        private const val PREFERENCE_TOP_CATEGORY = "folder_settings"
        private const val PREFERENCE_SYNC = "folder_settings_sync"
        private const val PREFERENCE_PUSH = "folder_settings_push"
        private const val PREFERENCE_NOTIFICATIONS = "folder_settings_notifications"
    }
}
