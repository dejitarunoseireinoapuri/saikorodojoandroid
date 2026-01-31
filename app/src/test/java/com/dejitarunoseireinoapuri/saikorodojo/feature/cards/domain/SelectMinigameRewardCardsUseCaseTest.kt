package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectMinigameRewardCardsUseCaseTest {
    @Test
    fun `returns two cards when random is below half`() {
        val useCase = SelectMinigameRewardCardsUseCase(
            randomProvider = TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
        )

        val rewards = useCase.execute()

        assertEquals(2, rewards.size)
    }

    @Test
    fun `returns three cards when random is at least half`() {
        val useCase = SelectMinigameRewardCardsUseCase(
            randomProvider = TestRewardRandomProvider(listOf(0.5f, 0.2f, 0.3f, 0.4f))
        )

        val rewards = useCase.execute()

        assertEquals(3, rewards.size)
    }

    @Test
    fun `retry card appears only once`() {
        val useCase = SelectMinigameRewardCardsUseCase(
            randomProvider = TestRewardRandomProvider(listOf(0.6f, 0.01f, 0.01f, 0.01f))
        )

        val rewards = useCase.execute()

        assertEquals(3, rewards.size)
        assertEquals(1, rewards.count { it == CardId.RETRY })
    }

    @Test
    fun `rolls under retry weight select retry when available`() {
        val useCase = SelectMinigameRewardCardsUseCase(
            randomProvider = TestRewardRandomProvider(listOf(0.4f, 0.06f, 0.2f))
        )

        val rewards = useCase.execute()

        assertEquals(
            listOf(CardId.RETRY, CardId.FLIP_FACE),
            rewards
        )
    }
}

private class TestRewardRandomProvider(
    private val values: List<Float>
) : RewardCardsRandomProvider {
    private var index = 0

    override fun nextFloat(): Float {
        val value = values.getOrElse(index) { values.last() }
        index += 1
        return value
    }
}
