package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ConsumeCardFromInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.GetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateLevelUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateObjectiveUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelDefinition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ObjectiveCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumAtLeastCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumAtMostCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumInRangeCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumMaxDifferenceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumMultipleCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumParityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasPairCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasThreeOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasFourOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.FullHouseCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AtLeastParityCountCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsHighAndLowValueCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.StraightCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesWithMultiplicityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactTwoPairsCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactlyDistinctValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ForbidValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SatisfyAndAvoidCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ThreeOfKindWithValueCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavePendingMainGameSnapshotUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val DEFAULT_DICE_COUNT = 5
private const val DEFAULT_ROLL_DURATION_MS = 1_000L
private const val DEFAULT_TICK_MS = 150L
private const val LEVEL_COMPLETE_DELAY_MS = 1_000L
private const val DEFAULT_MINIGAMES_AVAILABLE = 3
private const val MINIGAMES_REWARD_AMOUNT = 3

data class GameUiState(
    val diceValues: List<Int> = List(DEFAULT_DICE_COUNT) { 1 },
    val diceCount: Int = DEFAULT_DICE_COUNT,
    val diceType: DiceType = DiceType.D6,
    val diceTypes: List<DiceType> = List(DEFAULT_DICE_COUNT) { DiceType.D6 },
    val layoutSeed: Long = 0L,
    val isRolling: Boolean = false,
    val isAwaitingRerollSingle: Boolean = false,
    val isAwaitingRerollSelected: Boolean = false,
    val isAwaitingFlipFace: Boolean = false,
    val isAwaitingAdjustPlusMinus: Boolean = false,
    val isAwaitingSetValue: Boolean = false,
    val selectedDice: Set<Int> = emptySet(),
    val selectedRerollDice: Set<Int> = emptySet(),
    val selectedRerollSingleDieIndex: Int? = null,
    val selectedFlipDieIndex: Int? = null,
    val selectedAdjustmentDieIndex: Int? = null,
    val selectedSetValueDieIndex: Int? = null,
    val selectedDiceSum: Int = 0,
    val shouldShowSelectedSum: Boolean = false,
    val cardUiModels: List<CardUiModel> = emptyList(),
    val selectedCardIndex: Int? = null,
    val lastAppliedCardId: CardId? = null,
    val levelNumber: Int = 1,
    val objectiveLines: List<ObjectiveLineUiState> = emptyList(),
    val isLevelComplete: Boolean = false,
    val showLevelCompleteMessage: Boolean = false,
    val minigamesAvailable: Int = DEFAULT_MINIGAMES_AVAILABLE,
    val showMinigamesAdPrompt: Boolean = false
)

sealed interface GameUiEvent {
    data object StartRoll : GameUiEvent
    data class DiceClicked(val index: Int) : GameUiEvent
    data class SelectCard(val index: Int) : GameUiEvent
    data class ApplyCard(val index: Int) : GameUiEvent
    data class AdjustSelectedDie(val delta: Int) : GameUiEvent
    data class SetSelectedDieValue(val value: Int) : GameUiEvent
    data object RollSelectedDice : GameUiEvent
    data object RollSingleDie : GameUiEvent
    data object FlipSelectedDie : GameUiEvent
    data object DismissSelectedCard : GameUiEvent
    data object IncreaseDiceCount : GameUiEvent
    data object ConfirmSurrender : GameUiEvent
    data object ConfirmExit : GameUiEvent
    data object OpenRandomMinigame : GameUiEvent
    data object ConfirmMinigamesAd : GameUiEvent
    data object DismissMinigamesAdPrompt : GameUiEvent
    data object MinigamesAdCompleted : GameUiEvent
}

sealed interface GameUiEffect {
    data class NavigateToMinigame(val minigame: MinigameType) : GameUiEffect
    data class NavigateToMenu(val resetProgress: Boolean) : GameUiEffect
    data object ShowMinigamesRewardedAd : GameUiEffect
}

data class ObjectiveLineUiState(
    val text: ObjectiveLineText,
    val isMet: Boolean
)

