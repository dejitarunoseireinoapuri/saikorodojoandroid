package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.RollHigherLowerUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.SelectHigherLowerRewardCardUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.IntRandomProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HigherLowerGameViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `correct guess advances round and moves dice`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 2, 3, 1, 1, 4, 4)
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(listOf(4, 4), state.baseDiceValues)
        assertTrue(state.currentDiceValues.isEmpty())
        assertEquals(1, state.correctStreak)
        assertTrue(state.isChoiceVisible)
        assertFalse(state.isComplete)
    }

    @Test
    fun `incorrect guess completes the game as loss`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 3, 3, 1, 1, 2, 1)
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.hasLoss)
        assertEquals(listOf(2, 1), state.currentDiceValues)
        assertFalse(state.isChoiceVisible)
    }

    @Test
    fun `three correct guesses award a reward`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(
                1, 1, 2, 3,
                1, 1, 4, 4,
                1, 1, 1, 1,
                1, 1, 2, 2
            )
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.LOWER))
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertFalse(state.hasLoss)
        assertNotNull(state.rewardCard)
    }

    @Test
    fun `matching sums twice in a row trigger a win`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(
                1, 1, 3, 3,
                2, 2, 2, 4,
                5, 1, 1, 5
            )
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val midState = viewModel.uiState.value
        assertFalse(midState.isComplete)

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertNotNull(state.rewardCard)
        assertFalse(state.hasLoss)
    }

    private fun buildViewModel(
        diceValues: List<Int>
    ): HigherLowerGameViewModel {
        val diceRoller = QueueDiceRoller(diceValues.toMutableList())
        return HigherLowerGameViewModel(
            rollHigherLowerUseCase = RollHigherLowerUseCase(diceRoller),
            selectHigherLowerRewardCardUseCase = SelectHigherLowerRewardCardUseCase(
                randomProvider = FixedRandomProvider()
            ),
            dispatcher = dispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            resultDelayMs = 0L
        )
    }

    private class QueueDiceRoller(
        private val values: MutableList<Int>
    ) : DiceRoller {
        override fun roll(range: IntRange): Int {
            return values.removeAt(0)
        }
    }

    private class FixedRandomProvider : IntRandomProvider {
        override fun nextInt(bound: Int): Int = 0
    }
}
