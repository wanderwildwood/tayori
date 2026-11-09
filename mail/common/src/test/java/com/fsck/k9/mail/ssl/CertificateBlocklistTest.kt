package com.fsck.k9.mail.ssl

import assertk.assertFailure
import assertk.assertions.isInstanceOf
import com.fsck.k9.mail.ssl.tls.CertificateBlocklist
import com.fsck.k9.mail.ssl.tls.CompromisedCertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import org.junit.Test

class CertificateBlocklistTest {

    @Test
    fun `safe certificate hash is not blocked`() {
        CertificateBlocklist.checkCertificateBlockList(arrayOf(getGoodHostCertificate()))
    }

    @Test
    fun `known unsafe certificate hash is blocked`() {
        val badCert = getBadHostCertificate()

        assertFailure {
            CertificateBlocklist.checkCertificateBlockList(arrayOf(badCert))
        }.isInstanceOf<CompromisedCertificateException>()
    }

    @Test
    fun `known unsafe certificate hash is blocked in middle of chain`() {
        val goodCert = getGoodHostCertificate()
        val badCert = getBadHostCertificate()

        assertFailure {
            CertificateBlocklist.checkCertificateBlockList(arrayOf(goodCert, badCert, goodCert))
        }.isInstanceOf<CompromisedCertificateException>()
    }

    private fun getGoodHostCertificate(): X509Certificate = readCertificate("/certificates/goodHost.pem")

    private fun getBadHostCertificate(): X509Certificate =
        readCertificate("/certificates/cve-2026-11943-bad-cert.pem")

    private fun readCertificate(resourceName: String): X509Certificate {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        return CertificateBlocklistTest::class.java.getResourceAsStream(resourceName)!!.use { inputStream ->
            certificateFactory.generateCertificate(inputStream) as X509Certificate
        }
    }
}
