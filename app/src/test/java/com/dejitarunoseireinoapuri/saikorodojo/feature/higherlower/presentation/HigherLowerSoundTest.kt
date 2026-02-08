package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HigherLowerSoundTest {
    @Test
    fun `play success when correct streak increases`() {
        assertTrue(shouldPlayHigherLowerSuccess(previousStreak = 1, currentStreak = 2))
    }

    @Test
    fun `do not play success when streak does not increase`() {
        assertFalse(shouldPlayHigherLowerSuccess(previousStreak = 2, currentStreak = 2))
    }

    @Test
    fun `play loss when hasLoss becomes true`() {
        assertTrue(shouldPlayHigherLowerLoss(previousHasLoss = false, currentHasLoss = true))
    }

    @Test
    fun `do not play loss when hasLoss stays true`() {
        assertFalse(shouldPlayHigherLowerLoss(previousHasLoss = true, currentHasLoss = true))
    }
}
