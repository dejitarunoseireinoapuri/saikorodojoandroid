package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.rewardCardIds
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SequenceFailureReason
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.InMemoryGameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GetPendingMainGameSnapshotUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SequenceGameViewModelTest {

    @Test
    fun `start game rolls the first die and waits for decision`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(4),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isStarted)
        assertEquals(1, state.currentRoll)
        assertTrue(state.isAwaitingDecision)
        assertEquals(4, state.diceValue)
    }

    @Test
    fun `saving lower value fails with order reason`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(6, 4),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(SequenceFailureReason.ORDER, state.failureReason)
        assertEquals(4, state.failureDieValue)
        assertTrue(state.rewardCards.isEmpty())
    }

    @Test
    fun `successful sequence awards reward cards`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2, 3),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f),
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(3, state.savedValues.size)
        assertEquals(listOf(CardId.ADJUST_PLUS_MINUS_ONE, CardId.FLIP_FACE), state.rewardCards.map { it.id })
        assertTrue(state.pendingRewardCards.isEmpty())
    }

    @Test
    fun `save animation delays next roll start`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(3, 7),
            rollAnimationMs = 0L,
            saveAnimationMs = 200L,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.diceValue)

        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.runCurrent()

        val waitingState = viewModel.uiState.value
        assertEquals(2, waitingState.currentRoll)
        assertFalse(waitingState.isAwaitingDecision)
        assertEquals(3, waitingState.diceValue)

        testScheduler.advanceTimeBy(200L)
        testScheduler.advanceUntilIdle()

        val rolledState = viewModel.uiState.value
        assertTrue(rolledState.isAwaitingDecision)
        assertEquals(7, rolledState.diceValue)
    }

    @Test
    fun `discard can end game by rounds limit`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6),
            totalRolls = 3,
            dispatcher = UnconfinedTestDispatcher(testScheduler)
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
        assertEquals(1, state.discardCount)
    }

    private fun buildViewModel(
        diceRolls: List<Int>,
        dispatcher: TestDispatcher,
        rewardRolls: List<Float> = listOf(0.4f, 0.2f, 0.3f),
        totalRolls: Int = 5,
        maxDiscards: Int = 3,
        rewardRevealDelayMs: Long = 0L,
        targetSequence: Int = 3,
        rollAnimationMs: Long = 0L,
        tickMs: Long = 1L,
        saveAnimationMs: Long = 1L
    ): SequenceGameViewModel {
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollSequenceUseCase(diceRoller)
        val rewardUseCase = SelectMinigameRewardCardsUseCase(TestRewardRandomProvider(rewardRolls))
        val cardInventoryRepository = InMemoryCardInventoryRepository()
        val sessionRepository = InMemoryGameSessionRepository()
        return SequenceGameViewModel(
            rollSequenceUseCase = rollUseCase,
            selectMinigameRewardCardsUseCase = rewardUseCase,
            addCardsToInventoryUseCase = AddCardsToInventoryUseCase(cardInventoryRepository),
            loadGameSessionUseCase = LoadGameSessionUseCase(sessionRepository),
            saveGameSessionUseCase = SaveGameSessionUseCase(sessionRepository),
            getPendingMainGameSnapshotUseCase = GetPendingMainGameSnapshotUseCase(sessionRepository),
            clearGameSessionUseCase = ClearGameSessionUseCase(sessionRepository),
            dispatcher = dispatcher,
            rollAnimationMs = rollAnimationMs,
            tickMs = tickMs,
            rewardRevealDelayMs = rewardRevealDelayMs,
            saveAnimationMs = saveAnimationMs,
            totalRolls = totalRolls,
            targetSequence = targetSequence,
            maxDiscards = maxDiscards,
            cardUiModels = testCardUiModels()
        )
    }

    private fun testCardUiModels(): List<CardUiModel> {
        return (rewardCardIds() + CardId.MINIGAMES).map { id ->
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
