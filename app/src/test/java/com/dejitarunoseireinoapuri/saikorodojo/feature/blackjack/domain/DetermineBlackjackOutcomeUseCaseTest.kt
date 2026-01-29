package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DetermineBlackjackOutcomeUseCaseTest {
    private val useCase = DetermineBlackjackOutcomeUseCase()

    @Test
    fun `player busts and loses even if dealer busts`() {
        val outcome = useCase.execute(
            playerTotal = 25,
            dealerTotal = 24,
            isPlayerBust = true,
            isDealerBust = true
        )

        assertEquals(BlackjackOutcome.PLAYER_LOSE, outcome)
    }

    @Test
    fun `dealer busts and player wins`() {
        val outcome = useCase.execute(
            playerTotal = 18,
            dealerTotal = 24,
            isPlayerBust = false,
            isDealerBust = true
        )

        assertEquals(BlackjackOutcome.PLAYER_WIN, outcome)
    }

    @Test
    fun `dealer wins ties`() {
        val outcome = useCase.execute(
            playerTotal = 18,
            dealerTotal = 18,
            isPlayerBust = false,
            isDealerBust = false
        )

        assertEquals(BlackjackOutcome.PLAYER_LOSE, outcome)
    }
}
