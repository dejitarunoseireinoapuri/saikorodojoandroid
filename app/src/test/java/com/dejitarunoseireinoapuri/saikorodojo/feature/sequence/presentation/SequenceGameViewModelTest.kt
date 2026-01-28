package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.IntRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SelectSequenceRewardCardUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.sequenceRewardCardIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SequenceGameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `start game rolls the first die and waits for decision`() = runTest {
        val viewModel = buildViewModel(diceRolls = listOf(4))

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isStarted)
        assertEquals(1, state.currentRoll)
        assertTrue(state.isAwaitingDecision)
        assertEquals(4, state.diceValue)
    }

    @Test
    fun `saving three ascending dice awards a card`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 3, 5),
            rewardIndex = 0
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(3, state.savedValues.size)
        assertNotNull(state.rewardCard)
        assertEquals(CardId.ADJUST_PLUS_MINUS_ONE, state.rewardCard?.id)
    }

    @Test
    fun `saving a lower value ends the game`() = runTest {
        val viewModel = buildViewModel(diceRolls = listOf(6, 4))

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertNull(state.rewardCard)
    }

    @Test
    fun `discarding until the final round ends the game`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6),
            totalRolls = 3
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(3, state.discardCount)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
        assertNull(state.rewardCard)
    }

    @Test
    fun `reaching the maximum rolls without success ends the game`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2),
            totalRolls = 2,
            maxDiscards = 10
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(2, state.currentRoll)
        assertEquals(2, state.discardCount)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
        assertNull(state.rewardCard)
    }

    private fun buildViewModel(
        diceRolls: List<Int>,
        rewardIndex: Int = 0,
        totalRolls: Int = 5,
        maxDiscards: Int = 3
    ): SequenceGameViewModel {
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollSequenceUseCase(diceRoller)
        val rewardUseCase = SelectSequenceRewardCardUseCase(
            randomProvider = IntRandomProvider { rewardIndex }
        )
        return SequenceGameViewModel(
            rollSequenceUseCase = rollUseCase,
            selectSequenceRewardCardUseCase = rewardUseCase,
            dispatcher = dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            totalRolls = totalRolls,
            maxDiscards = maxDiscards,
            cardUiModels = testCardUiModels()
        )
    }

    private fun testCardUiModels(): List<CardUiModel> {
        return sequenceRewardCardIds().map { id ->
            CardUiModel(
                id = id,
                titleRes = 0,
                descriptionRes = 0,
                iconRes = 0
            )
        }
    }

    private class SequenceDiceRoller(
        private val rolls: List<Int>
    ) : DiceRoller {
        private var index = 0

        override fun roll(range: IntRange): Int {
            val value = rolls[index.coerceAtMost(rolls.lastIndex)]
            index += 1
            return value
        }
    }
}
