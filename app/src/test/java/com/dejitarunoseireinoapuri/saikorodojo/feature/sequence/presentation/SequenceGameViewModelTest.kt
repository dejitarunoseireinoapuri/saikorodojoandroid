package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.rewardCardIds
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `saving three ascending dice awards cards`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 1, 2, 2, 3, 3),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f)
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
        assertEquals(
            listOf(CardId.ADJUST_PLUS_MINUS_ONE, CardId.FLIP_FACE),
            state.rewardCards.map { it.id }
        )
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
        assertTrue(state.rewardCards.isEmpty())
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
        assertTrue(state.rewardCards.isEmpty())
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
        assertTrue(state.rewardCards.isEmpty())
    }

    private fun buildViewModel(
        diceRolls: List<Int>,
        rewardRolls: List<Float> = listOf(0.4f, 0.2f, 0.3f),
        totalRolls: Int = 5,
        maxDiscards: Int = 3,
        rewardRevealDelayMs: Long = 0L
    ): SequenceGameViewModel {
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollSequenceUseCase(diceRoller)
        val rewardUseCase = SelectMinigameRewardCardsUseCase(
            TestRewardRandomProvider(rewardRolls)
        )
        return SequenceGameViewModel(
            rollSequenceUseCase = rollUseCase,
            selectMinigameRewardCardsUseCase = rewardUseCase,
            dispatcher = dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            rewardRevealDelayMs = rewardRevealDelayMs,
            totalRolls = totalRolls,
            maxDiscards = maxDiscards,
            cardUiModels = testCardUiModels()
        )
    }

    private fun testCardUiModels(): List<CardUiModel> {
        return (rewardCardIds() + CardId.RETRY).map { id ->
            CardUiModel(
                id = id,
                titleRes = 0,
                descriptionRes = 0,
                iconRes = 0
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
