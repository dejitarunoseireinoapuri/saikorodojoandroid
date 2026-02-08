package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OddEvenSoundTest {
    @Test
    fun `play success when correct count increases`() {
        assertTrue(shouldPlayOddEvenSuccess(previousCorrect = 1, currentCorrect = 2))
    }

    @Test
    fun `do not play success when correct count does not increase`() {
        assertFalse(shouldPlayOddEvenSuccess(previousCorrect = 2, currentCorrect = 2))
    }

    @Test
    fun `play loss when loss screen first appears`() {
        assertTrue(shouldPlayOddEvenLoss(previousHasLoss = false, currentHasLoss = true))
    }

    @Test
    fun `do not play loss when loss state does not change`() {
        assertFalse(shouldPlayOddEvenLoss(previousHasLoss = true, currentHasLoss = true))
    }
}
