package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `reroll all clears selection before rolling`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(1))

        val selectedBefore = viewModel.uiState.value.selectedDice
        assertEquals(setOf(0, 1), selectedBefore)

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val stateAfterApply = viewModel.uiState.value
        assertTrue(stateAfterApply.selectedDice.isEmpty())
        assertEquals(0, stateAfterApply.selectedDiceSum)

        advanceUntilIdle()

        val stateAfterRoll = viewModel.uiState.value
        assertTrue(stateAfterRoll.selectedDice.isEmpty())
        assertEquals(0, stateAfterRoll.selectedDiceSum)
    }

    @Test
    fun `dice selection toggles and ignores invalid indices`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        assertEquals(setOf(0), viewModel.uiState.value.selectedDice)
        assertEquals(1, viewModel.uiState.value.selectedDiceSum)

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        assertTrue(viewModel.uiState.value.selectedDice.isEmpty())
        assertEquals(0, viewModel.uiState.value.selectedDiceSum)

        viewModel.onEvent(GameUiEvent.DiceClicked(99))
        assertTrue(viewModel.uiState.value.selectedDice.isEmpty())
    }

    @Test
    fun `selecting and dismissing a card updates selection`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.SelectCard(0))
        assertEquals(0, viewModel.uiState.value.selectedCardIndex)

        viewModel.onEvent(GameUiEvent.DismissSelectedCard)
        assertEquals(null, viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun `reroll single waits for a die and updates card count`() = runTest {
        val viewModel = buildViewModel(
            rollDiceUseCase = RollDiceUseCase(FixedRandomProvider(6)),
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_SINGLE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0,
                    count = 2
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val afterApply = viewModel.uiState.value
        assertTrue(afterApply.isAwaitingRerollSingle)
        assertEquals(1, afterApply.cardUiModels.single().count)

        viewModel.onEvent(GameUiEvent.DiceClicked(1))
        advanceUntilIdle()

        val afterRoll = viewModel.uiState.value
        assertEquals(6, afterRoll.diceValues[1])
        assertTrue(!afterRoll.isAwaitingRerollSingle)
    }

    private fun buildViewModel(
        rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(FixedRandomProvider(1)),
        cardUiModels: List<CardUiModel> = listOf(
            CardUiModel(
                id = CardId.REROLL_ALL,
                titleRes = 0,
                descriptionRes = 0,
                iconRes = 0
            )
        )
    ): GameViewModel {
        return GameViewModel(
            rollDiceUseCase = rollDiceUseCase,
            dispatcher = testDispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            diceCount = 3,
            diceType = DiceType.D6,
            layoutSeedProvider = { 0L },
            diceTypeProvider = { _, count -> List(count) { DiceType.D6 } },
            cardUiModels = cardUiModels
        )
    }

    private class FixedRandomProvider(private val value: Int) : DiceRandomProvider {
        override fun nextInt(from: Int, until: Int): Int {
            return value
        }
    }
}
