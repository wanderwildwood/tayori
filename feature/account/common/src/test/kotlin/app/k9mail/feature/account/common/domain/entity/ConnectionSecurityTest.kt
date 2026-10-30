package app.k9mail.feature.account.common.domain.entity

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class ConnectionSecurityTest {

    @Test
    fun `none is not offered when something safe is chosen`() {
        assertThat(ConnectionSecurity.offered(ConnectionSecurity.TLS))
            .isEqualTo(listOf(ConnectionSecurity.StartTLS, ConnectionSecurity.TLS))
        assertThat(ConnectionSecurity.offered(ConnectionSecurity.StartTLS))
            .isEqualTo(listOf(ConnectionSecurity.StartTLS, ConnectionSecurity.TLS))
    }

    @Test
    fun `none stays on the list of an account that already has it`() {
        assertThat(ConnectionSecurity.offered(ConnectionSecurity.None)).isEqualTo(ConnectionSecurity.all())
    }

    @Test
    fun `should provide right default smtp port`() {
        val connectionSecurities = ConnectionSecurity.all()

        for (security in connectionSecurities) {
            val port = security.toSmtpDefaultPort()

            assertThat(port).isEqualTo(
                when (security) {
                    ConnectionSecurity.None -> 587L
                    ConnectionSecurity.StartTLS -> 587L
                    ConnectionSecurity.TLS -> 465L
                },
            )
        }
    }

    @Test
    fun `should provide right default imap port`() {
        val connectionSecurities = ConnectionSecurity.all()

        for (security in connectionSecurities) {
            val port = security.toImapDefaultPort()

            assertThat(port).isEqualTo(
                when (security) {
                    ConnectionSecurity.None -> 143L
                    ConnectionSecurity.StartTLS -> 143L
                    ConnectionSecurity.TLS -> 993L
                },
            )
        }
    }

    @Test
    fun `should provide right default pop3 port`() {
        val connectionSecurities = ConnectionSecurity.all()

        for (security in connectionSecurities) {
            val port = security.toPop3DefaultPort()

            assertThat(port).isEqualTo(
                when (security) {
                    ConnectionSecurity.None -> 110L
                    ConnectionSecurity.StartTLS -> 110L
                    ConnectionSecurity.TLS -> 995L
                },
            )
        }
    }

    @Test
    fun `should map all MailConnectionSecurities to ConnectionSecurities`() {
        val securities = MailConnectionSecurity.entries

        for (security in securities) {
            val connectionSecurity = security.toConnectionSecurity()

            assertThat(connectionSecurity).isEqualTo(
                when (security) {
                    MailConnectionSecurity.NONE -> ConnectionSecurity.None
                    MailConnectionSecurity.STARTTLS_REQUIRED -> ConnectionSecurity.StartTLS
                    MailConnectionSecurity.SSL_TLS_REQUIRED -> ConnectionSecurity.TLS
                },
            )
        }
    }

    @Test
    fun `should map to all ConnectionSecurities to MailConnectionSecurities`() {
        val connectionSecurities = ConnectionSecurity.entries

        for (security in connectionSecurities) {
            val mailConnectionSecurity = security.toMailConnectionSecurity()

            assertThat(mailConnectionSecurity).isEqualTo(
                when (security) {
                    ConnectionSecurity.None -> MailConnectionSecurity.NONE
                    ConnectionSecurity.StartTLS -> MailConnectionSecurity.STARTTLS_REQUIRED
                    ConnectionSecurity.TLS -> MailConnectionSecurity.SSL_TLS_REQUIRED
                },
            )
        }
    }
}
