package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
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
        val viewModel = GameViewModel(
            dispatcher = testDispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            diceCount = 3,
            diceType = DiceType.D6,
            layoutSeedProvider = { 0L },
            diceTypeProvider = { _, count -> List(count) { DiceType.D6 } },
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
}
