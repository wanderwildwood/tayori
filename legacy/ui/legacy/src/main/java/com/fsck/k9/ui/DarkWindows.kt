package com.fsck.k9.ui

import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.compose.ui.Modifier
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.k9mail.feature.account.common.ui.house.DarkMode
import app.k9mail.feature.account.common.ui.house.DarkWindow
import app.k9mail.feature.account.common.ui.house.keepColors
import net.thunderbird.components.ui.bolt.atom.image.BoltPicture
import net.thunderbird.components.ui.bolt.organism.BoltWindow

/**
 * Dark mode for the windows the app opens on top of a page. Each is handed to [DarkMode.window]
 * as it is shown, from a few places every such window passes through:
 * - every dialog fragment (the confirmations, the settings' pickers, the bottom sheets), once started;
 * - every dialog, drop-down menu and bottom sheet of the Compose design system ([BoltWindow]);
 * - every popup and overflow menu, through its rows ([menuRow], called by the app's view inflater);
 * - and the few dialogs and popups the app builds and shows itself ([dialog], [popup]).
 * Nothing is searched for and nothing waits.
 */
object DarkWindows {

    fun install(application: Application) {
        BoltWindow.opened = { DarkWindow() }
        // The design system's pictures (attachment thumbnails, avatars) keep their colors.
        BoltPicture.kept = Modifier.keepColors()
        application.registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, state: Bundle?) {
                    if (activity is FragmentActivity) {
                        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(dialogFragments, true)
                    }
                }

                override fun onActivityStarted(activity: Activity) = Unit
                override fun onActivityResumed(activity: Activity) = Unit
                override fun onActivityPaused(activity: Activity) = Unit
                override fun onActivityStopped(activity: Activity) = Unit
                override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
                override fun onActivityDestroyed(activity: Activity) = Unit
            },
        )
    }

    /** A dialog fragment shows its dialog as it starts. */
    private val dialogFragments = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            if (f is DialogFragment) dialog(f.dialog)
        }
    }

    /** A dialog the app builds itself: call it once the dialog is made (before or after it is shown). */
    @JvmStatic
    fun dialog(dialog: Dialog?) {
        dialog?.window?.decorView?.let { DarkMode.window(it) }
    }

    /** A popup window the app shows itself: call it after it is shown, with any view in it. */
    @JvmStatic
    fun popup(anyViewInIt: View) = DarkMode.window(anyViewInIt)

    /** A row of a popup or overflow menu, or of a drop-down list: its window goes dark as the row is put in it. */
    @JvmStatic
    fun menuRow(row: View) {
        row.addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = DarkMode.window(v)
                override fun onViewDetachedFromWindow(v: View) = Unit
            },
        )
    }
}
