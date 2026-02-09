package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackDiceRollSoundTest {
    @Test
    fun `should play dice roll when rolling starts`() {
        assertTrue(
            shouldPlayDiceRollOnStart(
                isRolling = true,
                wasRolling = false
            )
        )
    }

    @Test
    fun `should not play dice roll when already rolling`() {
        assertFalse(
            shouldPlayDiceRollOnStart(
                isRolling = true,
                wasRolling = true
            )
        )
    }

    @Test
    fun `should not play dice roll when not rolling`() {
        assertFalse(
            shouldPlayDiceRollOnStart(
                isRolling = false,
                wasRolling = false
            )
        )
    }
}
