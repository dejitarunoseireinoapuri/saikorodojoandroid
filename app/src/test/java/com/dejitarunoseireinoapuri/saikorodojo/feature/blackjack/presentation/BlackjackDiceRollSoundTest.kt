package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackDiceRollSoundTest {
    @Test
    fun `should play dice roll when a new die is added`() {
        assertTrue(shouldPlayDiceRollForNewDie(previousCount = 1, currentCount = 2))
    }

    @Test
    fun `should not play dice roll when dice count stays the same`() {
        assertFalse(shouldPlayDiceRollForNewDie(previousCount = 2, currentCount = 2))
    }

    @Test
    fun `should not play dice roll when dice count decreases`() {
        assertFalse(shouldPlayDiceRollForNewDie(previousCount = 3, currentCount = 1))
    }
}
