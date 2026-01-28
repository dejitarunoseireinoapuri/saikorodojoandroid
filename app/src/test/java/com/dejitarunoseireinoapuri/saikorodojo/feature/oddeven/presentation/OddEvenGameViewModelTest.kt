package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.IntRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.SelectOddEvenRewardCardUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.oddEvenRewardCardIds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals(0, state.correctCount)
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
    fun `winning the game shows a reward card`() = runTest {
        val viewModel = buildViewModel(
            diceRolls = listOf(2, 4, 6),
            rewardIndex = 0
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
        assertNotNull(state.rewardCard)
        assertEquals(CardId.ADJUST_PLUS_MINUS_ONE, state.rewardCard?.id)
    }

    @Test
    fun `three wrong guesses end the game`() = runTest {
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
        assertTrue(state.isComplete)
        assertNull(state.rewardCard)
    }

    private fun buildViewModel(
        diceRolls: List<Int> = listOf(2),
        rewardIndex: Int = 0
    ): OddEvenGameViewModel {
        val diceRoller = SequenceDiceRoller(diceRolls)
        val rollUseCase = RollOddEvenUseCase(diceRoller)
        val rewardUseCase = SelectOddEvenRewardCardUseCase(
            randomProvider = IntRandomProvider { rewardIndex }
        )
        return OddEvenGameViewModel(
            rollOddEvenUseCase = rollUseCase,
            selectOddEvenRewardCardUseCase = rewardUseCase,
            dispatcher = dispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L,
            cardUiModels = testCardUiModels()
        )
    }

    private fun testCardUiModels(): List<CardUiModel> {
        return oddEvenRewardCardIds().map { id ->
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
