package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OddEvenGameViewModelTest {
    @Test
    fun endsGameAfterLossDelayWhenWinIsImpossible() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val losingRoller = DiceRoller { 1 }
        val viewModel = OddEvenGameViewModel(
            rollOddEvenUseCase = RollOddEvenUseCase(losingRoller),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L,
            lossMessageDelayMs = 1_000L,
            totalRounds = 3,
            targetCorrect = 3
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        runCurrent()

        assertFalse(viewModel.uiState.value.isComplete)

        advanceTimeBy(999L)
        runCurrent()
        assertFalse(viewModel.uiState.value.isComplete)

        advanceTimeBy(1L)
        runCurrent()
        assertTrue(viewModel.uiState.value.isComplete)
    }
}
