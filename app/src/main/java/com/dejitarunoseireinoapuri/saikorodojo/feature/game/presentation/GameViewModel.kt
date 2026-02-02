package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val DEFAULT_DICE_COUNT = 5
private const val MAX_DICE_COUNT = 12
private const val DEFAULT_ROLL_DURATION_MS = 1_000L
private const val DEFAULT_TICK_MS = 150L
data class GameUiState(
    val diceValues: List<Int> = List(DEFAULT_DICE_COUNT) { 1 },
    val diceCount: Int = DEFAULT_DICE_COUNT,
    val maxDiceCount: Int = MAX_DICE_COUNT,
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
    val cardUiModels: List<CardUiModel> = emptyList(),
    val selectedCardIndex: Int? = null,
    val lastAppliedCardId: CardId? = null
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
}

class GameViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollDurationMs: Long = DEFAULT_ROLL_DURATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val diceCount: Int = DEFAULT_DICE_COUNT,
    private val diceType: DiceType = DiceType.D6,
    private val layoutSeedProvider: () -> Long = { Random.Default.nextLong() },
    private val diceTypeProvider: (Long, Int) -> List<DiceType> = ::defaultDiceTypes,
    cardUiModels: List<CardUiModel> = defaultCardUiModels()
) : ViewModel() {
    private data class RollSnapshot(
        val diceValues: List<Int>,
        val diceTypes: List<DiceType>,
        val layoutSeed: Long
    )

    private val _uiState = MutableStateFlow(
        GameUiState(
            diceValues = List(diceCount) { 1 },
            diceCount = diceCount,
            maxDiceCount = MAX_DICE_COUNT,
            diceType = diceType,
            diceTypes = diceTypeProvider(0L, diceCount),
            cardUiModels = cardUiModels
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    private var rollJob: Job? = null
    private var initialRollSnapshot: RollSnapshot? = null

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
        }
    }

    private fun isCardInteractionBlocked(): Boolean {
        return _uiState.value.isAwaitingRerollSingle ||
            _uiState.value.isAwaitingRerollSelected ||
            _uiState.value.isAwaitingFlipFace ||
            _uiState.value.isAwaitingAdjustPlusMinus ||
            _uiState.value.isAwaitingSetValue
    }

    private fun startRolling(keepLayout: Boolean = false) {
        if (rollJob?.isActive == true) return

        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            val currentState = _uiState.value
            val seed = if (keepLayout) currentState.layoutSeed else layoutSeedProvider()
            val diceTypes = if (keepLayout) {
                currentState.diceTypes
            } else {
                diceTypeProvider(seed, currentState.diceCount)
            }
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
        if (currentState.isRolling || currentState.diceCount >= currentState.maxDiceCount) return
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
        }
    }

    private fun consumeCard(cards: List<CardUiModel>, index: Int): List<CardUiModel> {
        val updatedCards = cards.toMutableList()
        val card = updatedCards.getOrNull(index) ?: return cards
        if (card.count > 1) {
            updatedCards[index] = card.copy(count = card.count - 1)
        } else {
            updatedCards.removeAt(index)
        }
        return updatedCards
    }
}

internal fun calculateSelectedDiceSum(
    diceValues: List<Int>,
    selectedDice: Set<Int>
): Int {
    return selectedDice.sumOf { index -> diceValues.getOrNull(index) ?: 0 }
}

private fun defaultDiceTypes(seed: Long, diceCount: Int): List<DiceType> {
    val random = Random(seed)
    val types = DiceType.entries
    return List(diceCount) { types[random.nextInt(types.size)] }
}
