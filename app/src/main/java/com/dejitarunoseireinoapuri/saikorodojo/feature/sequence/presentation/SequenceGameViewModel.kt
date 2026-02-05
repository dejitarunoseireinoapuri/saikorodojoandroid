package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
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
private const val DEFAULT_REWARD_REVEAL_DELAY_MS = 1_500L
private const val DEFAULT_SEQUENCE_DIE_MAX = 10

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
    val rewardCards: List<CardUiModel> = emptyList(),
    val pendingRewardCards: List<CardUiModel> = emptyList(),
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
    private val selectMinigameRewardCardsUseCase: SelectMinigameRewardCardsUseCase =
        SelectMinigameRewardCardsUseCase(),
    private val addCardsToInventoryUseCase: AddCardsToInventoryUseCase =
        AddCardsToInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val rewardRevealDelayMs: Long = DEFAULT_REWARD_REVEAL_DELAY_MS,
    private val totalRolls: Int = DEFAULT_TOTAL_ROLLS,
    private val targetSequence: Int = DEFAULT_TARGET_SEQUENCE,
    private val maxDiscards: Int = DEFAULT_MAX_DISCARDS,
    private val cardUiModels: List<CardUiModel> = defaultCardUiModels(),
    private val sequenceDieMax: Int = DEFAULT_SEQUENCE_DIE_MAX
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
                rewardCards = emptyList(),
                pendingRewardCards = emptyList(),
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
        val remainingRolls = (state.totalRolls - nextRoll + 1).coerceAtLeast(0)
        val maxSavesByRounds = savedValues.size + remainingRolls
        val lastSaved = savedValues.lastOrNull()
        val maxSavesByValue = if (lastSaved == null) {
            state.targetSequence
        } else {
            savedValues.size + (sequenceDieMax - lastSaved).coerceAtLeast(0)
        }
        val canStillWin =
            maxSavesByRounds >= state.targetSequence && maxSavesByValue >= state.targetSequence

        if (nextRoll > state.totalRolls || !canStillWin) {
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
                pendingRewardCards = emptyList(),
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
        val rewardCards = resolveRewardCards()
        _uiState.update {
            it.copy(
                savedValues = savedValues,
                discardCount = discardCount,
                isComplete = true,
                isAwaitingDecision = false,
                isRolling = false,
                rewardCards = emptyList(),
                pendingRewardCards = rewardCards,
                failureReason = null,
                failureDieValue = null
            )
        }
        viewModelScope.launch(dispatcher) {
            delay(rewardRevealDelayMs)
            _uiState.update { current ->
                current.copy(
                    rewardCards = current.pendingRewardCards,
                    pendingRewardCards = emptyList()
                )
            }
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
                rewardCards = emptyList(),
                pendingRewardCards = emptyList(),
                failureReason = reason,
                failureDieValue = failureDieValue
            )
        }
    }

    private fun resolveRewardCards(): List<CardUiModel> {
        val rewardIds = selectMinigameRewardCardsUseCase.execute()
        addCardsToInventoryUseCase.execute(rewardIds)
        return rewardIds.mapNotNull { rewardId ->
            cardUiModels.firstOrNull { it.id == rewardId }?.copy(count = 1)
        }
    }
}
