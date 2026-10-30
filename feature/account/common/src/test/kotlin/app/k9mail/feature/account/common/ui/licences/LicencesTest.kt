package app.k9mail.feature.account.common.ui.licences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LicencesTest {
    @Test fun theListsAreNotEmptyAndNamedOnce() {
        assertTrue(LIBRARIES.isNotEmpty())
        assertEquals(LIBRARIES.size, LIBRARIES.map { it.name }.toSet().size)
        assertTrue(LICENCE_TEXTS.size >= 4)
        assertTrue(LIBRARIES.all { it.link.startsWith("http") })
    }

    @Test fun theSourceLinkIsTheRepo() {
        assertEquals("https://github.com/wanderwildwood/tayori", SOURCE_URL)
    }

    @Test fun paragraphsKeepEveryWord() {
        val text = "First line\nsecond line\n\n  Third  \r\n\r\nFourth"
        assertEquals(listOf("First line second line", "Third", "Fourth"), licenceParagraphs(text))
    }
}
