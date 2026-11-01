package com.fsck.k9.ui.settings

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.Test

class ArmedActionTest {

    @Test
    fun `first tap arms and does not confirm`() {
        val action = ArmedAction()
        assertThat(action.tap(now = 1_000)).isFalse()
        assertThat(action.isArmed(now = 1_001)).isTrue()
    }

    @Test
    fun `second tap within the window confirms and disarms`() {
        val action = ArmedAction()
        action.tap(now = 1_000)
        assertThat(action.tap(now = 6_900)).isTrue()
        assertThat(action.isArmed(now = 6_901)).isFalse()
    }

    @Test
    fun `it disarms itself after six seconds`() {
        val action = ArmedAction()
        action.tap(now = 1_000)
        assertThat(action.isArmed(now = 7_001)).isFalse()
        assertThat(action.tap(now = 7_002)).isFalse()
    }

    @Test
    fun `disarm clears it`() {
        val action = ArmedAction()
        action.tap(now = 0)
        action.disarm()
        assertThat(action.isArmed(now = 1)).isFalse()
    }
}
