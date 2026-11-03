package app.k9mail.feature.account.common.ui.house

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import app.k9mail.feature.account.common.R
import com.mudita.mmd.ThemeMMD

/** The address of the app's own source code. */
const val SOURCE_URL = "https://github.com/wanderwildwood/tayori"

/**
 * What the APK carries that is not this app's own code. Licences are the ones each project
 * declares itself; the full text of each distinct one is on its own page.
 */
val LICENCE_COMPONENTS: List<LicenceComponent> = listOf(
    LicenceComponent("Thunderbird for Android and K-9 Mail", "MZLA Technologies and the K-9 Mail contributors", "Apache-2.0", "https://github.com/thunderbird/thunderbird-android"),
    LicenceComponent("Lato typeface", "Łukasz Dziedzic", "OFL-1.1", "https://www.latofonts.com"),
    LicenceComponent("AndroidX, Jetpack Compose, Material components", "Google", "Apache-2.0", "https://developer.android.com/jetpack"),
    LicenceComponent("Kotlin, kotlinx.coroutines, kotlinx.serialization, kotlinx.datetime", "JetBrains", "Apache-2.0", "https://kotlinlang.org"),
    LicenceComponent("OkHttp and Okio", "Square", "Apache-2.0", "https://square.github.io/okhttp/"),
    LicenceComponent("Moshi", "Square", "Apache-2.0", "https://github.com/square/moshi"),
    LicenceComponent("Timber", "Jake Wharton", "Apache-2.0", "https://github.com/JakeWharton/timber"),
    LicenceComponent("Koin", "Kotzilla and contributors", "Apache-2.0", "https://insert-koin.io"),
    LicenceComponent("Apache MIME4J and Apache HttpClient", "The Apache Software Foundation", "Apache-2.0", "https://james.apache.org/mime4j/"),
    LicenceComponent("Apache Commons IO", "The Apache Software Foundation", "Apache-2.0", "https://commons.apache.org/proper/commons-io/"),
    LicenceComponent("jsoup", "Jonathan Hedley", "MIT", "https://jsoup.org"),
    LicenceComponent("ksoup", "Fleeksoft", "Apache-2.0", "https://github.com/fleeksoft/ksoup"),
    LicenceComponent("jutf7", "Jaap Beetstra", "MIT", "https://sourceforge.net/projects/jutf7/"),
    LicenceComponent("JZlib", "JCraft", "BSD-3-Clause", "https://www.jcraft.com/jzlib/"),
    LicenceComponent("Glide", "Bump Technologies and Google", "BSD-2-Clause, MIT and Apache-2.0", "https://github.com/bumptech/glide"),
    LicenceComponent("AppAuth for Android", "OpenID Foundation", "Apache-2.0", "https://github.com/openid/AppAuth-Android"),
    LicenceComponent("ZXing", "ZXing authors", "Apache-2.0", "https://github.com/zxing/zxing"),
    LicenceComponent("MiniDNS", "The MiniDNS authors", "Apache-2.0", "https://github.com/MiniDNS/minidns"),
    LicenceComponent("Coil", "Coil contributors", "Apache-2.0", "https://coil-kt.github.io/coil/"),
    LicenceComponent("Guava, Dagger, Flexbox", "Google", "Apache-2.0", "https://github.com/google/guava"),
    LicenceComponent("Apache Commons Lang and Text", "The Apache Software Foundation", "Apache-2.0", "https://commons.apache.org/"),
    LicenceComponent("Landscapist", "Jaewoong Eum", "Apache-2.0", "https://github.com/skydoves/landscapist"),
    LicenceComponent("Stately", "Touchlab", "Apache-2.0", "https://github.com/touchlab/Stately"),
    LicenceComponent("uri-kmp", "Eygraber", "Apache-2.0", "https://github.com/eygraber/uri-kmp"),
    LicenceComponent("SLF4J API and Checker Framework annotations", "QOS.ch and the Checker Framework authors", "MIT", "https://www.slf4j.org"),
    LicenceComponent("OpenPGP API library", "Dominik Schürmann and the OpenKeychain authors", "Apache-2.0", "https://github.com/open-keychain/openpgp-api"),
    LicenceComponent("CircleImageView, FastAdapter, PreferenceX, Safe Content Resolver", "their authors", "Apache-2.0", "https://github.com/hdodenhof/CircleImageView"),
    LicenceComponent("SearchPreference", "ByteHamster", "MIT", "https://github.com/ByteHamster/SearchPreference"),
)

