package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectStartingCardsUseCaseTest {
    @Test
    fun `selects three cards by default`() {
        val useCase = SelectStartingCardsUseCase(
            randomProvider = { 0f }
        )

        val cards = useCase.execute()

        assertEquals(3, cards.size)
        assertEquals(rewardCardIds().first(), cards.first())
    }

    @Test
    fun `returns empty list for non-positive count`() {
        val useCase = SelectStartingCardsUseCase(
            randomProvider = { 0.5f }
        )

        val cards = useCase.execute(count = 0)

        assertEquals(emptyList<CardId>(), cards)
    }
}
