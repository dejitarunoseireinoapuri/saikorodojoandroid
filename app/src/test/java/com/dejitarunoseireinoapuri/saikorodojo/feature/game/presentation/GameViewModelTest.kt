package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ConsumeCardFromInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.GetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelDefinition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.InMemoryGameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `reroll some keeps objective selection and awaits selection`() = runTest {
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
        assertEquals(setOf(0, 1), stateAfterApply.selectedDice)
        assertEquals(2, stateAfterApply.selectedDiceSum)
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
    fun `reroll single waits for selection and roll action`() = runTest {
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
        viewModel.onEvent(GameUiEvent.RollSingleDie)
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
    fun `minigames card adds three minigames`() = runTest {
        val viewModel = buildViewModel(
            cardUiModels = listOf(
                CardUiModel(
                    id = CardId.MINIGAMES,
                    titleRes = 0,
                    descriptionRes = 0,
                    iconRes = 0
                )
            )
        )

        val initialMinigames = viewModel.uiState.value.minigamesAvailable
        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val afterApply = viewModel.uiState.value
        assertEquals(initialMinigames + 3, afterApply.minigamesAvailable)
        assertEquals(CardId.MINIGAMES, afterApply.lastAppliedCardId)
        assertTrue(afterApply.cardUiModels.isEmpty())
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
    fun `roll selected dice updates only chosen indices and keeps objective selection`() = runTest {
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

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(2))

        viewModel.onEvent(GameUiEvent.RollSelectedDice)
        testDispatcher.scheduler.advanceUntilIdle()

        val stateAfterRoll = viewModel.uiState.value
        assertEquals(listOf(6, 1, 6), stateAfterRoll.diceValues)
        assertEquals(setOf(0), stateAfterRoll.selectedDice)
        assertTrue(stateAfterRoll.selectedRerollDice.isEmpty())
        assertEquals(6, stateAfterRoll.selectedDiceSum)
        assertTrue(!stateAfterRoll.isAwaitingRerollSelected)
    }

    @Test
    fun `reroll selection keeps objective selection while choosing dice`() = runTest {
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
        assertEquals(setOf(0), viewModel.uiState.value.selectedDice)

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(1))

        val state = viewModel.uiState.value
        assertEquals(setOf(0), state.selectedDice)
        assertEquals(setOf(1), state.selectedRerollDice)
        assertEquals(1, state.selectedDiceSum)
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
        viewModel.onEvent(GameUiEvent.RollSingleDie)
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

        viewModel.onEvent(GameUiEvent.DiceClicked(0))
        viewModel.onEvent(GameUiEvent.DiceClicked(2))
        viewModel.onEvent(GameUiEvent.RollSelectedDice)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val stateAfterRepeat = viewModel.uiState.value
        assertTrue(stateAfterRepeat.isAwaitingRerollSelected)
        assertEquals(setOf(0, 1), stateAfterRepeat.selectedDice)
        assertEquals(2, stateAfterRepeat.selectedDiceSum)
        assertEquals(CardId.REROLL_ALL, stateAfterRepeat.lastAppliedCardId)
        assertTrue(stateAfterRepeat.cardUiModels.isEmpty())
    }

    @Test
    fun `increase dice count appends a new die and clears pending actions`() = runTest {
        val viewModel = buildViewModel()

        viewModel.onEvent(GameUiEvent.ApplyCard(0))
        assertTrue(viewModel.uiState.value.isAwaitingRerollSelected)

        viewModel.onEvent(GameUiEvent.IncreaseDiceCount)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedState = viewModel.uiState.value
        assertEquals(4, updatedState.diceCount)
        assertEquals(4, updatedState.diceValues.size)
        assertEquals(4, updatedState.diceTypes.size)
        assertTrue(!updatedState.isAwaitingRerollSelected)
    }


    @Test
    fun `open random minigame emits navigation effect`() = runTest {
        val viewModel = buildViewModel()
        val effects = mutableListOf<GameUiEffect>()
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(1).toList(effects)
        }

        val startingMinigames = viewModel.uiState.value.minigamesAvailable
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertTrue(effects.single() is GameUiEffect.NavigateToMinigame)
        assertEquals(startingMinigames - 1, viewModel.uiState.value.minigamesAvailable)
        collectorJob.cancel()
    }


    @Test
    fun `open random minigame emits one effect per click`() = runTest {
        val viewModel = buildViewModel()
        val effects = mutableListOf<GameUiEffect>()
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(2).toList(effects)
        }

        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, effects.size)
        assertTrue(effects.all { it is GameUiEffect.NavigateToMinigame })
        assertEquals(1, viewModel.uiState.value.minigamesAvailable)
        collectorJob.cancel()
    }

    @Test
    fun `open random minigame shows ad prompt when empty`() = runTest {
        val viewModel = buildViewModel()
        val effects = mutableListOf<GameUiEffect>()
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(3).toList(effects)
        }

        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)

        assertTrue(viewModel.uiState.value.showMinigamesAdPrompt)
        collectorJob.cancel()
    }

    @Test
    fun `confirm minigames ad grants three minigames`() = runTest {
        val viewModel = buildViewModel()

        val navigationEffects = mutableListOf<GameUiEffect>()
        val navigationCollector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(3).toList(navigationEffects)
        }

        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        viewModel.onEvent(GameUiEvent.OpenRandomMinigame)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.minigamesAvailable)

        val effects = mutableListOf<GameUiEffect>()
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(1).toList(effects)
        }

        viewModel.onEvent(GameUiEvent.ConfirmMinigamesAd(isAdReady = false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, effects.size)
        assertEquals(GameUiEffect.ShowMinigamesRewardedAd, effects.single())
        assertTrue(!viewModel.uiState.value.showMinigamesAdPrompt)
        assertTrue(viewModel.uiState.value.isMinigamesAdLoading)
        assertEquals(0, viewModel.uiState.value.minigamesAvailable)

        viewModel.onEvent(GameUiEvent.MinigamesAdCompleted)

        assertEquals(3, viewModel.uiState.value.minigamesAvailable)
        assertTrue(!viewModel.uiState.value.isMinigamesAdLoading)
        navigationCollector.cancel()
        collectorJob.cancel()
    }

    @Test
    fun `minigames ad display clears loading without granting reward`() = runTest {
        val viewModel = buildViewModel()
        val initialMinigames = viewModel.uiState.value.minigamesAvailable

        viewModel.onEvent(GameUiEvent.ConfirmMinigamesAd(isAdReady = false))
        assertTrue(viewModel.uiState.value.isMinigamesAdLoading)

        viewModel.onEvent(GameUiEvent.MinigamesAdDisplayed)

        assertTrue(!viewModel.uiState.value.isMinigamesAdLoading)
        assertEquals(initialMinigames, viewModel.uiState.value.minigamesAvailable)
    }

    @Test
    fun `confirm minigames ad skips loading when ad is ready`() = runTest {
        val viewModel = buildViewModel()
        val effects = mutableListOf<GameUiEffect>()
        val collectorJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.take(1).toList(effects)
        }

        viewModel.onEvent(GameUiEvent.ConfirmMinigamesAd(isAdReady = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(GameUiEffect.ShowMinigamesRewardedAd), effects)
        assertTrue(!viewModel.uiState.value.isMinigamesAdLoading)
        collectorJob.cancel()
    }

    @Test
    fun `level completion waits before moving to next level`() = runTest {
        val viewModel = buildViewModel()

        val method = GameViewModel::class.java.getDeclaredMethod("handleLevelComplete")
        method.isAccessible = true
        method.invoke(viewModel)

        assertEquals(1, viewModel.uiState.value.levelNumber)

        testDispatcher.scheduler.advanceTimeBy(999)
        assertEquals(1, viewModel.uiState.value.levelNumber)

        testDispatcher.scheduler.advanceTimeBy(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.levelNumber)
        assertEquals(5, viewModel.uiState.value.minigamesAvailable)
    }

    @Test
    fun `level one starts without cards when inventory is empty`() = runTest {
        val viewModel = buildViewModel(cardUiModels = emptyList())

        assertTrue(viewModel.uiState.value.cardUiModels.isEmpty())
    }

    @Test
    fun `surrender clears saved session and blocks later saves`() = runTest {
        val sessionRepository = InMemoryGameSessionRepository()
        val viewModel = buildViewModel(sessionRepository = sessionRepository)

        viewModel.saveSession()
        assertTrue(sessionRepository.loadSession() is SavedSession.MainGame)

        viewModel.onEvent(GameUiEvent.ConfirmSurrender)
        viewModel.saveSession()

        assertEquals(null, sessionRepository.loadSession())
    }

    @Test
    fun `refresh inventory loads cards from repository`() = runTest {
        val repository = InMemoryCardInventoryRepository()
        val viewModel = buildViewModel(
            cardUiModels = emptyList(),
            cardInventoryRepository = repository
        )

        assertTrue(viewModel.uiState.value.cardUiModels.isEmpty())

        repository.addCards(listOf(CardId.REROLL_ALL))

        viewModel.onEvent(GameUiEvent.RefreshInventory)

        val updatedCards = viewModel.uiState.value.cardUiModels
        assertEquals(1, updatedCards.size)
        assertEquals(CardId.REROLL_ALL, updatedCards.first().id)
        assertEquals(1, updatedCards.first().count)
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
        ),
        cardInventoryRepository: InMemoryCardInventoryRepository = InMemoryCardInventoryRepository(),
        sessionRepository: GameSessionRepository = InMemoryGameSessionRepository()
    ): GameViewModel {
        val levelDefinition = LevelDefinition(
            levelNumber = 1,
            diceCount = 3,
            diceTypes = List(3) { DiceType.D6 }
        )
        return GameViewModel(
            rollDiceUseCase = rollDiceUseCase,
            getCardInventoryUseCase = GetCardInventoryUseCase(cardInventoryRepository),
            consumeCardFromInventoryUseCase = ConsumeCardFromInventoryUseCase(cardInventoryRepository),
            loadGameSessionUseCase = LoadGameSessionUseCase(sessionRepository),
            saveGameSessionUseCase = SaveGameSessionUseCase(sessionRepository),
            clearGameSessionUseCase = ClearGameSessionUseCase(sessionRepository),
            dispatcher = testDispatcher,
            rollDurationMs = 1L,
            tickMs = 1L,
            layoutSeedProvider = { 0L },
            initialLevelDefinition = levelDefinition,
            cardUiModels = cardUiModels
        )
    }

    private class FixedRandomProvider(private val value: Int) : DiceRandomProvider {
        override fun nextInt(from: Int, until: Int): Int {
            return value
        }
    }
}
