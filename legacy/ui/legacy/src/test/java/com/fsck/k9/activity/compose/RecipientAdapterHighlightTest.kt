package com.fsck.k9.activity.compose

import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import com.fsck.k9.K9RobolectricTest
import org.junit.Test

class RecipientAdapterHighlightTest : K9RobolectricTest() {

    @Test
    fun `typed letters are drawn in the given color and in bold`() {
        val text = RecipientAdapter.highlightText("Ada Whitlock", "ada", Color.BLACK)

        val colors = text.getSpans(0, text.length, ForegroundColorSpan::class.java)
        val bold = text.getSpans(0, text.length, StyleSpan::class.java)

        assertThat(colors.map { Triple(it.foregroundColor, text.getSpanStart(it), text.getSpanEnd(it)) })
            .containsExactly(Triple(Color.BLACK, 0, 3))
        assertThat(bold.map { Triple(it.style, text.getSpanStart(it), text.getSpanEnd(it)) })
            .containsExactly(Triple(Typeface.BOLD, 0, 3))
    }

    @Test
    fun `every match in an address is marked`() {
        val text = RecipientAdapter.highlightText("ada@ada.example", "ADA", Color.BLACK)

        val bold = text.getSpans(0, text.length, StyleSpan::class.java)

        assertThat(bold.map { text.getSpanStart(it) to text.getSpanEnd(it) }.sortedBy { it.first })
            .containsExactly(0 to 3, 4 to 7)
    }

    @Test
    fun `nothing typed marks nothing`() {
        val text = RecipientAdapter.highlightText("Tomas Reyes", null, Color.BLACK)

        assertThat(text.getSpans(0, text.length, Any::class.java).toList()).isEmpty()
    }
}