/** Each distinct licence text once: family id and the raw resource. */
val LICENCE_RAW: List<Pair<String, Int>> = listOf(
    "gpl" to R.raw.account_common_gpl_3_0,
    "apache" to R.raw.account_common_apache_2_0,
    "mit" to R.raw.account_common_mit,
    "bsd" to R.raw.account_common_bsd,
    "ofl" to R.raw.account_common_ofl_1_1,
)

@Composable
fun rememberLicencesData(): LicencesData = LicencesData(
    appName = "Mailbox",
    sourceUrl = SOURCE_URL,
    licenceLine = stringResource(R.string.account_common_licences_licence),
    components = LICENCE_COMPONENTS,
    texts = LICENCE_RAW.map { (id, raw) ->
        LicenceText(id, stringResource(when (id) {
            "gpl" -> R.string.account_common_licences_gpl
            "apache" -> R.string.account_common_licences_apache
            "mit" -> R.string.account_common_licences_mit
            "bsd" -> R.string.account_common_licences_bsd
            "ofl" -> R.string.account_common_licences_ofl
            else -> R.string.account_common_licences_full_head
        }), raw)
    },
)

@Composable
fun rememberLicencesLabels(): LicencesLabels {
    val none = stringResource(R.string.account_common_no_browser_address, "@@")
    val noText = stringResource(R.string.account_common_licences_no_text, "@@")
    return LicencesLabels(
        title = stringResource(R.string.account_common_licences_title),
        back = stringResource(R.string.account_common_licences_back),
        sourceHeading = stringResource(R.string.account_common_licences_source_head),
        noText = { licence -> noText.replace("@@", licence) },
        noBrowser = { address -> none.replace("@@", address) },
    )
}

/**
 * Mailbox's About: only the words and the address. The dialog, its one Close button, the Licences
 * row, the links and the llama are the shared HouseAbout.kt, the same in every app. It is the one
 * About for both places that open it: before any account exists (account setup) and in Settings.
 */
@Composable
fun MailboxAboutDialog(version: String, onDismiss: () -> Unit) {
    val resources = LocalResources.current
    val data = AboutData(
        title = stringResource(R.string.account_common_about_title, version),
        sections = listOf(
            AboutSection(stringResource(R.string.account_common_about_privacy_heading), stringResource(R.string.account_common_about_privacy)),
            AboutSection(null, stringResource(R.string.account_common_about_licence)),
            AboutSection(null, stringResource(R.string.account_common_about_after)),
            AboutSection(stringResource(R.string.account_common_about_terms_heading), stringResource(R.string.account_common_about_terms)),
        ),
        sourceUrl = SOURCE_URL,
    )
    val labels = AboutLabels(
        close = stringResource(R.string.account_common_about_close),
        licences = stringResource(R.string.account_common_licences_title),
        feedTheLlamas = stringResource(R.string.account_common_about_feed_the_llamas),
        noBrowser = { address -> resources.getString(R.string.account_common_no_browser_address, address) },
    )
    ThemeMMD(colorScheme = monochrome) {
        HouseAboutDialog(data, labels, R.drawable.account_common_llama, onDismiss) { modifier, back ->
            LicencesScreen(rememberLicencesData(), rememberLicencesLabels(), onBack = back, modifier = modifier)
        }
    }
}
