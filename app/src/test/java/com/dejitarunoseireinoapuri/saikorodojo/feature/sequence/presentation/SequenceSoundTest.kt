package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SequenceSoundTest {
    @Test
    fun `play success when saved count increases`() {
        assertTrue(shouldPlaySequenceSuccess(previousSavedCount = 1, currentSavedCount = 2))
    }

    @Test
    fun `do not play success when saved count does not increase`() {
        assertFalse(shouldPlaySequenceSuccess(previousSavedCount = 2, currentSavedCount = 2))
    }

    @Test
    fun `play loss when failure appears`() {
        assertTrue(shouldPlaySequenceLoss(previousHasFailure = false, currentHasFailure = true))
    }

    @Test
    fun `do not play loss when failure already present`() {
        assertFalse(shouldPlaySequenceLoss(previousHasFailure = true, currentHasFailure = true))
    }
}
