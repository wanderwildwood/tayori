package com.fsck.k9.helper

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.Test

class DefaultTrustedSocketFactoryTest {
    @Test
    fun allowedProtocols_shouldDropSslv3AndTls10And11_andKeepTls12And13() {
        val supported = arrayOf("SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3")

        val allowed = DefaultTrustedSocketFactory.allowedProtocols(supported)

        assertThat(allowed.toList()).containsExactly("TLSv1.2", "TLSv1.3")
    }
}