sealed interface ObjectiveLineText {
    data class StringRes(
        val resId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ObjectiveLineText

    data class PluralRes(
        val resId: Int,
        val quantity: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ObjectiveLineText
}

class GameViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val generateLevelUseCase: GenerateLevelUseCase = GenerateLevelUseCase(),
    private val generateObjectiveUseCase: GenerateObjectiveUseCase = GenerateObjectiveUseCase(),
    private val getCardInventoryUseCase: GetCardInventoryUseCase =
        GetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val consumeCardFromInventoryUseCase: ConsumeCardFromInventoryUseCase =
        ConsumeCardFromInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val setCardInventoryUseCase: SetCardInventoryUseCase =
        SetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val loadGameSessionUseCase: LoadGameSessionUseCase =
        LoadGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val saveGameSessionUseCase: SaveGameSessionUseCase =
        SaveGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val clearGameSessionUseCase: ClearGameSessionUseCase =
        ClearGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val savePendingMainGameSnapshotUseCase: SavePendingMainGameSnapshotUseCase =
        SavePendingMainGameSnapshotUseCase(GameSessionRepositoryProvider.provide()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollDurationMs: Long = DEFAULT_ROLL_DURATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val layoutSeedProvider: () -> Long = { Random.Default.nextLong() },
    private val baseSeedProvider: () -> Long = { Random.Default.nextLong() },
    private val initialLevelDefinition: LevelDefinition? = null,
    cardUiModels: List<CardUiModel> = emptyList()
) : ViewModel() {
    private var baseSeed = baseSeedProvider()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private val _effects = MutableSharedFlow<GameUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<GameUiEffect> = _effects

    private var rollJob: Job? = null
    private var initialRollSnapshot: GameRollSnapshot? = null
    private var completionJob: Job? = null
    private var currentObjective: LevelObjective? = null
    private var currentLevelNumber: Int = 1
    private var shouldAutoStartRoll = true

    init {
        val restoredSession = loadGameSessionUseCase.execute()
        val restoredSnapshot = when (restoredSession) {
            is SavedSession.MainGame -> restoredSession.snapshot
            is SavedSession.Minigame -> restoredSession.mainGameSnapshot
            null -> null
        }
        if (restoredSnapshot != null) {
            restoreFromSnapshot(restoredSnapshot)
            if (restoredSession is SavedSession.Minigame) {
                savePendingMainGameSnapshotUseCase.execute(restoredSnapshot)
            }
            shouldAutoStartRoll = false
        } else {
            val initialCards = cardUiModels.ifEmpty { loadInventoryCardModels() }
            applyLevelDefinition(
                levelDefinition = initialLevelDefinition
                    ?: generateLevelUseCase.execute(levelNumber = 1, seedBase = baseSeed),
                cardUiModels = initialCards
            )
        }
    }

    fun onEvent(event: GameUiEvent) {
        when (event) {
            GameUiEvent.StartRoll -> startRolling()
            is GameUiEvent.DiceClicked -> handleDiceClick(event.index)
            is GameUiEvent.SelectCard -> {
                if (!isCardInteractionBlocked()) {
                    selectCard(event.index)
                }
            }
            is GameUiEvent.ApplyCard -> {
                if (!isCardInteractionBlocked()) {
                    applyCard(event.index)
                }
            }
            is GameUiEvent.AdjustSelectedDie -> adjustSelectedDie(event.delta)
            is GameUiEvent.SetSelectedDieValue -> setSelectedDieValue(event.value)
            GameUiEvent.RollSelectedDice -> rollSelectedDice()
            GameUiEvent.RollSingleDie -> rollSingleDie()
            GameUiEvent.FlipSelectedDie -> flipSelectedDie()
            GameUiEvent.DismissSelectedCard -> {
                if (!isCardInteractionBlocked()) {
                    dismissSelectedCard()
                }
            }
            GameUiEvent.IncreaseDiceCount -> increaseDiceCount()
            GameUiEvent.ConfirmSurrender -> confirmSurrender()
            GameUiEvent.ConfirmExit -> confirmExit()
            GameUiEvent.OpenRandomMinigame -> openRandomMinigame()
            GameUiEvent.ConfirmMinigamesAd -> confirmMinigamesAd()
            GameUiEvent.DismissMinigamesAdPrompt -> dismissMinigamesAdPrompt()
            GameUiEvent.MinigamesAdCompleted -> grantMinigamesFromAd()
        }
    }

    fun saveSession() {
        val snapshot = buildMainGameSnapshot()
        saveGameSessionUseCase.execute(SavedSession.MainGame(snapshot))
    }

    private fun isCardInteractionBlocked(): Boolean {
        return _uiState.value.isAwaitingRerollSingle ||
            _uiState.value.isAwaitingRerollSelected ||
            _uiState.value.isAwaitingFlipFace ||
            _uiState.value.isAwaitingAdjustPlusMinus ||
            _uiState.value.isAwaitingSetValue
    }


    private fun openRandomMinigame() {
        val currentState = _uiState.value
        if (currentState.minigamesAvailable <= 0) {
            _uiState.update { it.copy(showMinigamesAdPrompt = true) }
            return
        }
        _uiState.update {
            it.copy(
                minigamesAvailable = (it.minigamesAvailable - 1).coerceAtLeast(0),
                showMinigamesAdPrompt = false
            )
        }
        val snapshot = buildMainGameSnapshot()
        savePendingMainGameSnapshotUseCase.execute(snapshot)
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMinigame(pickMinigame()))
        }
    }

