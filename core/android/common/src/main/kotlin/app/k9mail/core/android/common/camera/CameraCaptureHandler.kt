package app.k9mail.core.android.common.camera

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.core.app.ActivityCompat.startActivityForResult
import app.k9mail.core.android.common.camera.io.CaptureImageFileWriter

class CameraCaptureHandler(
    private val captureImageFileWriter: CaptureImageFileWriter,
) {

    private var capturedImageUri: Uri? = null

    companion object {
        const val REQUEST_IMAGE_CAPTURE: Int = 6

        // Kept public so callers can namespace the key inside their own
        // saved-state bundles if they prefer; the save/restore helpers
        // below use it by default.
        const val STATE_KEY_CAPTURED_IMAGE_URI: String =
            "app.k9mail.core.android.common.camera.CAPTURED_IMAGE_URI"
    }

    /**
     * Returns the URI the camera was told to write to for the last
     * [openCamera] call, or `null` if the handler has no captured URI
     * on record. Callers must handle the null case (see issue #11296:
     * when the app process is killed while the camera is in the
     * foreground, the URI is not restored and the activity result
     * arrives with data=null).
     */
    fun getCapturedImageUri(): Uri? = capturedImageUri

    /**
     * Persist the captured-image URI into [outState] so it survives
     * process death alongside the hosting Activity's state. Wire this
     * up from the Activity's `onSaveInstanceState`.
     */
    fun saveInstanceState(outState: Bundle) {
        capturedImageUri?.let { outState.putParcelable(STATE_KEY_CAPTURED_IMAGE_URI, it) }
    }

    /**
     * Restore the captured-image URI from the given saved-state bundle
     * (or clear it when [savedInstanceState] is null). Wire this up
     * from the Activity's `onCreate(savedInstanceState)`.
     */
    fun restoreInstanceState(savedInstanceState: Bundle?) {
        capturedImageUri = savedInstanceState?.let {
            @Suppress("DEPRECATION")
            it.getParcelable(STATE_KEY_CAPTURED_IMAGE_URI) as? Uri
        }
    }

    fun canLaunchCamera(context: Context) =
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    /**
     * Every installed camera app that takes a photo when another app asks it to. Each package is
     * asked by name, because Android 11 and later send an unaddressed request only to the camera
     * the phone came with, and some phones' own camera (the Kompakt's among them) takes none.
     */
    fun cameraApps(context: Context): List<CameraApp> {
        val pm = context.packageManager
        val found = LinkedHashMap<String, CameraApp>()

        @Suppress("DEPRECATION")
        val answering = pm.queryIntentActivities(Intent(MediaStore.ACTION_IMAGE_CAPTURE), 0) +
            pm.getInstalledPackages(0).flatMap { info ->
                pm.queryIntentActivities(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE).setPackage(info.packageName),
                    0,
                )
            }

        for (resolved in answering) {
            val packageName = resolved.activityInfo?.packageName ?: continue
            if (packageName == context.packageName || packageName in found) continue
            found[packageName] = CameraApp(packageName, resolved.loadLabel(pm).toString())
        }
        // Two cameras with one name (a phone's own "Camera" beside another "Camera") are told
        // apart by their package names.
        val labels = found.values.groupingBy { it.label }.eachCount()
        return found.values
            .map { if (labels.getValue(it.label) > 1) it.copy(label = "${it.label} (${it.packageName})") else it }
            .sortedBy { it.label.lowercase() }
    }

    /**
     * Asks the camera app [packageName] for a photo written into this app's own file.
     * Returns false, and starts nothing, when that app cannot be started.
     */
    fun openCamera(activity: Activity, packageName: String): Boolean {
        val uri = captureImageFileWriter.getFileUri()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            .setPackage(packageName)
            .putExtra(MediaStore.EXTRA_OUTPUT, uri)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // The grant travels with the clip as well as the flags, for camera apps that read it there.
        intent.clipData = ClipData.newRawUri("", uri)

        return try {
            startActivityForResult(activity, intent, REQUEST_IMAGE_CAPTURE, null)
            capturedImageUri = uri
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }
}

/** A camera app that takes a photo when another app asks it to. */
data class CameraApp(val packageName: String, val label: String)
