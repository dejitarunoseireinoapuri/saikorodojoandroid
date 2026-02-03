package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelDefinition
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelSetValueTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps group selection while choosing die to set value`() {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 1,
            diceTypes = listOf(DiceType.D6)
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            initialLevelDefinition = levelDefinition,
            cardUiModels = listOf(setValueCard())
        )

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))

        val state = viewModel.uiState.value
        assertTrue(state.isAwaitingSetValue)
        assertEquals(setOf(0), state.selectedDice)
        assertEquals(0, state.selectedSetValueDieIndex)
    }

    @Test
    fun `sets the selected die value and consumes the card`() {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 1,
            diceTypes = listOf(DiceType.D6)
        )
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            initialLevelDefinition = levelDefinition,
            cardUiModels = listOf(setValueCard(count = 2))
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val afterApply = viewModel.uiState.value
        assertTrue(afterApply.isAwaitingSetValue)
        assertEquals(1, afterApply.cardUiModels.single().count)

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.SetSelectedDieValue(5))

        val afterSet = viewModel.uiState.value
        assertEquals(5, afterSet.diceValues.first())
        assertTrue(!afterSet.isAwaitingSetValue)
    }

    private fun setValueCard(count: Int = 1): CardUiModel {
        return CardUiModel(
            id = CardId.SET_VALUE,
            titleRes = R.string.card_set_value_title,
            descriptionRes = R.string.card_set_value_description,
            iconRes = R.drawable.ic_card_set_value,
            count = count
        )
    }
}
