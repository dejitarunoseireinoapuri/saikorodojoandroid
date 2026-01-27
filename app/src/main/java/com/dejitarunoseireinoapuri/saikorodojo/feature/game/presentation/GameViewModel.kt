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
private const val DEFAULT_ROLL_DURATION_MS = 1_000L
private const val DEFAULT_TICK_MS = 150L
data class GameUiState(
    val diceValues: List<Int> = List(DEFAULT_DICE_COUNT) { 1 },
    val diceCount: Int = DEFAULT_DICE_COUNT,
    val diceType: DiceType = DiceType.D6,
    val diceTypes: List<DiceType> = List(DEFAULT_DICE_COUNT) { DiceType.D6 },
    val layoutSeed: Long = 0L,
    val isRolling: Boolean = false,
    val isAwaitingRerollSingle: Boolean = false,
    val isAwaitingFlipFace: Boolean = false,
    val isAwaitingAdjustPlusMinus: Boolean = false,
    val isAwaitingSetValue: Boolean = false,
    val selectedDice: Set<Int> = emptySet(),
    val selectedAdjustmentDieIndex: Int? = null,
    val selectedSetValueDieIndex: Int? = null,
    val selectedDiceSum: Int = 0,
    val cardUiModels: List<CardUiModel> = emptyList(),
    val selectedCardIndex: Int? = null
)

sealed interface GameUiEvent {
    data object StartRoll : GameUiEvent
    data class DiceClicked(val index: Int) : GameUiEvent
    data class SelectCard(val index: Int) : GameUiEvent
    data class ApplyCard(val index: Int) : GameUiEvent
    data class AdjustSelectedDie(val delta: Int) : GameUiEvent
    data class SetSelectedDieValue(val value: Int) : GameUiEvent
    data object DismissSelectedCard : GameUiEvent
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
    private val _uiState = MutableStateFlow(
        GameUiState(
            diceValues = List(diceCount) { 1 },
            diceCount = diceCount,
            diceType = diceType,
            diceTypes = diceTypeProvider(0L, diceCount),
            cardUiModels = cardUiModels
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    private var rollJob: Job? = null

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
            GameUiEvent.DismissSelectedCard -> {
                if (!isCardInteractionBlocked()) {
                    dismissSelectedCard()
                }
            }
        }
    }

    private fun isCardInteractionBlocked(): Boolean {
        return _uiState.value.isAwaitingRerollSingle ||
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
                    isAwaitingFlipFace = false,
                    isAwaitingAdjustPlusMinus = false,
                    isAwaitingSetValue = false,
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
        }
    }

    private fun handleDiceClick(index: Int) {
        val state = _uiState.value
        if (state.isAwaitingRerollSingle) {
            startSingleDieRoll(index)
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

    private fun applyCard(index: Int) {
        val applied = applyRerollAllCard(index)
        if (applied) {
            startRolling(keepLayout = true)
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
        applySetValueCard(index)
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
                    val updatedCards = cards.toMutableList().apply {
                        if (card.count > 1) {
                            this[index] = card.copy(count = card.count - 1)
                        } else {
                            removeAt(index)
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null,
                        isAwaitingRerollSingle = true,
                        isAwaitingFlipFace = false,
                        isAwaitingAdjustPlusMinus = false,
                        isAwaitingSetValue = false,
                        selectedAdjustmentDieIndex = null,
                        selectedSetValueDieIndex = null
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
                    val updatedCards = cards.toMutableList().apply {
                        if (card.count > 1) {
                            this[index] = card.copy(count = card.count - 1)
                        } else {
                            removeAt(index)
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null,
                        isAwaitingAdjustPlusMinus = true,
                        isAwaitingRerollSingle = false,
                        isAwaitingFlipFace = false,
                        isAwaitingSetValue = false,
                        selectedAdjustmentDieIndex = null,
                        selectedSetValueDieIndex = null
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
                    val updatedCards = cards.toMutableList().apply {
                        if (card.count > 1) {
                            this[index] = card.copy(count = card.count - 1)
                        } else {
                            removeAt(index)
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null,
                        isAwaitingSetValue = true,
                        isAwaitingRerollSingle = false,
                        isAwaitingFlipFace = false,
                        isAwaitingAdjustPlusMinus = false,
                        selectedAdjustmentDieIndex = null,
                        selectedSetValueDieIndex = null
                    )
                }
            }
        }
        return applied
    }

    private fun startSingleDieRoll(index: Int) {
        if (rollJob?.isActive == true) return
        if (index !in _uiState.value.diceValues.indices) return
        val diceType = _uiState.value.diceTypes.getOrElse(index) { diceType }
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            _uiState.update {
                it.copy(
                    isRolling = true,
                    isAwaitingRerollSingle = false,
                    isAwaitingFlipFace = false
                )
            }
            repeat(steps) {
                val value = rollDiceUseCase.execute(listOf(diceType)).first()
                _uiState.update { state ->
                    if (index !in state.diceValues.indices) {
                        state
                    } else {
                        val updatedValues = state.diceValues.toMutableList().apply {
                            this[index] = value
                        }
                        state.copy(
                            diceValues = updatedValues,
                            selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice)
                        )
                    }
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
                    val updatedCards = cards.toMutableList().apply {
                        if (card.count > 1) {
                            this[index] = card.copy(count = card.count - 1)
                        } else {
                            removeAt(index)
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null,
                        isAwaitingRerollSingle = false,
                        isAwaitingFlipFace = false,
                        isAwaitingAdjustPlusMinus = false,
                        isAwaitingSetValue = false,
                        selectedAdjustmentDieIndex = null,
                        selectedSetValueDieIndex = null,
                        selectedDice = emptySet(),
                        selectedDiceSum = 0
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
                    val updatedCards = cards.toMutableList().apply {
                        if (card.count > 1) {
                            this[index] = card.copy(count = card.count - 1)
                        } else {
                            removeAt(index)
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null,
                        isAwaitingFlipFace = true,
                        isAwaitingRerollSingle = false,
                        isAwaitingAdjustPlusMinus = false,
                        isAwaitingSetValue = false,
                        selectedAdjustmentDieIndex = null,
                        selectedSetValueDieIndex = null
                    )
                }
            }
        }
        return applied
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
