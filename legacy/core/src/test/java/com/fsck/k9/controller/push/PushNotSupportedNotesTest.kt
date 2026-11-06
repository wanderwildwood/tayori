package com.fsck.k9.controller.push

import android.content.Context
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import net.thunderbird.core.android.testing.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class PushNotSupportedNotesTest : RobolectricTest() {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val notes = PushNotSupportedNotes(context)

    @Test
    fun aServerThatRefusedPushIsRemembered() {
        notes.mark("ada-account")

        assertThat(PushNotSupportedNotes(context).isMarked("ada-account")).isTrue()
        assertThat(notes.isMarked("tomas-account")).isFalse()
    }

    @Test
    fun switchingPushOnAgainForgetsIt() {
        notes.mark("ada-account")
        notes.clear("ada-account")

        assertThat(notes.isMarked("ada-account")).isFalse()
    }
}
