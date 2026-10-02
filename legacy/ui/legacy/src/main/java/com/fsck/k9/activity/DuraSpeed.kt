package com.fsck.k9.activity

import android.app.Activity
import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import app.k9mail.core.ui.legacy.designsystem.atom.icon.Icons
import com.fsck.k9.ui.R

/**
 * DuraSpeed, MediaTek's background manager on the Kompakt, force-stops installed apps a few
 * minutes after the screen goes dark, and keeps them stopped until they are opened again: no
 * mail is looked for until then. Apps switched on in its list are left alone, but no app can
 * read that list, and Settings has no way into it. Its App info page can be opened, and has an
 * Open button, so that is where the button goes; whether it was done is the person's word.
 *
 * Mudita's notification panel shows a notification without its buttons, so tapping it opens the
 * choices here instead, and coming back from DuraSpeed asks whether it was done. A system stop
 * while DuraSpeed is on takes the word back: DuraSpeed does not stop apps on its list. Android
 * records it as "stop <package> due to from pid N"; a Force stop by hand reads the same.
 */
object DuraSpeed {

    private const val CHANNEL = "duraspeed"
    private const val NOTIFICATION_ID = 20261001
    private const val EXTRA_FIX = "duraspeedFix"
    private const val PREFS = "duraspeed"
    private const val ALLOWED = "allowed"
    private const val STOP_SEEN = "stop_seen"

    /** Sent to DuraSpeed from here; the next return asks whether Email was switched on. */
    private var askOnReturn = false

    private fun isKompakt() = Build.MANUFACTURER.equals("Mudita", ignoreCase = true)

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** DuraSpeed's own switch; null where the phone does not say. */
    private fun isOn(context: Context): Boolean? = runCatching {
        val cr = context.contentResolver
        (Settings.Global.getString(cr, "setting.duraspeed.enabled")
            ?: Settings.System.getString(cr, "setting.duraspeed.enabled"))?.let { it != "0" }
    }.getOrNull()

    private fun atRisk(context: Context): Boolean {
        if (!isKompakt()) return false
        val on = isOn(context) != false
        val prefs = prefs(context)
        val stop = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching {
                context.getSystemService(ActivityManager::class.java)
                    .getHistoricalProcessExitReasons(context.packageName, 0, 0)
            }.getOrDefault(emptyList())
                .filter {
                    it.reason == ApplicationExitInfo.REASON_USER_REQUESTED &&
                        it.description?.contains("due to from pid") == true
                }
                .maxOfOrNull { it.timestamp }
        } else {
            null
        }
        if (stop != null && stop > prefs.getLong(STOP_SEEN, 0)) {
            prefs.edit().putLong(STOP_SEEN, stop).apply()
            // Only a stop while DuraSpeed is on is DuraSpeed's.
            if (on) prefs.edit().putBoolean(ALLOWED, false).apply()
        }
        return on && !prefs.getBoolean(ALLOWED, false)
    }

    private fun markAllowed(context: Context) {
        prefs(context).edit().putBoolean(ALLOWED, true).apply()
        refreshNotification(context, false)
    }

    private fun appInfo() = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        .setData(Uri.parse("package:com.mediatek.duraspeed"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun open(context: Context) {
        askOnReturn = true
        runCatching { context.startActivity(appInfo()) }
    }

    private fun refreshNotification(context: Context, show: Boolean) {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (!show) {
            nm.cancel(NOTIFICATION_ID)
            return
        }
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, context.getString(R.string.duraspeed_channel), NotificationManager.IMPORTANCE_LOW),
        )
        val fix = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MessageHomeActivity::class.java)
                .putExtra(EXTRA_FIX, true)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val text = context.getString(R.string.duraspeed_text)
        nm.notify(
            NOTIFICATION_ID,
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(Icons.Outlined.Warning)
                .setContentTitle(context.getString(R.string.duraspeed_title))
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(fix)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build(),
        )
    }

    /** From the main screen's onResume. */
    fun onResume(activity: Activity) {
        if (!isKompakt()) return
        val risk = atRisk(activity)
        refreshNotification(activity, risk)
        if (!risk) return
        val intent = activity.intent
        if (intent?.getBooleanExtra(EXTRA_FIX, false) == true) {
            intent.removeExtra(EXTRA_FIX)
            AlertDialog.Builder(activity)
                .setTitle(R.string.duraspeed_title)
                .setMessage(R.string.duraspeed_text)
                .setPositiveButton(R.string.duraspeed_open) { _, _ -> open(activity) }
                .setNegativeButton(R.string.duraspeed_allowed) { _, _ -> markAllowed(activity) }
                .setNeutralButton(R.string.duraspeed_later, null)
                .show()
        } else if (askOnReturn) {
            askOnReturn = false
            AlertDialog.Builder(activity)
                .setMessage(R.string.duraspeed_ask_done)
                .setPositiveButton(R.string.duraspeed_allowed) { _, _ -> markAllowed(activity) }
                .setNegativeButton(R.string.duraspeed_not_yet, null)
                .show()
        }
    }
}
