package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateBlackjackScoreUseCaseTest {
    private val useCase = CalculateBlackjackScoreUseCase()

    @Test
    fun `uses ace as eleven when it helps reach blackjack`() {
        val score = useCase.execute(listOf(1, 10))

        assertEquals(21, score)
    }

    @Test
    fun `downgrades extra aces to avoid busting`() {
        val score = useCase.execute(listOf(1, 1, 10))

        assertEquals(12, score)
    }

    @Test
    fun `chooses best total with multiple aces`() {
        val score = useCase.execute(listOf(1, 1, 9))

        assertEquals(21, score)
    }
}
