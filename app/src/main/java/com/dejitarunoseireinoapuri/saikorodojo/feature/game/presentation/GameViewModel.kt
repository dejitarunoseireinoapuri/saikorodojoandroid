package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ConsumeCardFromInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.GetCardInventoryUseCase
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumInRangeCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumParityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasPairCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasThreeOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasFourOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.FullHouseCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.StraightCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesWithMultiplicityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.CollectionPartialCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ForbidValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
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
private const val DEFAULT_COMPLETION_MESSAGE_MS = 1_000L
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
    val selectedRerollSingleDieIndex: Int? = null,
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
    val showLevelCompleteMessage: Boolean = false
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
    data object DismissSelectedCard : GameUiEvent
    data object IncreaseDiceCount : GameUiEvent
    data object ConfirmSurrender : GameUiEvent
    data object ConfirmExit : GameUiEvent
    data object OpenRandomMinigame : GameUiEvent
}

sealed interface GameUiEffect {
    data class NavigateToMinigame(val minigame: MinigameType) : GameUiEffect
    data class NavigateToMenu(val resetProgress: Boolean) : GameUiEffect
}

data class ObjectiveLineUiState(
    val textRes: Int,
    val formatArgs: List<Any>,
    val isMet: Boolean
)

class GameViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val generateLevelUseCase: GenerateLevelUseCase = GenerateLevelUseCase(),
    private val generateObjectiveUseCase: GenerateObjectiveUseCase = GenerateObjectiveUseCase(),
    private val getCardInventoryUseCase: GetCardInventoryUseCase =
        GetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val consumeCardFromInventoryUseCase: ConsumeCardFromInventoryUseCase =
        ConsumeCardFromInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollDurationMs: Long = DEFAULT_ROLL_DURATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val completionMessageMs: Long = DEFAULT_COMPLETION_MESSAGE_MS,
    private val layoutSeedProvider: () -> Long = { Random.Default.nextLong() },
    private val baseSeedProvider: () -> Long = { Random.Default.nextLong() },
    private val initialLevelDefinition: LevelDefinition? = null,
    cardUiModels: List<CardUiModel> = emptyList()
) : ViewModel() {
    private data class RollSnapshot(
        val diceValues: List<Int>,
        val diceTypes: List<DiceType>,
        val layoutSeed: Long
    )

    private val baseSeed = baseSeedProvider()
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private val _effects = MutableSharedFlow<GameUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<GameUiEffect> = _effects

    private var rollJob: Job? = null
    private var initialRollSnapshot: RollSnapshot? = null
    private var completionJob: Job? = null
    private var currentObjective: LevelObjective? = null
    private var currentLevelNumber: Int = 1

    init {
        val initialCards = cardUiModels.ifEmpty { loadInventoryCardModels() }
        applyLevelDefinition(
            levelDefinition = initialLevelDefinition
                ?: generateLevelUseCase.execute(levelNumber = 1, seedBase = baseSeed),
            cardUiModels = initialCards
        )
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
            GameUiEvent.DismissSelectedCard -> {
                if (!isCardInteractionBlocked()) {
                    dismissSelectedCard()
                }
            }
            GameUiEvent.IncreaseDiceCount -> increaseDiceCount()
            GameUiEvent.ConfirmSurrender -> confirmSurrender()
            GameUiEvent.ConfirmExit -> confirmExit()
            GameUiEvent.OpenRandomMinigame -> openRandomMinigame()
        }
    }

    private fun isCardInteractionBlocked(): Boolean {
        return _uiState.value.isAwaitingRerollSingle ||
            _uiState.value.isAwaitingRerollSelected ||
            _uiState.value.isAwaitingFlipFace ||
            _uiState.value.isAwaitingAdjustPlusMinus ||
            _uiState.value.isAwaitingSetValue
    }


    private fun openRandomMinigame() {
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMinigame(pickMinigame()))
        }
    }

    private fun confirmSurrender() {
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMenu(resetProgress = true))
        }
    }

    private fun confirmExit() {
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
                initialRollSnapshot = RollSnapshot(
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
        } else if (state.isAwaitingFlipFace) {
            flipSelectedDie(index)
        } else if (state.isAwaitingAdjustPlusMinus) {
            selectAdjustmentDie(index)
        } else if (state.isAwaitingSetValue) {
            selectSetValueDie(index)
        } else {
            toggleDiceSelection(index)
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
        if (applyRetryCard(index)) {
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
        if (repeatedCardId == CardId.RETRY) {
            refreshObjectiveProgress()
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

    private fun applyRetryCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.RETRY) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = buildRetryState(state)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        if (applied) {
            refreshObjectiveProgress()
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

    private fun flipSelectedDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
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
                    isAwaitingFlipFace = false
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
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null,
                selectedDice = emptySet(),
                selectedDiceSum = 0
            )
            CardId.REPEAT_LAST,
            CardId.RETRY -> buildRetryState(state)
        }
    }

    private fun buildRetryState(state: GameUiState): GameUiState {
        val snapshot = initialRollSnapshot ?: RollSnapshot(
            diceValues = state.diceValues.toList(),
            diceTypes = state.diceTypes.toList(),
            layoutSeed = state.layoutSeed
        )
        return state.copy(
            diceValues = snapshot.diceValues,
            diceTypes = snapshot.diceTypes,
            layoutSeed = snapshot.layoutSeed,
            isRolling = false,
            selectedCardIndex = null,
            isAwaitingRerollSingle = false,
            isAwaitingRerollSelected = false,
            isAwaitingFlipFace = false,
            isAwaitingAdjustPlusMinus = false,
            isAwaitingSetValue = false,
            selectedRerollSingleDieIndex = null,
            selectedAdjustmentDieIndex = null,
            selectedSetValueDieIndex = null,
            selectedDice = emptySet(),
            selectedDiceSum = 0
        )
    }

    private fun rollSelectedDice() {
        val state = _uiState.value
        if (!state.isAwaitingRerollSelected || rollJob?.isActive == true) return
        val selectedIndices = state.selectedDice.toList()
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
                    selectedDice = emptySet(),
                    selectedDiceSum = 0
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
                    currentState.copy(diceValues = updatedValues)
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
        cardUiModels: List<CardUiModel> = _uiState.value.cardUiModels
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
                selectedRerollSingleDieIndex = null,
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
                showLevelCompleteMessage = false
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
        val selectedValues = state.selectedDice.mapNotNull { index ->
            state.diceValues.getOrNull(index)
        }
        if (selectedValues.isEmpty()) {
            val lines = buildObjectiveLines(objective, emptyList()).map { line ->
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
        val lines = buildObjectiveLines(objective, selectedValues)
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

    private fun handleLevelComplete() {
        if (completionJob?.isActive == true) return
        completionJob = viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(showLevelCompleteMessage = true) }
            if (completionMessageMs > 0L) {
                delay(completionMessageMs)
            }
            val nextLevel = (_uiState.value.levelNumber + 1).coerceAtLeast(1)
            val nextDefinition = generateLevelUseCase.execute(
                levelNumber = nextLevel,
                seedBase = baseSeed
            )
            applyLevelDefinition(nextDefinition)
            startRolling()
        }
    }

    private fun pickMinigame(): MinigameType {
        val values = MinigameType.values()
        return values[Random.Default.nextInt(values.size)]
    }

    private fun buildObjectiveLines(
        objective: LevelObjective,
        diceValues: List<Int>
    ): List<ObjectiveLineUiState> {
        val selectedCount = diceValues.size
        return objective.conditions.map { condition ->
            val (textRes, args) = objectiveLineText(condition, selectedCount)
            ObjectiveLineUiState(
                textRes = textRes,
                formatArgs = args,
                isMet = condition.isMet(diceValues)
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
            condition is SumExactCondition ||
            condition is SumInRangeCondition
    }
}

internal fun objectiveLineText(
    condition: ObjectiveCondition,
    selectedCount: Int
): Pair<Int, List<Any>> {
    return when (condition) {
        is SumAtLeastCondition -> R.string.objective_sum_at_least to listOf(condition.threshold)
        is SumExactCondition -> R.string.objective_sum_exact to listOf(condition.target)
        is SumInRangeCondition -> if (condition.min == condition.max) {
            R.string.objective_sum_exact to listOf(condition.min)
        } else {
            R.string.objective_sum_in_range to listOf(condition.min, condition.max)
        }
        is SumParityCondition -> if (condition.shouldBeEven) {
            R.string.objective_sum_even to emptyList()
        } else {
            R.string.objective_sum_odd to emptyList()
        }
        is HasPairCondition -> if (condition.requiredPairs >= 2) {
            R.string.objective_two_pairs to emptyList()
        } else {
            R.string.objective_pair to emptyList()
        }
        is HasThreeOfKindCondition -> R.string.objective_three_of_kind to emptyList()
        is HasFourOfKindCondition -> R.string.objective_four_of_kind to emptyList()
        is FullHouseCondition -> R.string.objective_full_house to emptyList()
        is AllDistinctCondition -> R.string.objective_all_distinct to emptyList()
        is StraightCondition -> R.string.objective_straight to listOf(condition.length)
        is ContainsValuesCondition -> {
            R.string.objective_contains_values to listOf(formatValues(condition.values))
        }
        is ContainsValuesWithMultiplicityCondition -> {
            R.string.objective_contains_values to listOf(formatMultiplicity(condition.values))
        }
        is CollectionPartialCondition -> {
            R.string.objective_collection_partial to listOf(
                formatValues(condition.values),
                condition.requiredCount
            )
        }
        is ForbidValuesCondition -> {
            R.string.objective_forbid_values to listOf(formatValues(condition.values))
        }
        is MinSelectedDiceCondition -> {
            R.string.objective_selected_progress to listOf(selectedCount, condition.minCount)
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
