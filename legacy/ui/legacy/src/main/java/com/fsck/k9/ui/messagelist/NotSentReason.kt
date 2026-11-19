package com.fsck.k9.ui.messagelist

import androidx.annotation.StringRes
import com.fsck.k9.ui.R

/**
 * Why an Outbox message was not sent, in plain words where the error is a common one. The
 * stored reason is the error's own text (as the send-failed notice shows it), so it is read
 * by the words Android and the mail code put in it; anything not recognized is shown as it is.
 */
internal enum class NotSentReason(@StringRes val text: Int) {
    OFFLINE(R.string.outbox_not_sent_offline),
    UNKNOWN_HOST(R.string.outbox_not_sent_unknown_host),
    REFUSED(R.string.outbox_not_sent_refused),
    TIMEOUT(R.string.outbox_not_sent_timeout),
    PASSWORD(R.string.outbox_not_sent_password),
    CERTIFICATE(R.string.outbox_not_sent_certificate),
    ;

    companion object {
        fun of(error: String): NotSentReason? {
            val e = error.lowercase()
            return when {
                "enetunreach" in e || "network is unreachable" in e -> OFFLINE
                "unable to resolve host" in e || "no address associated with hostname" in e -> UNKNOWN_HOST
                "econnrefused" in e || "connection refused" in e -> REFUSED
                "etimedout" in e || "timed out" in e || "timeout" in e -> TIMEOUT
                "authentication failed" in e || "authenticationfailed" in e ||
                    Regex("""\b535\b""").containsMatchIn(e) -> PASSWORD
                "certificate" in e || "certpath" in e || "trust anchor" in e -> CERTIFICATE
                else -> null
            }
        }
    }
}
