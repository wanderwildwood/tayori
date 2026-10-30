package com.fsck.k9.ui.messagelist.item

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class AccountLabelTest {
    private fun labels(vararg accounts: Triple<String, String, String>) = AccountLabels.forAccounts(accounts.toList())

    @Test
    fun `accounts on one domain with the address as name get their local parts`() {
        val result = labels(
            Triple("1", "davidwynn@mailbox.test", "davidwynn@mailbox.test"),
            Triple("2", "wander@mailbox.test", "wander@mailbox.test"),
            Triple("3", "lydia@mailbox.test", "lydia@mailbox.test"),
        )
        assertEquals(mapOf("1" to "davidwynn", "2" to "wander", "3" to "lydia"), result)
    }

    @Test
    fun `the same local part on different domains uses the whole address`() {
        val result = labels(Triple("1", "a@x.test", "a@x.test"), Triple("2", "a@y.test", "a@y.test"))
        assertEquals(mapOf("1" to "a@x.test", "2" to "a@y.test"), result)
    }

    @Test
    fun `a name the user gave is kept`() {
        val result = labels(Triple("1", "Work", "me@x.test"), Triple("2", "", "wander@x.test"))
        assertEquals(mapOf("1" to "Work", "2" to "wander"), result)
    }

    @Test
    fun `a long name is cut to fourteen characters with an ellipsis`() {
        val label = AccountLabels.single("Family mail account", "me@x.test")
        assertEquals(14, label.length)
        assertEquals('…', label.last())
    }

    @Test
    fun `a long address is shortened in the middle and keeps its domain`() {
        val result = labels(
            Triple("1", "", "averylongnamehere@x.test"),
            Triple("2", "", "averylongnamehere@y.test"),
        )
        assertEquals("averylongnam…@x.test", result["1"])
        assertEquals("averylongnam…@y.test", result["2"])
    }

    @Test
    fun `one account and an empty name give the part before the at sign`() {
        assertEquals(mapOf("1" to "me"), labels(Triple("1", "", "me@x.test")))
    }

    @Test
    fun `two accounts never print the same`() {
        val result = labels(Triple("1", "Home", "a@x.test"), Triple("2", "home", "b@x.test"), Triple("3", "", "a@x.test"))
        assertEquals(3, result.values.map { it.lowercase() }.toSet().size)
        assertNotEquals(result["1"], result["2"])
    }

    private val identities = listOf("" to "wander@wanderthe.dev", "" to "david@davidwynn.eco", "" to "project@other.test")

    @Test
    fun `identities on different domains are told apart by domain`() {
        val labels = AccountLabels.forIdentities(identities)
        assertEquals("wanderthe.dev", labels["wander@wanderthe.dev"])
        assertEquals("davidwynn.eco", labels["david@davidwynn.eco"])
    }

    @Test
    fun `identities on one domain are told apart by the part before the at sign`() {
        val labels = AccountLabels.forIdentities(listOf("" to "wander@x.test", "" to "david@x.test"))
        assertEquals(mapOf("wander@x.test" to "wander", "david@x.test" to "david"), labels)
    }

    @Test
    fun `a description the user set wins`() {
        val labels = AccountLabels.forIdentities(listOf("Studio" to "wander@x.test", "wander@y.test" to "wander@y.test"))
        assertEquals("Studio", labels["wander@x.test"])
        assertEquals("y.test", labels["wander@y.test"])
    }

    @Test
    fun `a message is badged by the identity it was sent to, by To or Cc`() {
        val labels = AccountLabels.forIdentities(identities)
        assertEquals("davidwynn.eco", AccountLabels.badge(listOf("david@davidwynn.eco"), labels, "acct"))
        assertEquals("other.test", AccountLabels.badge(listOf("someone@z.test", "project@other.test"), labels, "acct"))
    }

    @Test
    fun `no matching identity falls back to the account label`() {
        val labels = AccountLabels.forIdentities(identities)
        assertEquals("acct", AccountLabels.badge(listOf("stranger@z.test"), labels, "acct"))
        assertEquals("acct", AccountLabels.badge(emptyList(), labels, "acct"))
    }

    @Test
    fun `matching ignores case and plus tags`() {
        val labels = AccountLabels.forIdentities(identities)
        assertEquals("wanderthe.dev", AccountLabels.badge(listOf("Wander+news@WanderThe.dev"), labels, "acct"))
    }

    @Test
    fun `identities never print the same`() {
        val labels = AccountLabels.forIdentities(listOf("Home" to "a@x.test", "home" to "b@x.test"))
        assertEquals(2, labels.values.map { it.lowercase() }.toSet().size)
    }

    @Test
    fun `one identity gives one label`() {
        assertEquals(mapOf("me@x.test" to "x.test"), AccountLabels.forIdentities(listOf("" to "me@x.test")))
    }

    @Test
    fun `the description the app fills in is not taken for one the user set`() {
        val labels = AccountLabels.forIdentities(
            listOf("Initial identity" to "me@x.test", "" to "me@y.test"),
            ignoredDescriptions = setOf("Initial identity"),
        )
        assertEquals("x.test", labels["me@x.test"])
    }
}
