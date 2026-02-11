package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackDiceRollSoundTest {
    @Test
    fun `should play dice roll for first shared deal while rolling`() {
        assertTrue(
            shouldPlayInitialDealDiceRoll(
                previousPlayerCount = 0,
                currentPlayerCount = 2,
                previousDealerCount = 0,
                currentDealerCount = 1,
                isRolling = true
            )
        )
    }

    @Test
    fun `should not play dice roll when player adds die after initial deal`() {
        assertFalse(
            shouldPlayInitialDealDiceRoll(
                previousPlayerCount = 2,
                currentPlayerCount = 3,
                previousDealerCount = 1,
                currentDealerCount = 1,
                isRolling = true,
            )
        )
    }

    @Test
    fun `should not play dice roll when not rolling`() {
        assertFalse(
            shouldPlayInitialDealDiceRoll(
                previousPlayerCount = 0,
                currentPlayerCount = 2,
                previousDealerCount = 0,
                currentDealerCount = 1,
                isRolling = false,
            )
        )
    }
}
