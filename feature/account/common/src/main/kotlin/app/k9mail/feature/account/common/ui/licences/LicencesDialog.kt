package app.k9mail.feature.account.common.ui.licences

import android.content.Context
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import app.k9mail.feature.account.common.R
import app.k9mail.feature.account.common.ui.link.openLink
import net.thunderbird.components.ui.bolt.atom.Surface
import net.thunderbird.components.ui.bolt.atom.button.ButtonOutlined
import net.thunderbird.components.ui.bolt.atom.text.TextBodyLarge
import net.thunderbird.components.ui.bolt.atom.text.TextLabelSmall
import net.thunderbird.components.ui.bolt.theme.BoltTheme

/** One piece of other people's work that ships inside the app, and the licence it comes under. */
data class Library(val name: String, val by: String, val licence: String, val link: String)

/** A licence text bundled as a raw resource, with the heading it is shown under. */
data class LicenceText(@StringRes val title: Int, @RawRes val raw: Int)

/** The address of the app's own source code. */
const val SOURCE_URL = "https://github.com/wanderwildwood/tayori"

/**
 * What the APK carries that is not this app's own code. Licences are the ones each project
 * declares itself; the full text of each distinct one follows in [LICENCE_TEXTS].
 */
val LIBRARIES: List<Library> = listOf(
    Library("Thunderbird for Android and K-9 Mail", "MZLA Technologies and the K-9 Mail contributors", "Apache-2.0", "https://github.com/thunderbird/thunderbird-android"),
    Library("Lato typeface", "Łukasz Dziedzic", "OFL-1.1", "https://www.latofonts.com"),
    Library("AndroidX, Jetpack Compose, Material components", "Google", "Apache-2.0", "https://developer.android.com/jetpack"),
    Library("Kotlin, kotlinx.coroutines, kotlinx.serialization, kotlinx.datetime", "JetBrains", "Apache-2.0", "https://kotlinlang.org"),
    Library("OkHttp and Okio", "Square", "Apache-2.0", "https://square.github.io/okhttp/"),
    Library("Moshi", "Square", "Apache-2.0", "https://github.com/square/moshi"),
    Library("Timber", "Jake Wharton", "Apache-2.0", "https://github.com/JakeWharton/timber"),
    Library("Koin", "Kotzilla and contributors", "Apache-2.0", "https://insert-koin.io"),
    Library("Apache MIME4J and Apache HttpClient", "The Apache Software Foundation", "Apache-2.0", "https://james.apache.org/mime4j/"),
    Library("Apache Commons IO", "The Apache Software Foundation", "Apache-2.0", "https://commons.apache.org/proper/commons-io/"),
    Library("jsoup", "Jonathan Hedley", "MIT", "https://jsoup.org"),
    Library("ksoup", "Fleeksoft", "Apache-2.0", "https://github.com/fleeksoft/ksoup"),
    Library("jutf7", "Jaap Beetstra", "MIT", "https://sourceforge.net/projects/jutf7/"),
    Library("JZlib", "JCraft", "BSD-3-Clause", "http://www.jcraft.com/jzlib/"),
    Library("Glide", "Bump Technologies and Google", "BSD-2-Clause, MIT and Apache-2.0", "https://github.com/bumptech/glide"),
    Library("AppAuth for Android", "OpenID Foundation", "Apache-2.0", "https://github.com/openid/AppAuth-Android"),
    Library("ZXing", "ZXing authors", "Apache-2.0", "https://github.com/zxing/zxing"),
    Library("MiniDNS", "The MiniDNS authors", "Apache-2.0", "https://github.com/MiniDNS/minidns"),
    Library("Coil", "Coil contributors", "Apache-2.0", "https://coil-kt.github.io/coil/"),
    Library("Guava, Dagger, Flexbox", "Google", "Apache-2.0", "https://github.com/google/guava"),
    Library("Apache Commons Lang and Text", "The Apache Software Foundation", "Apache-2.0", "https://commons.apache.org/"),
    Library("Landscapist", "Jaewoong Eum", "Apache-2.0", "https://github.com/skydoves/landscapist"),
    Library("Stately", "Touchlab", "Apache-2.0", "https://github.com/touchlab/Stately"),
    Library("uri-kmp", "Eygraber", "Apache-2.0", "https://github.com/eygraber/uri-kmp"),
    Library("SLF4J API and Checker Framework annotations", "QOS.ch and the Checker Framework authors", "MIT", "https://www.slf4j.org"),
    Library("OpenPGP API library", "Dominik Schürmann and the OpenKeychain authors", "Apache-2.0", "https://github.com/open-keychain/openpgp-api"),
    Library("CircleImageView, FastAdapter, PreferenceX, Safe Content Resolver", "their authors", "Apache-2.0", "https://github.com/hdodenhof/CircleImageView"),
    Library("SearchPreference", "ByteHamster", "MIT", "https://github.com/ByteHamster/SearchPreference"),
)

