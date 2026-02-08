package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `play loss when wrong count increases`() {
        assertTrue(shouldPlayOddEvenLoss(previousWrong = 0, currentWrong = 1))
    }

    @Test
    fun `do not play loss when wrong count does not increase`() {
        assertFalse(shouldPlayOddEvenLoss(previousWrong = 1, currentWrong = 1))
    }
}
