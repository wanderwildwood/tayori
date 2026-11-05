package app.k9mail.feature.account.common.ui.house

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import app.k9mail.feature.account.common.R
import com.mudita.mmd.ThemeMMD

/** The address of the app's own source code. */
const val SOURCE_URL = "https://github.com/wanderwildwood/tayori"

/**
 * Each distinct family text once: family id and the raw resource. MIT and BSD libraries carry their
 * own license file with its copyright line (config/licenses), so no family text stands in for them.
 */
val LICENCE_RAW: List<Pair<String, Int>> = listOf(
    "gpl" to R.raw.account_common_gpl_3_0,
    "apache" to R.raw.account_common_apache_2_0,
    "ofl" to R.raw.account_common_ofl_1_1,
)

@Composable
fun rememberLicencesData(): LicencesData = LicencesData(
    appName = "Mailbox",
    sourceUrl = SOURCE_URL,
    licenceLine = stringResource(R.string.account_common_licences_licence),
    // One row per artifact in the APK, listed by the build in the app module (AboutLibraries plugin,
    // app-thunderbird); config/libraries adds what Gradle cannot see. This module cannot see the
    // app module's R, so the file is found by name.
    components = rememberGeneratedComponents(generatedLicencesResId(LocalContext.current)),
    texts = LICENCE_RAW.map { (id, raw) ->
        LicenceText(id, stringResource(when (id) {
            "gpl" -> R.string.account_common_licences_gpl
            "apache" -> R.string.account_common_licences_apache
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
 * Mailbox's About: only the words and the address. The dialog, its drawers, its one Close button,
 * the Licenses drawer, the links and the llama are the shared HouseAbout.kt, the same in every app.
 * It is the one About for both places that open it: before any account exists (account setup) and
 * in Settings.
 */
@Composable
fun MailboxAboutDialog(version: String, onDismiss: () -> Unit) {
    val resources = LocalResources.current
    val data = AboutData(
        title = stringResource(R.string.account_common_about_title, version),
        sections = listOf(
            AboutSection(null, stringResource(R.string.account_common_about_what)),
            AboutSection(stringResource(R.string.account_common_about_privacy_heading), stringResource(R.string.account_common_about_privacy)),
            AboutSection(stringResource(R.string.account_common_privacy_who_title), stringResource(R.string.account_common_privacy_who)),
            AboutSection(stringResource(R.string.account_common_privacy_contact_title), stringResource(R.string.account_common_privacy_contact)),
            AboutSection(stringResource(R.string.account_common_privacy_rights_title), stringResource(R.string.account_common_privacy_rights)),
            AboutSection(stringResource(R.string.account_common_about_terms_heading), stringResource(R.string.account_common_about_terms)),
            AboutSection(null, stringResource(R.string.account_common_about_licence)),
            AboutSection(null, stringResource(R.string.account_common_about_after)),
        ),
        sourceUrl = SOURCE_URL,
    )
    val labels = AboutLabels(
        close = stringResource(R.string.account_common_about_close),
        licences = stringResource(R.string.account_common_licences_title),
        feedTheLlamas = stringResource(R.string.account_common_about_feed_the_llamas),
        noBrowser = { address -> resources.getString(R.string.account_common_no_browser_address, address) },
        writeTo = { address -> resources.getString(R.string.account_common_about_write_to, address) },
        noMailApp = { address -> resources.getString(R.string.account_common_about_no_mail_app, address) },
        openDrawer = { heading -> resources.getString(R.string.account_common_about_open_drawer, heading) },
        closeDrawer = { heading -> resources.getString(R.string.account_common_about_close_drawer, heading) },
        contact = stringResource(R.string.account_common_about_contact_head),
        links = stringResource(R.string.account_common_about_links_head),
    )
    ThemeMMD(colorScheme = monochrome, typography = houseTypography) {
        HouseAboutDialog(data, labels, R.drawable.account_common_llama, onDismiss)
    }
}
