package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SelectSequenceRewardCardUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_TOTAL_ROLLS = 5
private const val DEFAULT_TARGET_SEQUENCE = 3
private const val DEFAULT_MAX_DISCARDS = 3
private const val DEFAULT_ROLL_ANIMATION_MS = 2_000L
private const val DEFAULT_TICK_MS = 120L

data class SequenceGameUiState(
    val isStarted: Boolean = false,
    val isRolling: Boolean = false,
    val isAwaitingDecision: Boolean = false,
    val currentRoll: Int = 0,
    val totalRolls: Int = DEFAULT_TOTAL_ROLLS,
    val targetSequence: Int = DEFAULT_TARGET_SEQUENCE,
    val maxDiscards: Int = DEFAULT_MAX_DISCARDS,
    val discardCount: Int = 0,
    val savedValues: List<Int> = emptyList(),
    val diceValue: Int? = null,
    val isComplete: Boolean = false,
    val rewardCard: CardUiModel? = null,
    val failureReason: SequenceFailureReason? = null,
    val failureDieValue: Int? = null
)

enum class SequenceFailureReason {
    ORDER,
    ROUNDS,
    DISCARDS
}

sealed interface SequenceGameUiEvent {
    data object StartGame : SequenceGameUiEvent
    data object SaveRoll : SequenceGameUiEvent
    data object DiscardRoll : SequenceGameUiEvent
}

class SequenceGameViewModel(
    private val rollSequenceUseCase: RollSequenceUseCase = RollSequenceUseCase(),
    private val selectSequenceRewardCardUseCase: SelectSequenceRewardCardUseCase =
        SelectSequenceRewardCardUseCase(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val totalRolls: Int = DEFAULT_TOTAL_ROLLS,
    private val targetSequence: Int = DEFAULT_TARGET_SEQUENCE,
    private val maxDiscards: Int = DEFAULT_MAX_DISCARDS,
    private val cardUiModels: List<CardUiModel> = defaultCardUiModels()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        SequenceGameUiState(
            totalRolls = totalRolls,
            targetSequence = targetSequence,
            maxDiscards = maxDiscards
        )
    )
    val uiState: StateFlow<SequenceGameUiState> = _uiState

    private var rollJob: Job? = null

    fun onEvent(event: SequenceGameUiEvent) {
        when (event) {
            SequenceGameUiEvent.StartGame -> startGame()
            SequenceGameUiEvent.SaveRoll -> handleSave()
            SequenceGameUiEvent.DiscardRoll -> handleDiscard()
        }
    }

    private fun startGame() {
        rollJob?.cancel()
        _uiState.update {
            it.copy(
                isStarted = true,
                isRolling = false,
                isAwaitingDecision = false,
                currentRoll = 0,
                discardCount = 0,
                savedValues = emptyList(),
                diceValue = null,
                isComplete = false,
                rewardCard = null,
                failureReason = null,
                failureDieValue = null
            )
        }
        startRoll(nextRoll = 1, savedValues = emptyList(), discardCount = 0)
    }

    private fun handleSave() {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || !state.isAwaitingDecision) {
            return
        }
        val value = state.diceValue ?: return
        val lastSaved = state.savedValues.lastOrNull()
        if (lastSaved != null && value <= lastSaved) {
            completeFailure(
                savedValues = state.savedValues,
                discardCount = state.discardCount,
                reason = SequenceFailureReason.ORDER,
                failureDieValue = value
            )
            return
        }
        val updatedSaved = state.savedValues + value
        if (updatedSaved.size >= state.targetSequence) {
            completeSuccess(updatedSaved, state.discardCount)
            return
        }
        advanceOrComplete(
            nextRoll = state.currentRoll + 1,
            savedValues = updatedSaved,
            discardCount = state.discardCount
        )
    }

    private fun handleDiscard() {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || !state.isAwaitingDecision) {
            return
        }
        val updatedDiscard = state.discardCount + 1
        if (updatedDiscard >= state.maxDiscards) {
            completeFailure(
                savedValues = state.savedValues,
                discardCount = updatedDiscard,
                reason = SequenceFailureReason.DISCARDS,
                failureDieValue = null
            )
            return
        }
        advanceOrComplete(
            nextRoll = state.currentRoll + 1,
            savedValues = state.savedValues,
            discardCount = updatedDiscard
        )
    }

    private fun advanceOrComplete(
        nextRoll: Int,
        savedValues: List<Int>,
        discardCount: Int
    ) {
        val state = _uiState.value
        if (state.currentRoll >= state.totalRolls) {
            completeFailure(
                savedValues = savedValues,
                discardCount = discardCount,
                reason = SequenceFailureReason.ROUNDS,
                failureDieValue = null
            )
            return
        }
        startRoll(nextRoll = nextRoll, savedValues = savedValues, discardCount = discardCount)
    }

    private fun startRoll(nextRoll: Int, savedValues: List<Int>, discardCount: Int) {
        rollJob?.cancel()
        _uiState.update {
            it.copy(
                currentRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                isRolling = true,
                isAwaitingDecision = false,
                diceValue = null,
                failureReason = null,
                failureDieValue = null
            )
        }
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            var finalRoll = rollSequenceUseCase.execute().value
            repeat(steps) {
                val roll = rollSequenceUseCase.execute().value
                finalRoll = roll
                _uiState.update { current ->
                    current.copy(diceValue = roll)
                }
                if (rollAnimationMs > 0L) {
                    delay(tickMs)
                }
            }
            _uiState.update {
                it.copy(
                    isRolling = false,
                    isAwaitingDecision = true,
                    diceValue = finalRoll
                )
            }
        }
    }

    private fun completeSuccess(savedValues: List<Int>, discardCount: Int) {
        rollJob?.cancel()
        val rewardCard = resolveRewardCard()
        _uiState.update {
            it.copy(
                savedValues = savedValues,
                discardCount = discardCount,
                isComplete = true,
                isAwaitingDecision = false,
                isRolling = false,
                rewardCard = rewardCard,
                failureReason = null,
                failureDieValue = null
            )
        }
    }

    private fun completeFailure(
        savedValues: List<Int>,
        discardCount: Int,
        reason: SequenceFailureReason,
        failureDieValue: Int?
    ) {
        rollJob?.cancel()
        _uiState.update {
            it.copy(
                savedValues = savedValues,
                discardCount = discardCount,
                isComplete = true,
                isAwaitingDecision = false,
                isRolling = false,
                rewardCard = null,
                failureReason = reason,
                failureDieValue = failureDieValue
            )
        }
    }

    private fun resolveRewardCard(): CardUiModel? {
        val rewardId = selectSequenceRewardCardUseCase.execute()
        return cardUiModels.firstOrNull { it.id == rewardId }
    }
}
