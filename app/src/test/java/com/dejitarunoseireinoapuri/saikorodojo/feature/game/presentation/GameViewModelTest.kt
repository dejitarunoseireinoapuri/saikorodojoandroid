package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialStateContainsAllCards() {
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher
        )

        val cards = viewModel.uiState.value.cardUiModels

        assertEquals(7, cards.size)
        assertEquals(7, cards.distinct().size)
        assertTrue(cards.all { it.count == 1 })
        assertNull(viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun selectCardUpdatesSelectedCardIndex() {
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher
        )

        viewModel.onEvent(GameUiEvent.SelectCard(1))

        assertEquals(1, viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun selectCardIgnoresInvalidIndex() {
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher
        )

        viewModel.onEvent(GameUiEvent.SelectCard(99))

        assertNull(viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun dismissSelectedCardClearsSelection() {
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher
        )

        viewModel.onEvent(GameUiEvent.SelectCard(0))
        viewModel.onEvent(GameUiEvent.DismissSelectedCard)

        assertNull(viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun applyRerollAllCardRemovesWhenSingleCopy() {
        val rerollAllCard = CardUiModel(
            id = CardId.REROLL_ALL,
            titleRes = R.string.card_reroll_all_title,
            descriptionRes = R.string.card_reroll_all_description,
            iconRes = R.drawable.ic_card_reroll_all,
            count = 1
        )
        val otherCard = CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust,
            count = 1
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            cardUiModels = listOf(rerollAllCard, otherCard)
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        assertEquals(listOf(otherCard), viewModel.uiState.value.cardUiModels)
        assertNull(viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun applyRerollAllCardMovesToBottomWithDecrementedCount() {
        val rerollAllCard = CardUiModel(
            id = CardId.REROLL_ALL,
            titleRes = R.string.card_reroll_all_title,
            descriptionRes = R.string.card_reroll_all_description,
            iconRes = R.drawable.ic_card_reroll_all,
            count = 2
        )
        val otherCard = CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust,
            count = 1
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            cardUiModels = listOf(rerollAllCard, otherCard)
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val updatedCards = viewModel.uiState.value.cardUiModels
        assertEquals(2, updatedCards.size)
        assertEquals(otherCard, updatedCards[0])
        assertEquals(1, updatedCards[1].count)
        assertEquals(CardId.REROLL_ALL, updatedCards[1].id)
        assertNull(viewModel.uiState.value.selectedCardIndex)
    }

    @Test
    fun applyCardIgnoresNonRerollAllCard() {
        val otherCard = CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust,
            count = 1
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            cardUiModels = listOf(otherCard)
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        assertEquals(listOf(otherCard), viewModel.uiState.value.cardUiModels)
    }

    @Test
    fun applyRerollSingleCardEnablesSelectionPrompt() {
        val rerollSingleCard = CardUiModel(
            id = CardId.REROLL_SINGLE,
            titleRes = R.string.card_reroll_single_title,
            descriptionRes = R.string.card_reroll_single_description,
            iconRes = R.drawable.ic_card_reroll_single,
            count = 1
        )
        val otherCard = CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust,
            count = 1
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            cardUiModels = listOf(rerollSingleCard, otherCard)
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        assertEquals(listOf(otherCard), viewModel.uiState.value.cardUiModels)
        assertTrue(viewModel.uiState.value.isAwaitingRerollSingle)
    }

    @Test
    fun diceClickRerollsSingleDieAndClearsPrompt() = runTest(mainDispatcherRule.dispatcher) {
        val rerollSingleCard = CardUiModel(
            id = CardId.REROLL_SINGLE,
            titleRes = R.string.card_reroll_single_title,
            descriptionRes = R.string.card_reroll_single_description,
            iconRes = R.drawable.ic_card_reroll_single,
            count = 1
        )
        val diceTypes = listOf(DiceType.D6, DiceType.D10)
        val viewModel = GameViewModel(
            rollDiceUseCase = RollDiceUseCase(MaxValueRandomProvider()),
            dispatcher = mainDispatcherRule.dispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            diceCount = diceTypes.size,
            diceTypeProvider = { _, _ -> diceTypes },
            cardUiModels = listOf(rerollSingleCard)
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        assertTrue(viewModel.uiState.value.isAwaitingRerollSingle)

        viewModel.onEvent(GameUiEvent.DiceClicked(1))
        advanceUntilIdle()

        val values = viewModel.uiState.value.diceValues
        assertEquals(1, values[0])
        assertEquals(10, values[1])
        assertFalse(viewModel.uiState.value.isAwaitingRerollSingle)
    }

    @Test
    fun startRollUsesDiceTypesForValues() = runTest(mainDispatcherRule.dispatcher) {
        val diceTypes = listOf(DiceType.D8, DiceType.D10)
        val useCase = RollDiceUseCase(MaxValueRandomProvider())
        val viewModel = GameViewModel(
            rollDiceUseCase = useCase,
            dispatcher = mainDispatcherRule.dispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            diceCount = diceTypes.size,
            diceTypeProvider = { _, _ -> diceTypes },
            layoutSeedProvider = { 42L }
        )

        viewModel.onEvent(GameUiEvent.StartRoll)
        advanceUntilIdle()

        assertEquals(diceTypes, viewModel.uiState.value.diceTypes)
        assertEquals(listOf(8, 10), viewModel.uiState.value.diceValues)
    }

    @Test
    fun rerollAllKeepsDiceTypesAndLayout() = runTest(mainDispatcherRule.dispatcher) {
        val rerollAllCard = CardUiModel(
            id = CardId.REROLL_ALL,
            titleRes = R.string.card_reroll_all_title,
            descriptionRes = R.string.card_reroll_all_description,
            iconRes = R.drawable.ic_card_reroll_all,
            count = 1
        )
        val initialDiceTypes = listOf(DiceType.D6, DiceType.D10)
        val alternateDiceTypes = listOf(DiceType.D8, DiceType.D8)
        val seedValues = listOf(42L, 100L).iterator()
        val viewModel = GameViewModel(
            rollDiceUseCase = RollDiceUseCase(MaxValueRandomProvider()),
            dispatcher = mainDispatcherRule.dispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            diceCount = initialDiceTypes.size,
            diceTypeProvider = { seed, _ ->
                if (seed == 42L) initialDiceTypes else alternateDiceTypes
            },
            layoutSeedProvider = { seedValues.next() },
            cardUiModels = listOf(rerollAllCard)
        )

        viewModel.onEvent(GameUiEvent.StartRoll)
        advanceUntilIdle()

        assertEquals(42L, viewModel.uiState.value.layoutSeed)
        assertEquals(initialDiceTypes, viewModel.uiState.value.diceTypes)
        assertEquals(listOf(6, 10), viewModel.uiState.value.diceValues)

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        advanceUntilIdle()

        assertEquals(42L, viewModel.uiState.value.layoutSeed)
        assertEquals(initialDiceTypes, viewModel.uiState.value.diceTypes)
        assertEquals(listOf(6, 10), viewModel.uiState.value.diceValues)
    }
}

private class MaxValueRandomProvider : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return until - 1
    }
}
