package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectOddEvenRewardCardUseCaseTest {
    @Test
    fun `reward cards never include retry`() {
        val useCase = SelectOddEvenRewardCardUseCase(
            randomProvider = IntRandomProvider { 0 }
        )

        val reward = useCase.execute()

        assertNotEquals(CardId.RETRY, reward)
        assertTrue(oddEvenRewardCardIds().contains(reward))
    }

    @Test
    fun `reward selection is deterministic with provided random`() {
        val cards = oddEvenRewardCardIds()
        val useCase = SelectOddEvenRewardCardUseCase(
            randomProvider = IntRandomProvider { cards.lastIndex }
        )

        val reward = useCase.execute()

        assertEquals(cards.last(), reward)
    }
}