    private fun confirmMinigamesAd() {
        _uiState.update { it.copy(showMinigamesAdPrompt = false) }
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.ShowMinigamesRewardedAd)
        }
    }

    private fun dismissMinigamesAdPrompt() {
        _uiState.update { it.copy(showMinigamesAdPrompt = false) }
    }

    private fun grantMinigamesFromAd() {
        _uiState.update {
            it.copy(minigamesAvailable = it.minigamesAvailable + MINIGAMES_REWARD_AMOUNT)
        }
    }

    private fun confirmSurrender() {
        clearGameSessionUseCase.execute()
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMenu(resetProgress = true))
        }
    }

    private fun confirmExit() {
        val snapshot = buildMainGameSnapshot()
        saveGameSessionUseCase.execute(SavedSession.MainGame(snapshot))
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMenu(resetProgress = false))
        }
    }

    private fun startRolling(keepLayout: Boolean = false) {
        if (rollJob?.isActive == true) return
        if (!keepLayout && initialRollSnapshot != null && currentObjective != null) {
            refreshCardInventory()
            return
        }

        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            val currentState = _uiState.value
            val seed = if (keepLayout) currentState.layoutSeed else layoutSeedProvider()
            val diceTypes = currentState.diceTypes
            _uiState.update {
                it.copy(
                    isRolling = true,
                    layoutSeed = seed,
                    diceTypes = diceTypes,
                    isAwaitingRerollSingle = false,
                    isAwaitingRerollSelected = false,
                    isAwaitingFlipFace = false,
                    isAwaitingAdjustPlusMinus = false,
                    isAwaitingSetValue = false,
                    selectedRerollSingleDieIndex = null,
                    selectedFlipDieIndex = null,
                    selectedAdjustmentDieIndex = null,
                    selectedSetValueDieIndex = null
                )
            }

            repeat(steps) {
                val values = rollDiceUseCase.execute(diceTypes)
                _uiState.update {
                    it.copy(
                        diceValues = values,
                        selectedDiceSum = calculateSelectedDiceSum(values, it.selectedDice)
                    )
                }
                delay(tickMs)
            }

            _uiState.update { it.copy(isRolling = false) }
            if (initialRollSnapshot == null) {
                val snapshotState = _uiState.value
                initialRollSnapshot = GameRollSnapshot(
                    diceValues = snapshotState.diceValues.toList(),
                    diceTypes = snapshotState.diceTypes.toList(),
                    layoutSeed = snapshotState.layoutSeed
                )
            }
            if (currentObjective == null) {
                currentObjective = generateObjectiveUseCase.execute(
                    levelNumber = currentLevelNumber,
                    diceTypes = _uiState.value.diceTypes,
                    seedBase = baseSeed
                )
            }
            refreshObjectiveProgress()
        }
    }

    private fun handleDiceClick(index: Int) {
        val state = _uiState.value
        if (state.isAwaitingRerollSingle) {
            selectRerollSingleDie(index)
        } else if (state.isAwaitingRerollSelected) {
            toggleRerollDiceSelection(index)
        } else if (state.isAwaitingFlipFace) {
            selectFlipDie(index)
        } else if (state.isAwaitingAdjustPlusMinus) {
            selectAdjustmentDie(index)
        } else if (state.isAwaitingSetValue) {
            selectSetValueDie(index)
        } else {
            toggleDiceSelection(index)
        }
    }

    private fun toggleRerollDiceSelection(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedSelection = if (state.selectedRerollDice.contains(index)) {
                    state.selectedRerollDice - index
                } else {
                    state.selectedRerollDice + index
                }
                state.copy(selectedRerollDice = updatedSelection)
            }
        }
    }

    private fun toggleDiceSelection(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedSelection = if (state.selectedDice.contains(index)) {
                    state.selectedDice - index
                } else {
                    state.selectedDice + index
                }
                state.copy(
                    selectedDice = updatedSelection,
                    selectedDiceSum = calculateSelectedDiceSum(state.diceValues, updatedSelection)
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun selectCard(index: Int) {
        _uiState.update { state ->
            if (index !in state.cardUiModels.indices) {
                state
            } else {
                state.copy(selectedCardIndex = index)
            }
        }
    }

    private fun dismissSelectedCard() {
        _uiState.update { it.copy(selectedCardIndex = null) }
    }

    private fun increaseDiceCount() {
        val currentState = _uiState.value
        if (currentState.isRolling) return
        val newCount = currentState.diceCount + 1
        val newDiceValues = currentState.diceValues + 1
        val newDiceTypes = currentState.diceTypes + currentState.diceType
        val updatedSelection = currentState.selectedDice.filter { it < newCount }.toSet()
        _uiState.update { state ->
            state.copy(
                diceCount = newCount,
                diceValues = newDiceValues,
                diceTypes = newDiceTypes,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedDice = updatedSelection,
                selectedDiceSum = calculateSelectedDiceSum(newDiceValues, updatedSelection),
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
        }
        initialRollSnapshot = null
        startRolling(keepLayout = false)
    }

    private fun applyCard(index: Int) {
        val repeatedCardId = applyRepeatLastCard(index)
        if (repeatedCardId != null) {
            return
        }
        val applied = applyRerollAllCard(index)
        if (applied) {
            return
        }
        if (applyRerollSingleCard(index)) {
            return
        }
        if (applyFlipFaceCard(index)) {
            return
        }
        if (applyAdjustPlusMinusCard(index)) {
            return
        }
        if (applyMinigamesCard(index)) {
            return
        }
        applySetValueCard(index)
    }

    private fun applyRepeatLastCard(index: Int): CardId? {
        var repeatedCardId: CardId? = null
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                val lastCardId = state.lastAppliedCardId
                if (card.id != CardId.REPEAT_LAST || lastCardId == null) {
                    state
                } else {
                    repeatedCardId = lastCardId
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, lastCardId)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = lastCardId
                    )
                }
            }
        }
        return repeatedCardId
    }

    private fun applyRerollSingleCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.REROLL_SINGLE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, card.id)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyAdjustPlusMinusCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.ADJUST_PLUS_MINUS_ONE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, card.id)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applySetValueCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.SET_VALUE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, card.id)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyMinigamesCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.MINIGAMES) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = state.copy(
                        selectedCardIndex = null,
                        minigamesAvailable = state.minigamesAvailable + MINIGAMES_REWARD_AMOUNT,
                        showMinigamesAdPrompt = false
                    )
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun selectRerollSingleDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedRerollSingleDieIndex = index)
            }
        }
    }

    private fun rollSingleDie() {
        val state = _uiState.value
        val selectedIndex = state.selectedRerollSingleDieIndex
        if (!state.isAwaitingRerollSingle || selectedIndex == null || rollJob?.isActive == true) return
        val diceType = state.diceTypes.getOrElse(selectedIndex) { state.diceType }
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            _uiState.update {
                it.copy(
                    isRolling = true,
                    isAwaitingRerollSingle = false,
                    selectedRerollSingleDieIndex = null
                )
            }
            repeat(steps) {
                val value = rollDiceUseCase.execute(listOf(diceType)).first()
                _uiState.update { currentState ->
                    val updatedValues = currentState.diceValues.toMutableList()
                    if (selectedIndex in updatedValues.indices) {
                        updatedValues[selectedIndex] = value
                    }
                    currentState.copy(
                        diceValues = updatedValues,
                        selectedDiceSum = calculateSelectedDiceSum(updatedValues, currentState.selectedDice)
                    )
                }
                delay(tickMs)
            }
            _uiState.update { it.copy(isRolling = false) }
            refreshObjectiveProgress()
        }
    }

    private fun selectFlipDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedIndex = if (state.selectedFlipDieIndex == index) null else index
                state.copy(selectedFlipDieIndex = updatedIndex)
            }
        }
    }

    private fun flipSelectedDie() {
        _uiState.update { state ->
            val index = state.selectedFlipDieIndex
            if (!state.isAwaitingFlipFace || index == null || index !in state.diceValues.indices) {
                state
            } else {
                val currentValue = state.diceValues[index]
                val diceType = state.diceTypes.getOrElse(index) { state.diceType }
                val flippedValue = (diceType.sides + 1 - currentValue).coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[index] = flippedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    isAwaitingFlipFace = false,
                    selectedFlipDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun selectAdjustmentDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedAdjustmentDieIndex = index)
            }
        }
    }

    private fun selectSetValueDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedSetValueDieIndex = index)
            }
        }
    }

    private fun adjustSelectedDie(delta: Int) {
        _uiState.update { state ->
            val selectedIndex = state.selectedAdjustmentDieIndex
            if (!state.isAwaitingAdjustPlusMinus || selectedIndex == null) {
                state
            } else if (selectedIndex !in state.diceValues.indices) {
                state.copy(
                    isAwaitingAdjustPlusMinus = false,
                    selectedAdjustmentDieIndex = null
                )
            } else {
                val diceType = state.diceTypes.getOrElse(selectedIndex) { state.diceType }
                val currentValue = state.diceValues[selectedIndex]
                val updatedValue = (currentValue + delta).coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[selectedIndex] = updatedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    isAwaitingAdjustPlusMinus = false,
                    selectedAdjustmentDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun setSelectedDieValue(value: Int) {
        _uiState.update { state ->
            val selectedIndex = state.selectedSetValueDieIndex
            if (!state.isAwaitingSetValue || selectedIndex == null) {
                state
            } else if (selectedIndex !in state.diceValues.indices) {
                state.copy(
                    isAwaitingSetValue = false,
                    selectedSetValueDieIndex = null
                )
            } else {
                val diceType = state.diceTypes.getOrElse(selectedIndex) { state.diceType }
                val updatedValue = value.coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[selectedIndex] = updatedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    isAwaitingSetValue = false,
                    selectedSetValueDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun applyRerollAllCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.REROLL_ALL) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, card.id)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyFlipFaceCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.FLIP_FACE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = applyCardEffect(state, card.id)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyCardEffect(state: GameUiState, cardId: CardId): GameUiState {
        return when (cardId) {
            CardId.REROLL_SINGLE -> state.copy(
                selectedCardIndex = null,
                isAwaitingRerollSingle = true,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
            CardId.FLIP_FACE -> state.copy(
                selectedCardIndex = null,
                isAwaitingFlipFace = true,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
            CardId.ADJUST_PLUS_MINUS_ONE -> state.copy(
                selectedCardIndex = null,
                isAwaitingAdjustPlusMinus = true,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingSetValue = false,
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
            CardId.SET_VALUE -> state.copy(
                selectedCardIndex = null,
                isAwaitingSetValue = true,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
            CardId.REROLL_ALL -> state.copy(
                selectedCardIndex = null,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = true,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null,
                selectedRerollDice = emptySet()
            )
            CardId.REPEAT_LAST -> state.copy(selectedCardIndex = null)
            CardId.MINIGAMES -> state.copy(
                selectedCardIndex = null,
                minigamesAvailable = state.minigamesAvailable + MINIGAMES_REWARD_AMOUNT,
                showMinigamesAdPrompt = false
            )
        }
    }

    private fun rollSelectedDice() {
        val state = _uiState.value
        if (!state.isAwaitingRerollSelected || rollJob?.isActive == true) return
        val selectedIndices = state.selectedRerollDice.toList()
        if (selectedIndices.isEmpty()) return
        val diceTypes = selectedIndices.map { index ->
            state.diceTypes.getOrElse(index) { state.diceType }
        }
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            _uiState.update {
                it.copy(
                    isRolling = true,
                    isAwaitingRerollSelected = false,
                    selectedRerollDice = emptySet()
                )
            }
            repeat(steps) {
                val values = rollDiceUseCase.execute(diceTypes)
                _uiState.update { currentState ->
                    val updatedValues = currentState.diceValues.toMutableList()
                    selectedIndices.forEachIndexed { listIndex, dieIndex ->
                        if (dieIndex in updatedValues.indices) {
                            updatedValues[dieIndex] = values.getOrNull(listIndex) ?: updatedValues[dieIndex]
                        }
                    }
                    currentState.copy(
                        diceValues = updatedValues,
                        selectedDiceSum = calculateSelectedDiceSum(updatedValues, currentState.selectedDice)
                    )
                }
                delay(tickMs)
            }
            _uiState.update { it.copy(isRolling = false) }
            refreshObjectiveProgress()
        }
    }

    private fun consumeCard(cards: List<CardUiModel>, index: Int): List<CardUiModel> {
        val updatedCards = cards.toMutableList()
        val card = updatedCards.getOrNull(index) ?: return cards
        consumeCardFromInventoryUseCase.execute(card.id)
        if (card.count > 1) {
            updatedCards[index] = card.copy(count = card.count - 1)
        } else {
            updatedCards.removeAt(index)
        }
        return updatedCards
    }

    private fun applyLevelDefinition(
        levelDefinition: LevelDefinition,
        cardUiModels: List<CardUiModel> = _uiState.value.cardUiModels,
        minigamesAvailable: Int = _uiState.value.minigamesAvailable
    ) {
        currentLevelNumber = levelDefinition.levelNumber
        currentObjective = null
        val diceValues = List(levelDefinition.diceCount) { 1 }
        _uiState.update { state ->
            state.copy(
                diceValues = diceValues,
                diceCount = levelDefinition.diceCount,
                diceType = levelDefinition.diceTypes.firstOrNull() ?: DiceType.D6,
                diceTypes = levelDefinition.diceTypes,
                layoutSeed = 0L,
                isRolling = false,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedDice = emptySet(),
                selectedRerollDice = emptySet(),
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null,
                selectedDiceSum = 0,
                shouldShowSelectedSum = false,
                cardUiModels = cardUiModels,
                selectedCardIndex = null,
                lastAppliedCardId = null,
                levelNumber = levelDefinition.levelNumber,
                objectiveLines = emptyList(),
                isLevelComplete = false,
                showLevelCompleteMessage = false,
                minigamesAvailable = minigamesAvailable,
                showMinigamesAdPrompt = false
            )
        }
        initialRollSnapshot = null
    }

    private fun refreshCardInventory() {
        val updatedCards = loadInventoryCardModels()
        _uiState.update { it.copy(cardUiModels = updatedCards) }
    }

    private fun loadInventoryCardModels(): List<CardUiModel> {
        val counts = getCardInventoryUseCase.execute()
        return defaultCardUiModels().mapNotNull { card ->
            val count = counts[card.id] ?: 0
            if (count > 0) {
                card.copy(count = count)
            } else {
                null
            }
        }
    }

    private fun refreshObjectiveProgress() {
        val objective = currentObjective ?: return
        val state = _uiState.value
        val showSelectedSum = shouldShowSelectedSum(objective.conditions)
        val selectedIndices = state.selectedDice.toList().sorted()
        val selectedValues = selectedIndices.mapNotNull { index -> state.diceValues.getOrNull(index) }
        val selectedSides = selectedIndices.mapNotNull { index -> state.diceTypes.getOrNull(index)?.sides }
        if (selectedValues.isEmpty()) {
            val lines = buildObjectiveLines(objective, emptyList(), emptyList()).map { line ->
                line.copy(isMet = false)
            }
            _uiState.update {
                it.copy(
                    objectiveLines = lines,
                    isLevelComplete = false,
                    shouldShowSelectedSum = showSelectedSum
                )
            }
            return
        }
        val lines = buildObjectiveLines(objective, selectedValues, selectedSides)
        val completed = lines.all { it.isMet }
        val wasComplete = _uiState.value.isLevelComplete
        _uiState.update {
            it.copy(
                objectiveLines = lines,
                isLevelComplete = completed,
                shouldShowSelectedSum = showSelectedSum
            )
        }
        if (completed && !wasComplete && !_uiState.value.isRolling) {
            handleLevelComplete()
        }
    }

    internal fun shouldStartRollOnLaunch(): Boolean {
        return shouldAutoStartRoll
    }

    private fun restoreFromSnapshot(snapshot: MainGameSnapshot) {
        val uiSnapshot = snapshot.uiSnapshot
        baseSeed = snapshot.baseSeed
        currentObjective = snapshot.currentObjective
        initialRollSnapshot = snapshot.initialRollSnapshot
        currentLevelNumber = uiSnapshot.levelNumber
        setCardInventoryUseCase.execute(uiSnapshot.cardCounts)
        val restoredCards = buildCardUiModels(uiSnapshot.cardCounts)
        _uiState.update {
            it.copy(
                diceValues = uiSnapshot.diceValues,
                diceCount = uiSnapshot.diceCount,
                diceType = uiSnapshot.diceType,
                diceTypes = uiSnapshot.diceTypes,
                layoutSeed = uiSnapshot.layoutSeed,
                isRolling = uiSnapshot.isRolling,
                isAwaitingRerollSingle = uiSnapshot.isAwaitingRerollSingle,
                isAwaitingRerollSelected = uiSnapshot.isAwaitingRerollSelected,
                isAwaitingFlipFace = uiSnapshot.isAwaitingFlipFace,
                isAwaitingAdjustPlusMinus = uiSnapshot.isAwaitingAdjustPlusMinus,
                isAwaitingSetValue = uiSnapshot.isAwaitingSetValue,
                selectedDice = uiSnapshot.selectedDice,
                selectedRerollDice = uiSnapshot.selectedRerollDice,
                selectedRerollSingleDieIndex = uiSnapshot.selectedRerollSingleDieIndex,
                selectedFlipDieIndex = uiSnapshot.selectedFlipDieIndex,
                selectedAdjustmentDieIndex = uiSnapshot.selectedAdjustmentDieIndex,
                selectedSetValueDieIndex = uiSnapshot.selectedSetValueDieIndex,
                selectedDiceSum = uiSnapshot.selectedDiceSum,
                shouldShowSelectedSum = uiSnapshot.shouldShowSelectedSum,
                cardUiModels = restoredCards,
                selectedCardIndex = uiSnapshot.selectedCardIndex,
                lastAppliedCardId = uiSnapshot.lastAppliedCardId,
                levelNumber = uiSnapshot.levelNumber,
                objectiveLines = emptyList(),
                isLevelComplete = uiSnapshot.isLevelComplete,
                showLevelCompleteMessage = uiSnapshot.showLevelCompleteMessage,
                minigamesAvailable = uiSnapshot.minigamesAvailable,
                showMinigamesAdPrompt = false
            )
        }
        refreshObjectiveProgress()
    }

    private fun buildMainGameSnapshot(): MainGameSnapshot {
        val state = _uiState.value
        val cardCounts = getCardInventoryUseCase.execute()
        val uiSnapshot = GameUiSnapshot(
            diceValues = state.diceValues,
            diceCount = state.diceCount,
            diceType = state.diceType,
            diceTypes = state.diceTypes,
            layoutSeed = state.layoutSeed,
            isRolling = state.isRolling,
            isAwaitingRerollSingle = state.isAwaitingRerollSingle,
            isAwaitingRerollSelected = state.isAwaitingRerollSelected,
            isAwaitingFlipFace = state.isAwaitingFlipFace,
            isAwaitingAdjustPlusMinus = state.isAwaitingAdjustPlusMinus,
            isAwaitingSetValue = state.isAwaitingSetValue,
            selectedDice = state.selectedDice,
            selectedRerollDice = state.selectedRerollDice,
            selectedRerollSingleDieIndex = state.selectedRerollSingleDieIndex,
            selectedFlipDieIndex = state.selectedFlipDieIndex,
            selectedAdjustmentDieIndex = state.selectedAdjustmentDieIndex,
            selectedSetValueDieIndex = state.selectedSetValueDieIndex,
            selectedDiceSum = state.selectedDiceSum,
            shouldShowSelectedSum = state.shouldShowSelectedSum,
            cardCounts = cardCounts,
            selectedCardIndex = state.selectedCardIndex,
            lastAppliedCardId = state.lastAppliedCardId,
            levelNumber = state.levelNumber,
            isLevelComplete = state.isLevelComplete,
            showLevelCompleteMessage = state.showLevelCompleteMessage,
            minigamesAvailable = state.minigamesAvailable
        )
        return MainGameSnapshot(
            uiSnapshot = uiSnapshot,
            baseSeed = baseSeed,
            currentObjective = currentObjective,
            initialRollSnapshot = initialRollSnapshot
        )
    }

    private fun buildCardUiModels(cardCounts: Map<CardId, Int>): List<CardUiModel> {
        return defaultCardUiModels().mapNotNull { card ->
            val count = cardCounts[card.id] ?: 0
            if (count > 0) {
                card.copy(count = count)
            } else {
                null
            }
        }
    }

    private fun handleLevelComplete() {
        if (completionJob?.isActive == true) return
        completionJob = viewModelScope.launch(dispatcher) {
            delay(LEVEL_COMPLETE_DELAY_MS)
            val updatedMinigames = _uiState.value.minigamesAvailable + MINIGAMES_REWARD_AMOUNT
            val nextLevel = (_uiState.value.levelNumber + 1).coerceAtLeast(1)
            val nextDefinition = generateLevelUseCase.execute(
                levelNumber = nextLevel,
                seedBase = baseSeed
            )
            applyLevelDefinition(
                levelDefinition = nextDefinition,
                minigamesAvailable = updatedMinigames
            )
            startRolling()
        }
    }

    private fun pickMinigame(): MinigameType {
        val values = MinigameType.values()
        return values[Random.Default.nextInt(values.size)]
    }

    private fun buildObjectiveLines(
        objective: LevelObjective,
        diceValues: List<Int>,
        diceSides: List<Int>
    ): List<ObjectiveLineUiState> {
        val selectedCount = diceValues.size
        return objective.conditions.map { condition ->
            val text = objectiveLineText(condition, selectedCount)
            ObjectiveLineUiState(
                text = text,
                isMet = condition.isMet(diceValues, diceSides)
            )
        }
    }
}

internal fun calculateSelectedDiceSum(
    diceValues: List<Int>,
    selectedDice: Set<Int>
): Int {
    return selectedDice.sumOf { index -> diceValues.getOrNull(index) ?: 0 }
}

internal fun shouldShowSelectedSum(conditions: List<ObjectiveCondition>): Boolean {
    return conditions.any { condition ->
        condition is SumAtLeastCondition ||
            condition is SumAtMostCondition ||
            condition is SumExactCondition ||
            condition is SumInRangeCondition ||
            condition is SumMultipleCondition ||
            condition is SumMaxDifferenceCondition
    }
}

internal fun objectiveLineText(
    condition: ObjectiveCondition,
    selectedCount: Int
): ObjectiveLineText {
    return when (condition) {
        is SumAtLeastCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_least,
            formatArgs = listOf(condition.threshold)
        )
        is SumAtMostCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_most,
            formatArgs = listOf(condition.threshold)
        )
        is SumExactCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_exact,
            formatArgs = listOf(condition.target)
        )
        is SumMultipleCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_multiple,
            formatArgs = listOf(condition.factor)
        )
        is SumMaxDifferenceCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_sum_max_difference,
                formatArgs = listOf(condition.target, condition.maxDifference)
            )
        }
        is SumInRangeCondition -> if (condition.min == condition.max) {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_sum_exact,
                formatArgs = listOf(condition.min)
            )
        } else {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_sum_in_range,
                formatArgs = listOf(condition.min, condition.max)
            )
        }
        is SumParityCondition -> if (condition.shouldBeEven) {
            ObjectiveLineText.StringRes(resId = R.string.objective_sum_even)
        } else {
            ObjectiveLineText.StringRes(resId = R.string.objective_sum_odd)
        }
        is HasPairCondition -> if (condition.requiredPairs >= 2) {
            ObjectiveLineText.StringRes(resId = R.string.objective_two_pairs)
        } else {
            ObjectiveLineText.StringRes(resId = R.string.objective_pair)
        }
        is ExactTwoPairsCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_two_pairs_exact)
        is HasThreeOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_three_of_kind)
        is ThreeOfKindWithValueCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_three_of_kind_with_value,
                formatArgs = listOf(condition.requiredValue)
            )
        }
        is HasFourOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_four_of_kind)
        is FullHouseCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_full_house)
        is AllDistinctCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_all_distinct)
        is ExactlyDistinctValuesCondition -> ObjectiveLineText.PluralRes(
            resId = R.plurals.objective_exact_distinct_values,
            quantity = condition.distinctCount,
            formatArgs = listOf(condition.distinctCount)
        )
        is ContainsHighAndLowValueCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_contains_high_and_low)
        is StraightCondition -> ObjectiveLineText.PluralRes(
            resId = R.plurals.objective_straight,
            quantity = condition.length,
            formatArgs = listOf(condition.length)
        )
        is ContainsValuesCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_contains_values,
                formatArgs = listOf(formatValues(condition.values))
            )
        }
        is ContainsValuesWithMultiplicityCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_contains_values,
                formatArgs = listOf(formatMultiplicity(condition.values))
            )
        }
        is ForbidValuesCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_forbid_values,
                formatArgs = listOf(formatValues(condition.values))
            )
        }
        is MinSelectedDiceCondition -> {
            val clampedCount = selectedCount.coerceAtMost(condition.minCount)
            ObjectiveLineText.PluralRes(
                resId = R.plurals.objective_selected_progress,
                quantity = clampedCount,
                formatArgs = listOf(clampedCount, condition.minCount)
            )
        }
        is ExactSelectedDiceCondition -> {
            ObjectiveLineText.PluralRes(
                resId = R.plurals.objective_selected_exact,
                quantity = selectedCount,
                formatArgs = listOf(selectedCount, condition.count)
            )
        }
        is AtLeastParityCountCondition -> {
            if (condition.even) {
                ObjectiveLineText.PluralRes(
                    resId = R.plurals.objective_at_least_even_values,
                    quantity = condition.minCount,
                    formatArgs = listOf(condition.minCount)
                )
            } else {
                ObjectiveLineText.PluralRes(
                    resId = R.plurals.objective_at_least_odd_values,
                    quantity = condition.minCount,
                    formatArgs = listOf(condition.minCount)
                )
            }
        }
        is SatisfyAndAvoidCondition -> {
            ObjectiveLineText.StringRes(resId = R.string.objective_satisfy_and_avoid)
        }
    }
}

internal fun formatValues(values: List<Int>): String {
    return values.distinct().sorted().joinToString(", ")
}

internal fun formatMultiplicity(values: List<Int>): String {
    val counts = values.groupingBy { it }.eachCount().toSortedMap()
    return counts.entries.joinToString(", ") { (value, count) -> "${count}x$value" }
}
