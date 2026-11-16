package app.k9mail.feature.account.server.validation.ui

import app.k9mail.feature.account.server.validation.R
import app.k9mail.feature.account.server.validation.ui.ServerValidationContract.Error
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ServerValidationStringMapperTest {
    private val resources = RuntimeEnvironment.getApplication().resources

    @Test
    fun `server offering only an old TLS version gets a plain explanation`() {
        val exception = SSLHandshakeException(
            "Read error: ssl=0x1: Failure in SSL library, usually a protocol error\n" +
                "error:10000410:SSL routines:OPENSSL_internal:TLSV1_ALERT_PROTOCOL_VERSION",
        )

        val text = Error.NetworkError(exception).toResourceString(resources)

        assertThat(text).isEqualTo(resources.getString(R.string.account_server_validation_error_old_tls))
    }

    @Test
    fun `other TLS failure is called a secure connection failure`() {
        val exception = SSLHandshakeException("Connection closed by peer")

        val text = Error.NetworkError(exception).toResourceString(resources)

        assertThat(text).startsWith(resources.getString(R.string.account_server_validation_error_secure_connection))
    }

    @Test
    fun `plain network failure stays a network error`() {
        val text = Error.NetworkError(IOException("Connection refused")).toResourceString(resources)

        assertThat(text).startsWith(resources.getString(R.string.account_server_validation_error_network))
    }
}
