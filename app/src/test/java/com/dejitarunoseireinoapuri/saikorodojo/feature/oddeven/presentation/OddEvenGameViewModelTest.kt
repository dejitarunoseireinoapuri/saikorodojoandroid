package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.rewardCardIds
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OddEvenGameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Test
    fun `start game initializes first round`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)

        val state = viewModel.uiState.value
        assertTrue(state.isStarted)
        assertEquals(1, state.currentRound)
        assertEquals(7, state.totalRounds)
        assertEquals(0, state.correctCount)
        assertNull(state.diceValue)
    }

    @Test
    fun `selecting a correct choice increments round and score`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertEquals(1, state.correctCount)
        assertNull(state.selectedChoice)
        assertEquals(2, state.diceValue)
    }


    @Test
    fun `non-terminal rounds do not wait for result animation to show choices again`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2),
            resultAnimationMs = 1_500L
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.runCurrent()

        val state = viewModel.uiState.value
        assertEquals(2, state.currentRound)
        assertNull(state.selectedChoice)
        assertTrue(!state.isComplete)
        assertTrue(state.showFireworks)
    }

    @Test
    fun `winning the game shows reward cards`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6),
            rewardRolls = listOf(0.4f, 0.2f, 0.3f)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.rewardCards.isNotEmpty())
        assertEquals(
            listOf(CardId.ADJUST_PLUS_MINUS_ONE, CardId.FLIP_FACE),
            state.rewardCards.map { it.id }
        )
    }

    @Test
    fun `three wrong guesses do not end the game early`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete.not())
        assertTrue(state.rewardCards.isEmpty())
    }

    @Test
    fun `losing after seven rounds completes the game`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6, 2, 4, 6, 2)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        repeat(7) {
            viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
            dispatcher.scheduler.advanceUntilIdle()
        }

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertTrue(state.rewardCards.isEmpty())
    }

    @Test
    fun `success state persists until next roll`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        dispatcher.scheduler.advanceUntilIdle()

        val afterWinState = viewModel.uiState.value
        assertTrue(afterWinState.showFireworks)
        assertTrue(!afterWinState.isRolling)

        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))

        val nextRollState = viewModel.uiState.value
        assertTrue(!nextRollState.showFireworks)
        assertTrue(nextRollState.isRolling)
    }

    @Test
    fun `failure state persists until next roll`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4)
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.advanceUntilIdle()

        val afterLossState = viewModel.uiState.value
        assertTrue(afterLossState.showFailure)
        assertTrue(!afterLossState.isRolling)

        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))

        val nextRollState = viewModel.uiState.value
        assertTrue(!nextRollState.showFailure)
        assertTrue(nextRollState.isRolling)
    }

    @Test
    fun `loss completion waits for delay before ending`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2),
            targetCorrect = 1,
            lossMessageDelayMs = 2_000L
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.runCurrent()

        val interimState = viewModel.uiState.value
        assertTrue(interimState.isStarted)
        assertTrue(interimState.showFailure)
        assertTrue(!interimState.isComplete)

        dispatcher.scheduler.advanceTimeBy(2_000L)
        dispatcher.scheduler.runCurrent()

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isComplete)
        assertTrue(finalState.rewardCards.isEmpty())
    }

    @Test
    fun `loss completion uses default delay`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2),
            targetCorrect = 1,
            lossMessageDelayMs = null
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.ODD))
        dispatcher.scheduler.runCurrent()

        val interimState = viewModel.uiState.value
        assertTrue(interimState.isStarted)
        assertTrue(interimState.showFailure)
        assertTrue(!interimState.isComplete)

        dispatcher.scheduler.advanceTimeBy(999L)
        dispatcher.scheduler.runCurrent()

        val beforeDelayState = viewModel.uiState.value
        assertTrue(!beforeDelayState.isComplete)

        dispatcher.scheduler.advanceTimeBy(1L)
        dispatcher.scheduler.runCurrent()

        val finalState = viewModel.uiState.value
        assertTrue(finalState.isComplete)
        assertTrue(finalState.rewardCards.isEmpty())
    }

    private fun buildViewModel(
        diceRolls: List<Int> = listOf(2),
        rewardRolls: List<Float> = listOf(0.4f, 0.2f, 0.3f),
        targetCorrect: Int = 3,
        resultAnimationMs: Long = 0L,
        lossMessageDelayMs: Long? = 0L
    ): OddEvenGameViewModel {
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollOddEvenUseCase(diceRoller)
        val rewardUseCase = SelectMinigameRewardCardsUseCase(
            TestRewardRandomProvider(rewardRolls)
        )
        return if (lossMessageDelayMs == null) {
            OddEvenGameViewModel(
                rollOddEvenUseCase = rollUseCase,
                selectMinigameRewardCardsUseCase = rewardUseCase,
                dispatcher = dispatcher,
                rollAnimationMs = 0L,
                resultAnimationMs = resultAnimationMs,
                tickMs = 1L,
                targetCorrect = targetCorrect,
                cardUiModels = testCardUiModels()
            )
        } else {
            OddEvenGameViewModel(
                rollOddEvenUseCase = rollUseCase,
                selectMinigameRewardCardsUseCase = rewardUseCase,
                dispatcher = dispatcher,
                rollAnimationMs = 0L,
                resultAnimationMs = resultAnimationMs,
                tickMs = 1L,
                targetCorrect = targetCorrect,
                lossMessageDelayMs = lossMessageDelayMs,
                cardUiModels = testCardUiModels()
            )
        }
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
