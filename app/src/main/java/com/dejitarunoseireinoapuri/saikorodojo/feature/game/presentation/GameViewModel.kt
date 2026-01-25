package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
private const val DEFAULT_ROLL_DURATION_MS = 3_000L
private const val DEFAULT_TICK_MS = 150L

data class GameUiState(
    val diceValues: List<Int> = List(DEFAULT_DICE_COUNT) { 1 },
    val diceCount: Int = DEFAULT_DICE_COUNT,
    val layoutSeed: Long = 0L,
    val isRolling: Boolean = false
)

sealed interface GameUiEvent {
    data object StartRoll : GameUiEvent
}

class GameViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollDurationMs: Long = DEFAULT_ROLL_DURATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val diceCount: Int = DEFAULT_DICE_COUNT,
    private val layoutSeedProvider: () -> Long = { Random.Default.nextLong() }
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        GameUiState(
            diceValues = List(diceCount) { 1 },
            diceCount = diceCount
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState

    private var rollJob: Job? = null

    fun onEvent(event: GameUiEvent) {
        when (event) {
            GameUiEvent.StartRoll -> startRolling()
        }
    }

    private fun startRolling() {
        if (rollJob?.isActive == true) return

        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollDurationMs / tickMs).coerceAtLeast(1L).toInt()
            _uiState.update { it.copy(isRolling = true, layoutSeed = layoutSeedProvider()) }

            repeat(steps) {
                val values = rollDiceUseCase.execute(_uiState.value.diceCount)
                _uiState.update { it.copy(diceValues = values) }
                delay(tickMs)
            }

            _uiState.update { it.copy(isRolling = false) }
        }
    }
}
