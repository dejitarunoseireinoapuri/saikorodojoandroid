package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerRoll
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.RollHigherLowerUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_TOTAL_ROUNDS = 3
private const val DEFAULT_TARGET_CORRECT = 3
private const val DEFAULT_ROLL_ANIMATION_MS = 2_000L
private const val DEFAULT_TICK_MS = 120L
private const val DEFAULT_RESULT_DELAY_MS = 1_500L
private const val DEFAULT_TRANSITION_MS = 900L

@JvmInline
value class DiceSum(val value: Int)

data class HigherLowerGameUiState(
    val isStarted: Boolean = false,
    val currentRound: Int = 0,
    val totalRounds: Int = DEFAULT_TOTAL_ROUNDS,
    val correctStreak: Int = 0,
    val targetCorrect: Int = DEFAULT_TARGET_CORRECT,
    val selectedChoice: HigherLowerChoice? = null,
    val baseDiceValues: List<Int> = emptyList(),
    val currentDiceValues: List<Int> = emptyList(),
    val isRolling: Boolean = false,
    val isChoiceVisible: Boolean = false,
    val isTransitioning: Boolean = false,
    val isComplete: Boolean = false,
    val hasLoss: Boolean = false,
    val rewardCards: List<CardUiModel> = emptyList()
)

sealed interface HigherLowerGameUiEvent {
    data object StartGame : HigherLowerGameUiEvent
    data class SelectChoice(val choice: HigherLowerChoice) : HigherLowerGameUiEvent
}

class HigherLowerGameViewModel(
    private val rollHigherLowerUseCase: RollHigherLowerUseCase = RollHigherLowerUseCase(),
    private val selectMinigameRewardCardsUseCase: SelectMinigameRewardCardsUseCase =
        SelectMinigameRewardCardsUseCase(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val resultDelayMs: Long = DEFAULT_RESULT_DELAY_MS,
    private val transitionMs: Long = DEFAULT_TRANSITION_MS,
    private val totalRounds: Int = DEFAULT_TOTAL_ROUNDS,
    private val targetCorrect: Int = DEFAULT_TARGET_CORRECT,
    private val cardUiModels: List<CardUiModel> = defaultCardUiModels()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HigherLowerGameUiState(
            totalRounds = totalRounds,
            targetCorrect = targetCorrect
        )
    )
    val uiState: StateFlow<HigherLowerGameUiState> = _uiState

    private var rollJob: Job? = null

    fun onEvent(event: HigherLowerGameUiEvent) {
        when (event) {
            HigherLowerGameUiEvent.StartGame -> startGame()
            is HigherLowerGameUiEvent.SelectChoice -> handleChoice(event.choice)
        }
    }

    private fun startGame() {
        rollJob?.cancel()
        _uiState.update {
            it.copy(
                isStarted = true,
                currentRound = 1,
                correctStreak = 0,
                selectedChoice = null,
                baseDiceValues = emptyList(),
                currentDiceValues = emptyList(),
                isRolling = true,
                isChoiceVisible = false,
                isTransitioning = false,
                isComplete = false,
                hasLoss = false,
                rewardCards = emptyList()
            )
        }
        startRoll(
            onTick = { roll -> updateBaseDice(roll) },
            onComplete = { roll ->
                _uiState.update { state ->
                    state.copy(
                        baseDiceValues = roll.values,
                        isRolling = false,
                        isChoiceVisible = true
                    )
                }
            }
        )
    }

    private fun handleChoice(choice: HigherLowerChoice) {
        val state = _uiState.value
        if (!state.isStarted || state.isComplete || state.isRolling || state.selectedChoice != null) {
            return
        }
        if (state.baseDiceValues.isEmpty()) return
        rollJob?.cancel()
        _uiState.update {
            it.copy(
                selectedChoice = choice,
                isRolling = true,
                isChoiceVisible = false
            )
        }
        startRoll(
            onTick = { roll -> updateCurrentDice(roll) },
            onComplete = { roll ->
                val baseSum = DiceSum(state.baseDiceValues.sum())
                val newSum = DiceSum(roll.sum)
                if (newSum.value == baseSum.value) {
                    resolveWin()
                } else if (isCorrectGuess(choice, baseSum, newSum)) {
                    resolveCorrectGuess(roll.values)
                } else {
                    resolveLoss(roll.values)
                }
            }
        )
    }

    private fun updateBaseDice(roll: HigherLowerRoll) {
        _uiState.update { state ->
            state.copy(baseDiceValues = roll.values)
        }
    }

    private fun updateCurrentDice(roll: HigherLowerRoll) {
        _uiState.update { state ->
            state.copy(currentDiceValues = roll.values)
        }
    }

    private fun isCorrectGuess(
        choice: HigherLowerChoice,
        baseSum: DiceSum,
        newSum: DiceSum
    ): Boolean {
        return when (choice) {
            HigherLowerChoice.HIGHER -> newSum.value > baseSum.value
            HigherLowerChoice.LOWER -> newSum.value < baseSum.value
        }
    }

    private fun resolveCorrectGuess(newValues: List<Int>) {
        val state = _uiState.value
        val updatedStreak = state.correctStreak + 1
        if (updatedStreak >= targetCorrect) {
            resolveWin()
            return
        }
        rollJob = viewModelScope.launch(dispatcher) {
            if (resultDelayMs > 0L) {
                delay(resultDelayMs)
            }
            _uiState.update {
                it.copy(
                    correctStreak = updatedStreak,
                    currentRound = it.currentRound + 1,
                    selectedChoice = null,
                    isRolling = false,
                    isTransitioning = true,
                    isChoiceVisible = false
                )
            }
            if (transitionMs > 0L) {
                delay(transitionMs)
            }
            _uiState.update {
                it.copy(
                    baseDiceValues = newValues,
                    currentDiceValues = emptyList(),
                    isTransitioning = false,
                    isChoiceVisible = true
                )
            }
        }
    }

    private fun resolveLoss(newValues: List<Int>) {
        rollJob = viewModelScope.launch(dispatcher) {
            if (resultDelayMs > 0L) {
                delay(resultDelayMs)
            }
            _uiState.update {
                it.copy(
                    currentDiceValues = newValues,
                    isRolling = false,
                    isChoiceVisible = false,
                    isTransitioning = false,
                    isComplete = true,
                    hasLoss = true
                )
            }
        }
    }

    private fun resolveWin() {
        rollJob = viewModelScope.launch(dispatcher) {
            if (resultDelayMs > 0L) {
                delay(resultDelayMs)
            }
            val rewardCards = resolveRewardCards()
            _uiState.update {
                it.copy(
                    isRolling = false,
                    isChoiceVisible = false,
                    isTransitioning = false,
                    isComplete = true,
                    rewardCards = rewardCards
                )
            }
        }
    }

    private fun resolveRewardCards(): List<CardUiModel> {
        return selectMinigameRewardCardsUseCase.execute().mapNotNull { rewardId ->
            cardUiModels.firstOrNull { it.id == rewardId }
        }
    }

    private fun startRoll(
        onTick: (HigherLowerRoll) -> Unit,
        onComplete: (HigherLowerRoll) -> Unit
    ) {
        rollJob?.cancel()
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            var finalRoll = rollHigherLowerUseCase.execute()
            repeat(steps) {
                val roll = rollHigherLowerUseCase.execute()
                finalRoll = roll
                onTick(roll)
                if (rollAnimationMs > 0L) {
                    delay(tickMs)
                }
            }
            onComplete(finalRoll)
        }
    }
}
