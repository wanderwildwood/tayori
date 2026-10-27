package com.fsck.k9.ui.messagesource

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.k9mail.legacy.message.controller.MessageReference
import com.fsck.k9.ui.R
import com.fsck.k9.ui.base.extensions.withArguments
import com.fsck.k9.ui.base.loader.observeLoading
import com.fsck.k9.view.turnsAPageOnSwipe
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The message as it was received, in a monospace list that turns a page on a swipe.
 */
class MessageSourceFragment : Fragment() {
    private val viewModel: MessageSourceViewModel by viewModel()
    private var source: MessageSource? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.message_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageReferenceString = requireArguments().getString(ARG_REFERENCE)
            ?: error("Missing argument $ARG_REFERENCE")
        val messageReference = MessageReference.parse(messageReferenceString)
            ?: error("Invalid message reference: $messageReferenceString")

        val list = view.findViewById<RecyclerView>(R.id.message_source_data)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.itemAnimator = null
        list.turnsAPageOnSwipe()

        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.message_source_menu, menu)
                }

                override fun onPrepareMenu(menu: Menu) {
                    menu.findItem(R.id.copy_source)?.isVisible = source != null
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return if (menuItem.itemId == R.id.copy_source) {
                        copySource()
                        true
                    } else {
                        false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )

        viewModel.loadSource(messageReference).observeLoading(
            owner = this,
            loadingView = view.findViewById(R.id.message_source_loading),
            errorView = view.findViewById(R.id.message_source_error),
            dataView = list,
        ) { loaded ->
            source = loaded
            val rows = if (loaded.isShortened) {
                loaded.rows + getString(R.string.message_source_shortened, loaded.shownKilobytes, loaded.totalKilobytes)
            } else {
                loaded.rows
            }
            list.adapter = SourceAdapter(rows)
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun copySource() {
        val text = source?.text ?: return
        val clipboard = requireContext().getSystemService<ClipboardManager>() ?: return
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.show_headers_action), text))
        Toast.makeText(requireContext(), R.string.message_source_copied, Toast.LENGTH_SHORT).show()
    }

    private class SourceAdapter(private val rows: List<String>) : RecyclerView.Adapter<SourceAdapter.Holder>() {
        class Holder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val textView = TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                typeface = Typeface.MONOSPACE
                setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, TEXT_SIZE_SP)
                setTextColor(android.graphics.Color.BLACK)
                includeFontPadding = false
            }
            return Holder(textView)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            // An empty row still has to be one line tall.
            holder.textView.text = rows[position].ifEmpty { " " }
        }

        override fun getItemCount() = rows.size
    }

    companion object {
        private const val ARG_REFERENCE = "reference"
        private const val TEXT_SIZE_SP = 14f

        fun newInstance(reference: MessageReference): MessageSourceFragment {
            return MessageSourceFragment().withArguments(
                ARG_REFERENCE to reference.toIdentityString(),
            )
        }
    }
}
