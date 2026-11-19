package com.fsck.k9.ui.messagelist

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotSentReasonTest {
    @Test
    fun `common errors read in plain words`() {
        assertEquals(
            NotSentReason.REFUSED,
            NotSentReason.of("ErrnoException: isConnected failed: ECONNREFUSED (Connection refused)"),
        )
        assertEquals(
            NotSentReason.UNKNOWN_HOST,
            NotSentReason.of("Unable to resolve host \"mail.example.test\": No address associated with hostname"),
        )
        assertEquals(NotSentReason.OFFLINE, NotSentReason.of("connect failed: ENETUNREACH (Network is unreachable)"))
        assertEquals(NotSentReason.TIMEOUT, NotSentReason.of("Read timed out"))
        assertEquals(
            NotSentReason.TIMEOUT,
            NotSentReason.of("failed to connect to /10.0.2.2 (port 3465) after 30000ms: isConnected failed: ETIMEDOUT"),
        )
        assertEquals(NotSentReason.PASSWORD, NotSentReason.of("Negative SMTP reply: 535 5.7.8 Authentication failed"))
        assertEquals(
            NotSentReason.CERTIFICATE,
            NotSentReason.of("java.security.cert.CertPathValidatorException: Trust anchor for certification path not found."),
        )
    }

    @Test
    fun `anything else is shown as the server said it`() {
        assertNull(NotSentReason.of("554 5.7.1 Message rejected: sender address not verified"))
        assertNull(NotSentReason.of("Message too large: 26214400 bytes"))
    }
}
