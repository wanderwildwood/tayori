package com.fsck.k9.ui.messagesource

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.ui.R
import com.fsck.k9.ui.base.BaseActivity

/**
 * Temporary Activity used until the fragment can be displayed in the main activity.
 */
class MessageSourceActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLayout(R.layout.message_source_activity)
        setTitle(R.string.show_headers_action)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            addMessageSourceFragment()
        }
    }

    private fun addMessageSourceFragment() {
        val messageReferenceString = intent.getStringExtra(ARG_REFERENCE) ?: error("Missing argument $ARG_REFERENCE")
        val messageReference = MessageReference.parse(messageReferenceString)
            ?: error("Invalid message reference: $messageReferenceString")

        val fragment = MessageSourceFragment.newInstance(messageReference)
        supportFragmentManager.commit {
            add(R.id.message_source_fragment, fragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val ARG_REFERENCE = "reference"

        fun createLaunchIntent(context: Context, messageReference: MessageReference): Intent {
            return Intent(context, MessageSourceActivity::class.java).apply {
                putExtra(ARG_REFERENCE, messageReference.toIdentityString())
            }
        }
    }
}
