package com.fsck.k9.ui.messagesource

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class MessageSourceViewModelTest {
    @Test
    fun `one row per line, with the blank line between headers and body kept`() {
        val rows = MessageSourceViewModel.splitIntoRows("From: a@example.test\r\nSubject: Hi\r\n\r\nHello\r\n")

        assertThat(rows).isEqualTo(listOf("From: a@example.test", "Subject: Hi", "", "Hello", ""))
    }

    @Test
    fun `bare line feeds split too`() {
        assertThat(MessageSourceViewModel.splitIntoRows("a\nb")).isEqualTo(listOf("a", "b"))
    }

    @Test
    fun `a very long line continues on following rows and loses nothing`() {
        val max = MessageSourceViewModel.MAX_ROW_LENGTH
        val line = "x".repeat(max * 2 + 5)

        val rows = MessageSourceViewModel.splitIntoRows(line)

        assertThat(rows.map { it.length }).isEqualTo(listOf(max, max, 5))
        assertThat(rows.joinToString("")).isEqualTo(line)
    }
}
