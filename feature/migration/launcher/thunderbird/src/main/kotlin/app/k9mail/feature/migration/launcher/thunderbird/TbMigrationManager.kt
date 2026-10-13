package app.k9mail.feature.migration.launcher.thunderbird

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import app.k9mail.feature.migration.launcher.api.MigrationManager

/**
 * The QR-code import from Thunderbird for desktop is left out of this build: it is the only thing
 * that needed the camera permission, and settings can still be imported from a file. The contract
 * below finishes at once with no result, so any leftover route to the scanner does nothing.
 */
internal class TbMigrationManager : MigrationManager {
    override fun isFeatureIncluded() = false

    override fun getQrCodeActivityResultContract(): ActivityResultContract<Unit, Uri?> {
        return NoQrCodeScannerContract()
    }
}

private class NoQrCodeScannerContract : ActivityResultContract<Unit, Uri?>() {
    override fun createIntent(context: Context, input: Unit): Intent = Intent()

    override fun getSynchronousResult(context: Context, input: Unit): SynchronousResult<Uri?> {
        return SynchronousResult(null)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = null
}
