package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OddEvenSoundTest {
    @Test
    fun `play success when correct count increases`() {
        assertTrue(shouldPlayOddEvenSuccess(previousCorrect = 1, currentCorrect = 2))
    }

    @Test
    fun `do not play success when correct count does not increase`() {
        assertFalse(shouldPlayOddEvenSuccess(previousCorrect = 2, currentCorrect = 2))
    }

    @Test
    fun `play loss when wrong count increases`() {
        assertTrue(
            shouldPlayOddEvenLoss(
                previousWrong = 0,
                currentWrong = 1,
                previousHasLoss = false,
                currentHasLoss = false
            )
        )
    }

    @Test
    fun `play loss when loss screen first appears without new wrong count`() {
        assertTrue(
            shouldPlayOddEvenLoss(
                previousWrong = 1,
                currentWrong = 1,
                previousHasLoss = false,
                currentHasLoss = true
            )
        )
    }

    @Test
    fun `do not play loss when neither wrong count nor loss state changes`() {
        assertFalse(
            shouldPlayOddEvenLoss(
                previousWrong = 1,
                currentWrong = 1,
                previousHasLoss = true,
                currentHasLoss = true
            )
        )
    }

    @Test
    fun `show dice after loss while no rewards are present`() {
        val uiState = OddEvenGameUiState(
            isStarted = true,
            isComplete = true,
            rewardCards = emptyList()
        )

        assertTrue(shouldShowOddEvenDice(uiState))
    }

    @Test
    fun `hide dice when reward cards are shown`() {
        val uiState = OddEvenGameUiState(
            isStarted = true,
            isComplete = true,
            rewardCards = listOf(defaultCardUiModels().first().copy(count = 1))
        )

        assertFalse(shouldShowOddEvenDice(uiState))
    }
}
