package com.fsck.k9.mail.ssl

import javax.net.ssl.SSLException

private const val MAX_CAUSES = 8

// Conscrypt names a protocol-version refusal in its message: the server's own alert, or its check on the
// server's choice of version.
private val TLS_VERSION_MARKERS = listOf("PROTOCOL_VERSION", "UNSUPPORTED_PROTOCOL", "NO_SUPPORTED_VERSIONS")

private fun Throwable.sslCauses(): Sequence<SSLException> =
    generateSequence(this) { it.cause }.take(MAX_CAUSES).filterIsInstance<SSLException>()

/** True when the failure is a TLS handshake failure of any kind. */
fun Throwable.isTlsFailure(): Boolean = sslCauses().any()

/**
 * True when the handshake was refused over the protocol version: the server speaks only TLS 1.0 or 1.1, which the
 * app no longer offers.
 */
fun Throwable.isTlsVersionRefused(): Boolean = sslCauses().any { e ->
    val message = e.message.orEmpty()
    TLS_VERSION_MARKERS.any { message.contains(it) }
}
