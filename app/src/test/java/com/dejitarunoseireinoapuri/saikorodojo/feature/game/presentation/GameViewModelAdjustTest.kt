package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ConsumeCardFromInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.GetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelDefinition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelAdjustTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `keeps group selection while choosing die to adjust`() {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 1,
            diceTypes = listOf(DiceType.D6)
        )
        val repository = InMemoryCardInventoryRepository()
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            getCardInventoryUseCase = GetCardInventoryUseCase(repository),
            consumeCardFromInventoryUseCase = ConsumeCardFromInventoryUseCase(repository),
            initialLevelDefinition = levelDefinition,
            cardUiModels = listOf(adjustCard())
        )

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))

        val state = viewModel.uiState.value
        assertTrue(state.isAwaitingAdjustPlusMinus)
        assertEquals(setOf(0), state.selectedDice)
        assertEquals(0, state.selectedAdjustmentDieIndex)
    }

    @Test
    fun `does not reduce below one when adjusting down`() {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 1,
            diceTypes = listOf(DiceType.D6)
        )
        val repository = InMemoryCardInventoryRepository()
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            getCardInventoryUseCase = GetCardInventoryUseCase(repository),
            consumeCardFromInventoryUseCase = ConsumeCardFromInventoryUseCase(repository),
            initialLevelDefinition = levelDefinition,
            cardUiModels = listOf(adjustCard())
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.AdjustSelectedDie(-1))

        val state = viewModel.uiState.value
        assertEquals(1, state.diceValues.first())
        assertFalse(state.isAwaitingAdjustPlusMinus)
    }

    @Test
    fun `does not increase above max when adjusting up`() = runTest {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 1,
            diceTypes = listOf(DiceType.D6)
        )
        val repository = InMemoryCardInventoryRepository()
        val viewModel = GameViewModel(
            rollDiceUseCase = RollDiceUseCase(FixedDiceRandomProvider(6)),
            dispatcher = mainDispatcherRule.dispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            getCardInventoryUseCase = GetCardInventoryUseCase(repository),
            consumeCardFromInventoryUseCase = ConsumeCardFromInventoryUseCase(repository),
            initialLevelDefinition = levelDefinition,
            cardUiModels = listOf(adjustCard())
        )

        viewModel.onEvent(GameUiEvent.StartRoll)
        advanceUntilIdle()

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.AdjustSelectedDie(1))

        val state = viewModel.uiState.value
        assertEquals(6, state.diceValues.first())
        assertFalse(state.isAwaitingAdjustPlusMinus)
    }

    private fun adjustCard(): CardUiModel {
        return CardUiModel(
            id = CardId.ADJUST_PLUS_MINUS_ONE,
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            iconRes = R.drawable.ic_card_adjust
        )
    }
}

private class FixedDiceRandomProvider(private val value: Int) : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return value
    }
}
