package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.RollHigherLowerUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
        assertEquals(listOf(4, 4), state.currentDiceValues)
        assertFalse(state.isCurrentDiceHidden)
        assertTrue(state.isCurrentDiceAnchoredUp)
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
        assertTrue(state.rewardCards.isNotEmpty())
    }

    @Test
    fun `matching sums trigger a win`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 3, 3, 2, 2, 2, 4)
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.rewardCards.isNotEmpty())
        assertFalse(state.hasLoss)
    }

    @Test
    fun `selecting a choice hides buttons while rolling`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 2, 3, 1, 1, 4, 4)
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))

        val state = viewModel.uiState.value
        assertTrue(state.isRolling)
        assertFalse(state.isChoiceVisible)
        assertEquals(HigherLowerChoice.HIGHER, state.selectedChoice)
    }

    @Test
    fun `success highlight shows before transition starts`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 2, 3, 1, 1, 4, 4),
            successHighlightMs = 1_000L,
            transitionMs = 10L
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        runCurrent()

        val highlightState = viewModel.uiState.value
        assertTrue(highlightState.isSuccessHighlighting)
        assertFalse(highlightState.isTransitioning)

        advanceTimeBy(1_000L)
        runCurrent()

        val transitionState = viewModel.uiState.value
        assertFalse(transitionState.isSuccessHighlighting)
        assertTrue(transitionState.isTransitioning)
    }

    @Test
    fun `transition keeps current dice visible while updating base`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 2, 3, 1, 1, 4, 4),
            successHighlightMs = 0L,
            transitionMs = 1_000L,
            postTransitionHoldMs = 0L
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        runCurrent()

        val transitionState = viewModel.uiState.value
        val currentDice = transitionState.currentDiceValues
        assertTrue(currentDice.isNotEmpty())
        assertTrue(transitionState.isTransitioning)

        advanceTimeBy(1_000L)
        runCurrent()

        val finalState = viewModel.uiState.value
        assertEquals(currentDice, finalState.baseDiceValues)
        assertEquals(currentDice, finalState.currentDiceValues)
        assertFalse(finalState.isCurrentDiceHidden)
        assertTrue(finalState.isCurrentDiceAnchoredUp)
        assertFalse(finalState.isTransitioning)
    }

    @Test
    fun `post transition hold keeps dice visible before showing buttons`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 2, 3, 1, 1, 4, 4),
            successHighlightMs = 0L,
            transitionMs = 1_000L,
            postTransitionHoldMs = 500L
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        runCurrent()

        advanceTimeBy(1_000L)
        runCurrent()

        val holdState = viewModel.uiState.value
        assertFalse(holdState.isTransitioning)
        assertFalse(holdState.isCurrentDiceHidden)
        assertTrue(holdState.isCurrentDiceAnchoredUp)
        assertFalse(holdState.isChoiceVisible)

        advanceTimeBy(500L)
        runCurrent()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isCurrentDiceHidden)
        assertTrue(finalState.isCurrentDiceAnchoredUp)
        assertTrue(finalState.isChoiceVisible)
    }

    @Test
    fun `win waits before showing reward cards`() = runTest {
        val viewModel = buildViewModel(
            diceValues = listOf(1, 1, 1, 1, 1, 1, 1, 1),
            successResultDelayMs = 1_000L
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        runCurrent()

        val pendingState = viewModel.uiState.value
        assertFalse(pendingState.isComplete)
        assertTrue(pendingState.rewardCards.isEmpty())

        advanceTimeBy(1_000L)
        runCurrent()

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isComplete)
        assertTrue(finalState.rewardCards.isNotEmpty())
    }

    private fun buildViewModel(
        diceValues: List<Int>,
        successHighlightMs: Long = 0L,
        transitionMs: Long = 0L,
        successResultDelayMs: Long = 0L,
        postTransitionHoldMs: Long = 0L
    ): HigherLowerGameViewModel {
        val diceRoller = QueueDiceRoller(diceValues.toMutableList())
        return HigherLowerGameViewModel(
            rollHigherLowerUseCase = RollHigherLowerUseCase(diceRoller),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                FixedRewardRandomProvider()
            ),
            dispatcher = dispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            resultDelayMs = 0L,
            transitionMs = transitionMs,
            successHighlightMs = successHighlightMs,
            successResultDelayMs = successResultDelayMs,
            postTransitionHoldMs = postTransitionHoldMs
        )
    }

    private class QueueDiceRoller(
        private val values: MutableList<Int>
    ) : DiceRoller {
        override fun roll(range: IntRange): Int {
            return values.removeAt(0)
        }
    }

    private class FixedRewardRandomProvider : RewardCardsRandomProvider {
        override fun nextFloat(): Float = 0.4f
    }
}
