package com.fsck.k9.ui.messageview

import org.junit.Test
import org.junit.Assert.assertEquals

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
}
