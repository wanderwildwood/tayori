package com.fsck.k9.ui.messagelist.item

/**
 * What the Unified Inbox prints beside the date to say which account a row is from. Each account
 * gets the shortest label that no other account has: the name the user gave it (when that is not
 * just the address), else the part of the address before the @, else the whole address.
 */
object AccountLabels {
    private const val MAX_LABEL = 14
    private const val MAX_ADDRESS = 20

    /** For one account on its own (no others to be told apart from). */
    fun single(name: String, email: String): String = candidates(name, email).first()

    /** [accounts] are (id, name, address); the result maps each id to its label. */
    fun forAccounts(accounts: List<Triple<String, String, String>>): Map<String, String> =
        resolve(accounts.associate { (id, name, email) -> id to candidates(name, email) })

    /**
     * The labels of one account's identities, by normalized address. [identities] are (description,
     * address). A description the user set wins (not one the app filled in, see [ignoredDescriptions]); else the domain when the identities' domains all
     * differ, else the part before the @, else the whole address.
     */
    fun forIdentities(
        identities: List<Pair<String, String>>,
        ignoredDescriptions: Set<String> = emptySet(),
    ): Map<String, String> {
        val domains = identities.map { it.second.substringAfter('@', "").lowercase() }
        val domainsDiffer = domains.toSet().size == domains.size
        val options = identities.associate { (description, email) ->
            val key = normalize(email)
            val list = buildList {
                val d = description.trim()
                if (d.isNotEmpty() && !d.equals(email.trim(), ignoreCase = true) && d !in ignoredDescriptions) add(cut(d))
                val local = email.substringBefore('@').trim()
                val domain = email.substringAfter('@', "").trim()
                val first = if (domainsDiffer && domain.isNotEmpty()) cut(domain) else cut(local)
                val second = if (domainsDiffer && domain.isNotEmpty()) cut(local) else cut(domain)
                if (first.isNotEmpty()) add(first)
                if (second.isNotEmpty()) add(second)
                add(address(email.trim()))
            }.distinct()
            key to list
        }
        return resolve(options)
    }

    /** The badge for a message: its identity's label when one of [recipients] matches, else [accountLabel]. */
    fun badge(recipients: List<String>, identityLabels: Map<String, String>, accountLabel: String): String =
        recipients.firstNotNullOfOrNull { identityLabels[normalize(it)] } ?: accountLabel

    /** Lower case, and name+tag@domain read as name@domain. */
    fun normalize(address: String): String {
        val trimmed = address.trim().lowercase()
        val local = trimmed.substringBefore('@')
        val domain = trimmed.substringAfter('@', "")
        return local.substringBefore('+') + (if ('@' in trimmed) "@$domain" else "")
    }

    private fun resolve(options: Map<String, List<String>>): Map<String, String> {
        val ids = options.keys.toList()
        val step = ids.associateWith { 0 }.toMutableMap()
        fun label(id: String) = options.getValue(id)[step.getValue(id)]

        // While two entries print the same, those that still have a longer label move on to it.
        while (true) {
            val clashing = ids.groupBy { label(it).lowercase() }.values.filter { it.size > 1 }
            val movable = clashing.flatten().filter { step.getValue(it) < options.getValue(it).lastIndex }
            if (movable.isEmpty()) break
            movable.forEach { step[it] = step.getValue(it) + 1 }
        }

        // Still the same (identical addresses): number the later ones so no two read alike.
        val seen = mutableMapOf<String, Int>()
        return ids.associateWith { id ->
            val text = label(id)
            val count = seen.merge(text.lowercase(), 1, Int::plus) ?: 1
            if (count > 1) "$text $count" else text
        }
    }

    private fun candidates(name: String, email: String): List<String> = buildList {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !trimmed.equals(email.trim(), ignoreCase = true)) add(cut(trimmed))
        val local = email.substringBefore('@').trim()
        if (local.isNotEmpty()) add(cut(local))
        add(address(email.trim()))
    }.distinct()

    private fun cut(text: String) = if (text.length > MAX_LABEL) text.take(MAX_LABEL - 1) + "\u2026" else text

    /** The whole address, shortened in the middle (the start of the name kept, the domain kept). */
    private fun address(email: String): String {
        if (email.length <= MAX_ADDRESS) return email
        val domain = email.substringAfter('@', "")
        val local = email.substringBefore('@')
        val room = (MAX_ADDRESS - domain.length - 2).coerceAtLeast(3)
        return local.take(room) + "\u2026@" + domain
    }
}
