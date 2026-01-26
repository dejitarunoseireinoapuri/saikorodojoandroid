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
    val selectedDice: Set<Int> = emptySet(),
    val selectedDiceSum: Int = 0,
    val cardUiModels: List<CardUiModel> = emptyList(),
    val selectedCardIndex: Int? = null
)

sealed interface GameUiEvent {
    data object StartRoll : GameUiEvent
    data class ToggleDiceSelection(val index: Int) : GameUiEvent
    data class SelectCard(val index: Int) : GameUiEvent
    data class ApplyCard(val index: Int) : GameUiEvent
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
            is GameUiEvent.ToggleDiceSelection -> toggleDiceSelection(event.index)
            is GameUiEvent.SelectCard -> selectCard(event.index)
            is GameUiEvent.ApplyCard -> applyCard(event.index)
            GameUiEvent.DismissSelectedCard -> dismissSelectedCard()
        }
    }

    private fun startRolling() {
        if (rollJob?.isActive == true) return

        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            val seed = layoutSeedProvider()
            val diceTypes = diceTypeProvider(seed, _uiState.value.diceCount)
            _uiState.update {
                it.copy(
                    isRolling = true,
                    layoutSeed = seed,
                    diceTypes = diceTypes
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
            startRolling()
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
                        removeAt(index)
                        if (card.count > 1) {
                            add(card.copy(count = card.count - 1))
                        }
                    }
                    state.copy(
                        cardUiModels = updatedCards,
                        selectedCardIndex = null
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
