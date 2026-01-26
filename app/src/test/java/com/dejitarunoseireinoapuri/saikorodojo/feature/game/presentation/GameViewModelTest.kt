package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
}
