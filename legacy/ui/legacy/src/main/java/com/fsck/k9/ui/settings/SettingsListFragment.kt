package com.fsck.k9.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.k9mail.core.ui.legacy.designsystem.atom.icon.Icons
import app.k9mail.feature.launcher.FeatureLauncherActivity
import app.k9mail.feature.launcher.FeatureLauncherTarget
import com.fsck.k9.ui.R
import com.fsck.k9.ui.base.livedata.observeNotNull
import com.fsck.k9.ui.settings.account.AccountSettingsActivity
import com.fsck.k9.view.DraggableFrameLayout
import com.fsck.k9.view.turnsAPageOnSwipe
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.drag.SimpleDragCallback
import com.mikepenz.fastadapter.utils.DragDropUtil
import net.thunderbird.core.android.account.LegacyAccountDto
import net.thunderbird.core.common.provider.BrandNameProvider
import net.thunderbird.core.ui.theme.api.FeatureThemeProvider
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import app.k9mail.feature.settings.importing.R as SettingsImportR

class SettingsListFragment : Fragment(), ItemTouchCallback {
    private val viewModel: SettingsViewModel by viewModel()
    private val themeProvider: FeatureThemeProvider by inject()
    private val brandNameProvider: BrandNameProvider by inject()

    private lateinit var itemAdapter: ItemAdapter<GenericItem>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeSettingsList(recyclerView = view.findViewById(R.id.settings_list))
        populateSettingsList()
        initializeAbout(view.findViewById(R.id.about_host))
    }

    // About is an i in the top bar opening a dialog, not a row: it is not a setting.
    private fun initializeAbout(host: ComposeView) {
        var showAbout by mutableStateOf(false)
        host.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        host.setContent {
            if (showAbout) {
                themeProvider.WithTheme {
                    AboutDialog(version = appVersion(), onDismiss = { showAbout = false })
                }
            }
        }

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menu.add(Menu.NONE, R.id.settings_about, Menu.NONE, R.string.about_action).apply {
                        setIcon(Icons.Outlined.Info)
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    if (menuItem.itemId != R.id.settings_about) return false
                    showAbout = true
                    return true
                }
            },
            viewLifecycleOwner,
        )
    }

    private fun appVersion(): String {
        val context = requireContext()
        return context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }

    private fun initializeSettingsList(recyclerView: RecyclerView) {
        itemAdapter = ItemAdapter()

        val touchCallBack = SimpleDragCallback(this).apply {
            setIsDragEnabled(true)
        }
        val touchHelper = ItemTouchHelper(touchCallBack)

        val settingsListAdapter = FastAdapter.with(itemAdapter).apply {
            setHasStableIds(true)
            onClickListener = { _, _, item, _ ->
                handleItemClick(item)
                true
            }
            addEventHook(
                DragHandleTouchEvent { position ->
                    recyclerView.findViewHolderForAdapterPosition(position)?.let { viewHolder ->
                        touchHelper.startDrag(viewHolder)
                    }
                },
            )
        }

        recyclerView.adapter = settingsListAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.turnsAPageOnSwipe()
        touchHelper.attachToRecyclerView(recyclerView)
    }

    private fun populateSettingsList() {
        viewModel.accounts.observeNotNull(this) { accounts ->
            if (accounts.isEmpty()) {
                launchOnboarding()
            } else {
                populateSettingsList(accounts)
            }
        }
    }

    private fun populateSettingsList(accounts: List<LegacyAccountDto>) {
        val listItems = buildSettingsList {
            addAction(
                text = getString(R.string.general_settings_title),
                navigationAction = R.id.action_settingsListScreen_to_generalSettingsScreen,
                icon = Icons.Outlined.Settings,
            )

            addSection(title = getString(R.string.accounts_title)) {
                val isDraggable = accounts.size > 1
                for (account in accounts) {
                    addAccount(account, isDraggable)
                }

                addAction(
                    text = getString(R.string.add_account_action),
                    navigationAction = R.id.action_settingsListScreen_to_addAccountScreen,
                    icon = Icons.Outlined.Add,
                )
            }

            addSection(title = getString(R.string.settings_list_backup_category)) {
                addAction(
                    text = getString(R.string.settings_export_title),
                    navigationAction = R.id.action_settingsListScreen_to_settingsExportScreen,
                    icon = Icons.Outlined.Upload,
                )

                addAction(
                    text = getString(SettingsImportR.string.settings_import_title),
                    navigationAction = R.id.action_settingsListScreen_to_settingsImportScreen,
                    icon = Icons.Outlined.Download,
                )
            }

        }

        itemAdapter.setNewList(listItems)
    }


    private fun handleItemClick(item: GenericItem) {
        when (item) {
            is AccountItem -> launchAccountSettings(item.account)
            is IntentItem -> startActivity(item.intent)
            is SettingsActionItem -> findNavController().navigate(item.navigationAction)
            is UrlActionItem -> openUrl(item.url)
        }
    }

    private fun openUrl(url: String) {
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(viewIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.error_activity_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchAccountSettings(account: LegacyAccountDto) {
        AccountSettingsActivity.start(requireActivity(), account.uuid)
    }

    private fun launchOnboarding() {
        FeatureLauncherActivity.launch(requireActivity(), FeatureLauncherTarget.Onboarding)

        requireActivity().finishAffinity()
    }

    private fun buildSettingsList(block: SettingsListBuilder.() -> Unit): List<GenericItem> {
        return SettingsListBuilder().apply(block).toList()
    }

    internal class SettingsListBuilder {
        private val settingsList = mutableListOf<GenericItem>()
        private var itemId = 0L

        fun addAction(text: String, @IdRes navigationAction: Int, @DrawableRes icon: Int) {
            itemId++
            settingsList.add(SettingsActionItem(itemId, text, navigationAction, icon))
        }

        fun addUrlAction(text: String, url: String, @DrawableRes icon: Int) {
            itemId++
            settingsList.add(UrlActionItem(itemId, text, url, icon))
        }

        fun addAccount(account: LegacyAccountDto, isDraggable: Boolean) {
            settingsList.add(AccountItem(account, isDraggable))
        }

        fun addIntent(text: String, @DrawableRes icon: Int, intent: Intent) {
            itemId++
            settingsList.add(IntentItem(itemId, text, icon, intent))
        }

        fun addSection(title: String, block: SettingsListBuilder.() -> Unit) {
            itemId++
            settingsList.add(SettingsDividerItem(itemId, title))
            block()
        }

        fun toList(): List<GenericItem> = settingsList
    }

    override fun itemTouchStartDrag(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder.itemView as DraggableFrameLayout).isDragged = true
    }

    override fun itemTouchStopDrag(viewHolder: RecyclerView.ViewHolder) {
        (viewHolder.itemView as DraggableFrameLayout).isDragged = false
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        val firstDropPosition = itemAdapter.adapterItems.indexOfFirst { it is AccountItem }
        val lastDropPosition = itemAdapter.adapterItems.indexOfLast { it is AccountItem }

        return if (newPosition in firstDropPosition..lastDropPosition) {
            DragDropUtil.onMove(itemAdapter, oldPosition, newPosition)
            true
        } else {
            false
        }
    }

    override fun itemTouchDropped(oldPosition: Int, newPosition: Int) {
        if (oldPosition == newPosition) return

        val account = (itemAdapter.getAdapterItem(newPosition) as AccountItem).account
        val firstAccountPosition = itemAdapter.adapterItems.indexOfFirst { it is AccountItem }
        val newAccountPosition = newPosition - firstAccountPosition

        viewModel.moveAccount(account, newAccountPosition)
    }
}