/** Each distinct licence text once, in the order the names above first use them. */
val LICENCE_TEXTS: List<LicenceText> = listOf(
    LicenceText(R.string.account_common_licences_gpl, R.raw.account_common_gpl_3_0),
    LicenceText(R.string.account_common_licences_apache, R.raw.account_common_apache_2_0),
    LicenceText(R.string.account_common_licences_mit, R.raw.account_common_mit),
    LicenceText(R.string.account_common_licences_ofl, R.raw.account_common_ofl_1_1),
)

/**
 * A licence text as paragraphs: blocks split at blank lines, each joined into one row. No word is
 * dropped or repeated by it.
 */
fun licenceParagraphs(text: String): List<String> =
    text
        .replace("\r\n", "\n")
        .split(Regex("\n[ \t]*\n"))
        .map { block -> block.lines().joinToString(" ") { it.trim() }.trim() }
        .filter { it.isNotEmpty() }

/**
 * The Licences page, reachable from both About dialogs (before any account exists, and in
 * Settings): the source link, this app's licence, what it is built with, and each licence's
 * full text once.
 */
@Composable
fun LicencesDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val texts = remember { LICENCE_TEXTS.map { it to readParagraphs(context, it.raw) } }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        }
        Surface(
            color = BoltTheme.colors.surface,
            contentColor = BoltTheme.colors.onSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .border(2.dp, BoltTheme.colors.onSurface, RoundedCornerShape(12.dp)),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                TextBodyLarge(
                    text = stringResource(R.string.account_common_licences_title),
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item { Heading(stringResource(R.string.account_common_licences_source_head)) }
                    item { Link(SOURCE_URL.removePrefix("https://"), SOURCE_URL) }
                    item { Heading(stringResource(R.string.account_common_licences_licence_head)) }
                    item { TextLabelSmall(text = stringResource(R.string.account_common_licences_licence)) }
                    item { Heading(stringResource(R.string.account_common_licences_built_head)) }
                    LIBRARIES.forEach { library ->
                        item {
                            Column {
                                TextLabelSmall(text = "${library.name}, ${library.by}: ${library.licence}")
                                Link(library.link.removePrefix("https://").removePrefix("http://"), library.link)
                            }
                        }
                    }
                    texts.forEach { (text, paragraphs) ->
                        item { Heading(stringResource(text.title)) }
                        paragraphs.forEach { paragraph -> item { TextLabelSmall(text = paragraph) } }
                    }
                }
                Spacer(Modifier.height(14.dp))
                ButtonOutlined(
                    text = stringResource(R.string.account_common_licences_back),
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                )
            }
        }
    }
}

private fun readParagraphs(context: Context, @RawRes raw: Int): List<String> =
    runCatching {
        licenceParagraphs(context.resources.openRawResource(raw).bufferedReader().use { it.readText() })
    }.getOrDefault(emptyList())

@Composable
private fun Heading(text: String) {
    TextLabelSmall(
        text = buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text) } },
        modifier = Modifier.padding(top = 10.dp),
    )
}

/** An address that opens in a browser, on a 48 dp row; a phone with none says so plainly. */
@Composable
private fun Link(label: String, address: String) {
    val context = LocalContext.current
    TextLabelSmall(
        text = buildAnnotatedString {
            withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(label) }
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable {
                openLink(context, address, context.getString(R.string.account_common_no_browser_address, label))
            },
    )
}
