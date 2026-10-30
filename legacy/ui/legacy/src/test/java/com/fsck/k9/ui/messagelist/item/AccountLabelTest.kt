package com.fsck.k9.ui.messagelist.item

import kotlin.test.Test
import kotlin.test.assertEquals

class AccountLabelTest {
    @Test
    fun `a short name is used as it is`() {
        assertEquals("Work", accountLabel("Work", "me@example.test"))
    }

    @Test
    fun `a name that is the address falls back to the domain`() {
        assertEquals("example.test", accountLabel("me@example.test", "me@example.test"))
        assertEquals("example.test", accountLabel("", "me@example.test"))
    }

    @Test
    fun `a long name is cut to twelve characters with an ellipsis`() {
        assertEquals(12, accountLabel("Family mail account", "me@example.test").length)
        assertEquals('…', accountLabel("Family mail account", "me@example.test").last())
    }
}
