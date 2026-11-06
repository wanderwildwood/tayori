package app.k9mail.core.android.common.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.wanderwildwood.tayori.files.FilesPicker

class CreateDocumentResultContract : ActivityResultContract<CreateDocumentResultContract.Input, Uri?>() {
    override fun createIntent(context: Context, input: Input): Intent {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input.mimeType)
            .putExtra(Intent.EXTRA_TITLE, input.title)
        return FilesPicker.aim(context, intent, writable = true)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }

    data class Input(
        val title: String,
        val mimeType: String,
    )
}
