package com.fsck.k9.ui.messageview

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RemoteImageTest {
    @Test
    fun keepsAnOrdinaryName() {
        assertEquals("heron.jpg", RemoteImage.fileName("heron.jpg"))
    }

    @Test
    fun removesBadCharacters() {
        assertEquals("a_b_c.png", RemoteImage.fileName("a/b:c.png"))
    }

    @Test
    fun fallsBackToImage() {
        assertEquals("image", RemoteImage.fileName(null))
        assertEquals("image", RemoteImage.fileName("  "))
        assertEquals("image", RemoteImage.fileName(".."))
    }

    @Test
    fun longNamesAreCut() {
        assertEquals(80, RemoteImage.fileName("a".repeat(200)).length)
    }

    @Test
    fun copiesAPictureWithinTheLimit() {
        val bytes = ByteArray(20_000) { it.toByte() }
        val out = ByteArrayOutputStream()
        RemoteImage.copyCapped(ByteArrayInputStream(bytes), out, limit = 20_000)
        assertArrayEquals(bytes, out.toByteArray())
    }

    @Test
    fun aPictureOverTheLimitIsRefused() {
        assertThrows(IOException::class.java) {
            RemoteImage.copyCapped(ByteArrayInputStream(ByteArray(20_001)), ByteArrayOutputStream(), limit = 20_000)
        }
    }

    @Test
    fun aDownloadCutOffHalfWayFails() {
        val cutOff = object : InputStream() {
            private var sent = 0
            override fun read(): Int = if (sent++ < 100) 1 else throw IOException("connection reset")
        }
        assertThrows(IOException::class.java) {
            RemoteImage.copyCapped(cutOff, ByteArrayOutputStream())
        }
    }
}
