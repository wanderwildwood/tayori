package com.fsck.k9.mail.ssl

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import kotlin.test.Test
import net.thunderbird.core.common.exception.MessagingException

class TlsVersionRefusedTest {
    private val versionAlert = SSLHandshakeException(
        "Read error: ssl=0x1: Failure in SSL library, usually a protocol error\n" +
            "error:10000410:SSL routines:OPENSSL_internal:TLSV1_ALERT_PROTOCOL_VERSION",
    )

    @Test
    fun `protocol version alert is a refused TLS version`() {
        assertThat(versionAlert.isTlsVersionRefused()).isTrue()
    }

    @Test
    fun `wrapped protocol version alert is a refused TLS version`() {
        assertThat(MessagingException(versionAlert).isTlsVersionRefused()).isTrue()
    }

    @Test
    fun `other handshake failure is a TLS failure but not a refused version`() {
        val closed = SSLHandshakeException("Connection closed by peer")

        assertThat(closed.isTlsFailure()).isTrue()
        assertThat(closed.isTlsVersionRefused()).isFalse()
    }

    @Test
    fun `plain network failure is neither`() {
        val refused = IOException("Connection refused")

        assertThat(refused.isTlsFailure()).isFalse()
        assertThat(refused.isTlsVersionRefused()).isFalse()
    }
}
