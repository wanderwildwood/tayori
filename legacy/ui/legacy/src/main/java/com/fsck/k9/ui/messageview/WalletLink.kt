package com.fsck.k9.ui.messageview

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.fsck.k9.mailstore.AttachmentViewInfo

/**
 * "Add to Wallet" on a boarding pass or a ticket: the attachment goes to Wallet (satsuire),
 * which reads the barcode out of it. A pass file is opened in it, as Files would open one; a
 * picture or a PDF is shared to it. Offered only when Wallet is on the phone.
 */
object WalletLink {
    const val WALLET_APP = "com.wanderwildwood.satsuire"

    private const val PASS_TYPE = "application/vnd.apple.pkpass"
    private val PASS_TYPES = setOf(PASS_TYPE, "application/vnd-com.apple.pkpass", "application/vnd.apple.pkpasses")

    /** How the attachment is handed over, and as which type; null for anything Wallet does not read. */
    data class Kind(val action: String, val type: String)

    fun kindOf(attachment: AttachmentViewInfo): Kind? {
        val type = attachment.mimeType?.lowercase().orEmpty()
        val name = attachment.displayName?.lowercase().orEmpty()
        return when {
            // Mail often carries a pass as application/octet-stream, so its name is the surer sign.
            type in PASS_TYPES -> Kind(Intent.ACTION_VIEW, type)
            name.endsWith(".pkpasses") -> Kind(Intent.ACTION_VIEW, "application/vnd.apple.pkpasses")
            name.endsWith(".pkpass") -> Kind(Intent.ACTION_VIEW, PASS_TYPE)
            type == "application/pdf" || name.endsWith(".pdf") -> Kind(Intent.ACTION_SEND, "application/pdf")
            type.startsWith("image/") -> Kind(Intent.ACTION_SEND, type)
            else -> null
        }
    }

    /** The kind, if Wallet is installed and takes it. */
    fun offered(context: Context, attachment: AttachmentViewInfo): Kind? {
        val kind = kindOf(attachment) ?: return null
        val probe = Intent(kind.action).setPackage(WALLET_APP)
        if (kind.action == Intent.ACTION_VIEW) {
            probe.setDataAndType(Uri.parse("content://probe/pass.pkpass"), kind.type)
        } else {
            probe.type = kind.type
        }
        return if (probe.resolveActivity(context.packageManager) != null) kind else null
    }

    fun intent(kind: Kind, uri: Uri): Intent {
        val intent = Intent(kind.action).setPackage(WALLET_APP)
        if (kind.action == Intent.ACTION_VIEW) {
            intent.setDataAndType(uri, kind.type)
        } else {
            intent.type = kind.type
            intent.putExtra(Intent.EXTRA_STREAM, uri)
        }
        return intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
