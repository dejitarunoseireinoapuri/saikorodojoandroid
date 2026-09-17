package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OddEvenGameViewModelTest {

    @Test
    fun resultStaysFixedDuringPlaybackAndScoringWaitsUntilItEnds() = runTest {
        var rolls = 0
        val viewModel = OddEvenGameViewModel(
            rollOddEvenUseCase = RollOddEvenUseCase(DiceRoller { ++rolls }),
            dispatcher = StandardTestDispatcher(testScheduler),
            rollAnimationMs = 300L,
            tickMs = 100L
        )
        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        runCurrent()
        assertEquals(4, viewModel.uiState.value.diceValue)
        assertTrue(viewModel.uiState.value.isRolling)
        assertEquals(0, viewModel.uiState.value.correctCount)

        testScheduler.advanceTimeBy(100L)
        runCurrent()
        assertEquals(4, viewModel.uiState.value.diceValue)
        assertEquals(0, viewModel.uiState.value.correctCount)
        testScheduler.advanceTimeBy(200L)
        runCurrent()
        assertEquals(4, rolls)
        assertTrue(!viewModel.uiState.value.isRolling)
        assertEquals(1, viewModel.uiState.value.correctCount)
    }

    @Test
    fun startsWithSevenRoundsByDefault() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val viewModel = OddEvenGameViewModel(
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L
        )

        assertEquals(7, viewModel.uiState.value.totalRounds)
    }

    @Test
    fun completesImmediatelyWhenWinIsImpossibleAfterALoss() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val losingRoller = DiceRoller { 1 }
        val viewModel = OddEvenGameViewModel(
            rollOddEvenUseCase = RollOddEvenUseCase(losingRoller),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L,
            totalRounds = 3,
            targetCorrect = 3
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        runCurrent()

        runCurrent()
        assertTrue(viewModel.uiState.value.isComplete)
    }
}
