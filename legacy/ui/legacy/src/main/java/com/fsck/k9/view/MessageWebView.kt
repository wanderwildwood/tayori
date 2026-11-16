package com.fsck.k9.view

import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.ActionMode
import android.webkit.WebView
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import app.k9mail.feature.account.common.ui.house.DarkMode
import com.fsck.k9.core.BuildConfig
import com.fsck.k9.mailstore.AttachmentResolver
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.thunderbird.core.android.common.view.showInDarkMode
import net.thunderbird.core.android.common.view.showInLightMode
import net.thunderbird.core.android.webkit.ThunderbirdWebViewSettings
import net.thunderbird.legacy.logging.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageWebView : WebView, KoinComponent, ThunderbirdWebViewSettings {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    private val webViewClientFactory: WebViewClientFactory by inject()

    // Every view of a mail, the printed copy too: no script, no files, no content of this or another app, and no
    // network until the reader allows the pictures. A picture inside the mail comes through K9WebViewClient (cid:).
    init {
        with(settings) {
            javaScriptEnabled = false
            allowFileAccess = false
            allowContentAccess = false
        }
        blockNetworkData(true)
    }

    // The Kompakt's selection bar keeps two items and drops the rest; SelectionMenu gives them back behind a ⋮.
    override fun startActionMode(callback: ActionMode.Callback, type: Int): ActionMode? =
        super.startActionMode(SelectionMenu.fold(this, callback), type)

    override var loadWithOverviewMode: Boolean
        get() = settings.loadWithOverviewMode
        set(value) {
            settings.loadWithOverviewMode = value
        }

    override fun blockNetworkData(shouldBlockNetworkData: Boolean) {
        // Images with content: URIs will not be blocked, nor will network images that are already in the WebView cache.
        try {
            settings.blockNetworkLoads = shouldBlockNetworkData
        } catch (e: SecurityException) {
            Log.e(e, "Failed to unblock network loads. Missing INTERNET permission?")
        }
    }

    override fun configure(config: WebViewConfig) {
        isVerticalScrollBarEnabled = false
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        isLongClickable = true

        // Do not force a view-specific hardware layer. Long messages can make this WebView taller than the
        // device's maximum GPU texture size, causing WebView to crash while creating the layer.
        setLayerType(LAYER_TYPE_NONE, null)

        configureDarkLightMode(this, config)

        with(settings) {
            setSupportZoom(true)
            builtInZoomControls = true
            useWideViewPort = true

            if (config.autoFitWidth) {
                loadWithOverviewMode = true
            }

            disableDisplayZoomControls()

            javaScriptEnabled = false
            loadsImagesAutomatically = true

            overScrollMode = OVER_SCROLL_NEVER

            textZoom = config.textZoom

            // The system's font size times the app's own text size (see TextScaleContextWrapper), so the body grows
            // with the rest of the screen.
            val fontScale = context.resources.configuration.fontScale
            settings.textZoom = (settings.textZoom * fontScale).roundToInt()
        }

        // Disable network images by default. This is overridden by preferences.
        blockNetworkData(true)
        setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }

    private fun configureDarkLightMode(
        webView: WebView,
        config: WebViewConfig,
    ) {
        if (config.useDarkMode) {
            webView.showInDarkMode()
        } else {
            webView.showInLightMode()
        }
    }

    private fun disableDisplayZoomControls() {
        val packageManager = context.packageManager
        val supportsMultiTouch = packageManager.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH) ||
            packageManager.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT)

        settings.displayZoomControls = !supportsMultiTouch
    }

    fun displayHtmlContentWithInlineAttachments(
        htmlText: String,
        attachmentResolver: AttachmentResolver?,
        onPageFinishedListener: OnPageFinishedListener?,
    ) {
        setWebViewClient(attachmentResolver, onPageFinishedListener)
        setHtmlContent(htmlText)
    }

    private fun setWebViewClient(
        attachmentResolver: AttachmentResolver?,
        onPageFinishedListener: OnPageFinishedListener?,
    ) {
        val webViewClient = webViewClientFactory.create(attachmentResolver, onPageFinishedListener)
        setWebViewClient(webViewClient)
    }

    private fun setHtmlContent(htmlText: String) {
        shownHtml = htmlText
        shownDark = DarkMode.on
        val page = if (shownDark) htmlText + PICTURES_KEEP_COLORS else htmlText
        loadDataWithBaseURL("about:blank", page, "text/html", "utf-8", null)
        resumeTimers()
    }

    // Dark mode draws the whole window through an inverting filter, the mail with it. While it is on, the mail's
    // pictures are inverted once more by the page itself, so they show their real colors and the text stays white
    // on black. Switched while a mail is open, the mail is shown again.
    private var shownHtml: String? = null
    private var shownDark = false
    private var darkWatch: Job? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        darkWatch?.cancel()
        darkWatch = findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
            snapshotFlow { DarkMode.on }.collect { dark ->
                val html = shownHtml
                if (dark != shownDark && html != null) setHtmlContent(html)
            }
        }
    }

    override fun onDetachedFromWindow() {
        darkWatch?.cancel()
        darkWatch = null
        super.onDetachedFromWindow()
    }

    private companion object {
        // Appended after the mail, so it comes last and no doctype is pushed out of first place. A picture is drawn
        // on white, as on a light page, so a logo with see-through parts keeps its dark lines visible.
        const val PICTURES_KEEP_COLORS =
            "<style>img, video, canvas, svg, embed, object " +
                "{ filter: invert(1) !important; background-color: #fff; }</style>"
    }

    fun interface OnPageFinishedListener {
        fun onPageFinished()
    }
}
