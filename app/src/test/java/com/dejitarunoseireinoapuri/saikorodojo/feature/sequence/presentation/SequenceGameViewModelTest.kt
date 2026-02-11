package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SequenceGameViewModelTest {
    private lateinit var dispatcher: StandardTestDispatcher

    @Test
    fun `start game rolls the first die and waits for decision`() = runTest {
        val viewModel = buildViewModel(diceRolls = listOf(4))

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isStarted)
        assertEquals(1, state.currentRoll)
        assertTrue(state.isAwaitingDecision)
        assertEquals(4, state.diceValue)
    }

    @Test
    fun `saving three ascending dice awards cards`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2, 3),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f)
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
        assertEquals(
            listOf(CardId.ADJUST_PLUS_MINUS_ONE, CardId.FLIP_FACE),
            state.rewardCards.map { it.id }
        )
    }

    @Test
    fun `success keeps pending rewards visible for one second before revealing cards`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2, 3),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f),
            rewardRevealDelayMs = 1_000L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)

        testScheduler.runCurrent()
        val pendingState = viewModel.uiState.value
        assertTrue(pendingState.isComplete)
        assertTrue(pendingState.rewardCards.isEmpty())
        assertEquals(2, pendingState.pendingRewardCards.size)

        testScheduler.advanceTimeBy(999L)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.rewardCards.isEmpty())

        testScheduler.advanceTimeBy(1L)
        testScheduler.runCurrent()
        assertEquals(2, viewModel.uiState.value.rewardCards.size)
        assertTrue(viewModel.uiState.value.pendingRewardCards.isEmpty())
    }

    @Test
    fun `winning save does not hide latest die and reveals rewards after configured delay`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2, 3),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f),
            saveAnimationMs = 200L,
            rewardRevealDelayMs = 1_000L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isLatestSavedValueHidden.not())
        assertTrue(viewModel.uiState.value.rewardCards.isEmpty())

        testScheduler.advanceTimeBy(200L)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.rewardCards.isEmpty())

        testScheduler.advanceTimeBy(799L)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.rewardCards.isEmpty())

        testScheduler.advanceTimeBy(1L)
        testScheduler.runCurrent()
        assertEquals(2, viewModel.uiState.value.rewardCards.size)
    }

    @Test
    fun `saving a lower value ends the game`() = runTest {
        val viewModel = buildViewModel(diceRolls = listOf(6, 4))

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.rewardCards.isEmpty())
    }

    @Test
    fun `saving an equal value keeps the sequence alive`() = runTest {
        val viewModel = buildViewModel(diceRolls = listOf(4, 4, 5))

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete.not())
        assertEquals(listOf(4, 4), state.savedValues)
        assertTrue(state.isAwaitingDecision)
    }

    @Test
    fun `starting a new roll keeps previous dice visible before animation ticks`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(4, 7),
            rollAnimationMs = 1_000L,
            tickMs = 100L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        assertEquals(7, viewModel.uiState.value.diceValue)

        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)

        val immediateState = viewModel.uiState.value
        assertTrue(immediateState.isRolling)
        assertEquals(7, immediateState.diceValue)
    }



    @Test
    fun `save action keeps latest saved die visible while waiting next roll`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(3, 7),
            rollAnimationMs = 200L,
            tickMs = 100L,
            saveAnimationMs = 200L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLatestSavedValueHidden.not())
        testScheduler.advanceTimeBy(199L)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isLatestSavedValueHidden.not())
        testScheduler.advanceTimeBy(1L)
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLatestSavedValueHidden.not())
    }

    @Test
    fun `roll finishes without extra trailing delay after last animation tick`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 5, 7),
            rollAnimationMs = 200L,
            tickMs = 100L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)

        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isRolling)

        testScheduler.advanceTimeBy(100L)
        testScheduler.runCurrent()
        assertTrue(viewModel.uiState.value.isAwaitingDecision)
        assertTrue(viewModel.uiState.value.isRolling.not())
    }

    @Test
    fun `roll starts after save animation completes`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(3, 7),
            rollAnimationMs = 0L,
            tickMs = 1L,
            saveAnimationMs = 200L
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()

        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.runCurrent()
        assertEquals(3, viewModel.uiState.value.diceValue)

        testScheduler.advanceTimeBy(199L)
        testScheduler.runCurrent()
        assertEquals(3, viewModel.uiState.value.diceValue)

        testScheduler.advanceTimeBy(1L)
        testScheduler.runCurrent()
        assertEquals(7, viewModel.uiState.value.diceValue)
    }

    @Test
    fun `discarding until the final round ends the game`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6),
            totalRolls = 3
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(1, state.currentRoll)
        assertEquals(1, state.discardCount)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
        assertTrue(state.rewardCards.isEmpty())
    }


    @Test
    fun `game ends before round four when target is no longer reachable`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 3, 4),
            totalRolls = 5,
            maxDiscards = 10
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(3, state.currentRoll)
        assertEquals(3, state.discardCount)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
    }

    @Test
    fun `saving ten keeps the game alive while rolls remain`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(10),
            totalRolls = 5,
            maxDiscards = 10
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete.not())
        assertEquals(listOf(10), state.savedValues)
        assertEquals(2, state.currentRoll)
        assertTrue(state.isAwaitingDecision)
    }
    @Test
    fun `reaching the maximum rolls without success ends the game`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(1, 2),
            totalRolls = 2,
            maxDiscards = 10,
            targetSequence = 2
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        testScheduler.advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.DiscardRoll)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(2, state.currentRoll)
        assertEquals(1, state.discardCount)
        assertEquals(SequenceFailureReason.ROUNDS, state.failureReason)
        assertTrue(state.rewardCards.isEmpty())
    }

    private fun buildViewModel(
        diceRolls: List<Int>,
        rewardRolls: List<Float> = listOf(0.4f, 0.2f, 0.3f),
        totalRolls: Int = 5,
        maxDiscards: Int = 3,
        rewardRevealDelayMs: Long = 0L,
        targetSequence: Int = 3,
        rollAnimationMs: Long = 0L,
        tickMs: Long = 1L,
        saveAnimationMs: Long = 0L
    ): SequenceGameViewModel {
        dispatcher = StandardTestDispatcher()
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollSequenceUseCase(diceRoller)
        val rewardUseCase = SelectMinigameRewardCardsUseCase(
            TestRewardRandomProvider(rewardRolls)
        )
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
