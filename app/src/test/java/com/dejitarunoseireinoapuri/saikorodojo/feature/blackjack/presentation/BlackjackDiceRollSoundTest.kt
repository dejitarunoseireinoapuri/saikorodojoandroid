package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackDiceRollSoundTest {
    @Test
    fun `should play dice roll on first shared deal while rolling`() {
        assertTrue(
            shouldPlayDiceRollSound(
                previousPlayerCount = 0,
                currentPlayerCount = 2,
                previousDealerCount = 0,
                currentDealerCount = 1,
                isRolling = true
            )
        )
    }

    @Test
    fun `should play dice roll when player adds a die after initial deal`() {
        assertTrue(
            shouldPlayDiceRollSound(
                previousPlayerCount = 2,
                currentPlayerCount = 3,
                previousDealerCount = 1,
                currentDealerCount = 1,
                isRolling = true,
            )
        )
    }

    @Test
    fun `should play dice roll when dealer adds a die after stand`() {
        assertTrue(
            shouldPlayDiceRollSound(
                previousPlayerCount = 2,
                currentPlayerCount = 2,
                previousDealerCount = 1,
                currentDealerCount = 2,
                isRolling = true,
            )
        )
    }

    @Test
    fun `should not play dice roll when no dice count increases`() {
        assertFalse(
            shouldPlayDiceRollSound(
                previousPlayerCount = 2,
                currentPlayerCount = 2,
                previousDealerCount = 2,
                currentDealerCount = 2,
                isRolling = true,
            )
        )
    }

    @Test
    fun `should not play dice roll when not rolling`() {
        assertFalse(
            shouldPlayDiceRollSound(
                previousPlayerCount = 2,
                currentPlayerCount = 3,
                previousDealerCount = 1,
                currentDealerCount = 1,
                isRolling = false,
            )
        )
    }
}
