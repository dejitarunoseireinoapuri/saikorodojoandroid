package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackDiceRollSoundTest {
    @Test
    fun `should play dice roll when a new die is added while rolling`() {
        assertTrue(
            shouldPlayDiceRollForNewDie(
                previousCount = 1,
                currentCount = 2,
                isRolling = true
            )
        )
    }

    @Test
    fun `should not play dice roll when die count does not increase`() {
        assertFalse(
            shouldPlayDiceRollForNewDie(
                previousCount = 2,
                currentCount = 2,
                isRolling = true,
            )
        )
    }

    @Test
    fun `should not play dice roll when not rolling even if die count increases`() {
        assertFalse(
            shouldPlayDiceRollForNewDie(
                previousCount = 1,
                currentCount = 2,
                isRolling = false,
            )
        )
    }
}
