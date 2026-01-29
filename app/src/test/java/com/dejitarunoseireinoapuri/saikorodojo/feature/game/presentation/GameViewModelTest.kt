package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `reroll some clears selection and awaits selection`() = runTest {
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
        assertTrue(stateAfterApply.isAwaitingRerollSelected)
        assertTrue(stateAfterApply.selectedDice.isEmpty())
        assertEquals(0, stateAfterApply.selectedDiceSum)
        assertTrue(!stateAfterApply.isRolling)
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
        testDispatcher.scheduler.advanceUntilIdle()

        val afterRoll = viewModel.uiState.value
        assertEquals(6, afterRoll.diceValues[1])
        assertTrue(!afterRoll.isAwaitingRerollSingle)
    }

    @Test
    fun `reroll single keeps card order while decrementing count`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_SINGLE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0,
                    count = 2
                ),
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val updatedCards = viewModel.uiState.value.cardUiModels
        assertEquals(CardId.REROLL_SINGLE, updatedCards[0].id)
        assertEquals(1, updatedCards[0].count)
        assertEquals(CardId.REROLL_ALL, updatedCards[1].id)
    }

    @Test
    fun `retry resets dice to the initial roll state`() = runTest {
        val viewModel = buildViewModel(
            rollDiceUseCase = RollDiceUseCase(FixedRandomProvider(6)),
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.SET_VALUE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                ),
                CardUiModel(
                    id = CardId.RETRY,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.StartRoll)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialValues = viewModel.uiState.value.diceValues
        assertEquals(listOf(6, 6, 6), initialValues)

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.SetSelectedDieValue(1))

        assertEquals(1, viewModel.uiState.value.diceValues[0])

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val afterRetry = viewModel.uiState.value
        assertEquals(initialValues, afterRetry.diceValues)
        assertTrue(afterRetry.selectedDice.isEmpty())
        assertEquals(0, afterRetry.selectedDiceSum)
        assertEquals(CardId.RETRY, afterRetry.lastAppliedCardId)
        assertTrue(!afterRetry.isAwaitingSetValue)
        assertTrue(!afterRetry.isAwaitingRerollSingle)
    }

    @Test
    fun `card interactions are ignored while awaiting single reroll`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_SINGLE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0,
                    count = 1
                ),
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val awaitingState = viewModel.uiState.value
        assertTrue(awaitingState.isAwaitingRerollSingle)
        assertEquals(1, awaitingState.cardUiModels.size)
        assertEquals(CardId.REROLL_ALL, awaitingState.cardUiModels.first().id)

        viewModel.onEvent(GameUiEvent.SelectCard(0))
        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DismissSelectedCard)

        val blockedState = viewModel.uiState.value
        assertEquals(null, blockedState.selectedCardIndex)
        assertEquals(1, blockedState.cardUiModels.size)
        assertEquals(CardId.REROLL_ALL, blockedState.cardUiModels.first().id)
    }

    @Test
    fun `reroll all keeps card order while decrementing count`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0,
                    count = 2
                ),
                CardUiModel(
                    id = CardId.REROLL_SINGLE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val updatedCards = viewModel.uiState.value.cardUiModels
        assertEquals(CardId.REROLL_ALL, updatedCards[0].id)
        assertEquals(1, updatedCards[0].count)
        assertEquals(CardId.REROLL_SINGLE, updatedCards[1].id)
    }

    @Test
    fun `roll selected dice updates only chosen indices and clears selection`() = runTest {
        val viewModel = buildViewModel(
            rollDiceUseCase = RollDiceUseCase(FixedRandomProvider(6)),
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(2))

        viewModel.onEvent(GameUiEvent.RollSelectedDice)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterRoll = viewModel.uiState.value
        assertEquals(listOf(6, 1, 6), stateAfterRoll.diceValues)
        assertTrue(stateAfterRoll.selectedDice.isEmpty())
        assertEquals(0, stateAfterRoll.selectedDiceSum)
        assertTrue(!stateAfterRoll.isAwaitingRerollSelected)
    }

    @Test
    fun `repeat last reapplies the previous card effect and consumes repeat card`() = runTest {
        val viewModel = buildViewModel(
            rollDiceUseCase = RollDiceUseCase(FixedRandomProvider(6)),
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_SINGLE,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                ),
                CardUiModel(
                    id = CardId.REPEAT_LAST,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        assertTrue(viewModel.uiState.value.isAwaitingRerollSingle)
        assertEquals(CardId.REROLL_SINGLE, viewModel.uiState.value.lastAppliedCardId)

        viewModel.onEvent(GameUiEvent.DiceClicked(1))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val stateAfterRepeat = viewModel.uiState.value
        assertTrue(stateAfterRepeat.isAwaitingRerollSingle)
        assertEquals(CardId.REROLL_SINGLE, stateAfterRepeat.lastAppliedCardId)
        assertTrue(stateAfterRepeat.cardUiModels.isEmpty())
    }

    @Test
    fun `repeat last reopens reroll selection when the last card was reroll some`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.REROLL_ALL,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                ),
                CardUiModel(
                    id = CardId.REPEAT_LAST,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(1))
        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val stateAfterRepeat = viewModel.uiState.value
        assertTrue(stateAfterRepeat.isAwaitingRerollSelected)
        assertTrue(stateAfterRepeat.selectedDice.isEmpty())
        assertEquals(0, stateAfterRepeat.selectedDiceSum)
        assertEquals(CardId.REROLL_ALL, stateAfterRepeat.lastAppliedCardId)
        assertTrue(stateAfterRepeat.cardUiModels.isEmpty())
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
