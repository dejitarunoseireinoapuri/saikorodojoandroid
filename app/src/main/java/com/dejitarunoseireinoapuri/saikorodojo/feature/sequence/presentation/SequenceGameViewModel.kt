package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SequenceFailureReason
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GetPendingMainGameSnapshotUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MinigameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
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
private const val DEFAULT_ROLL_ANIMATION_MS = 1_500L
private const val DEFAULT_TICK_MS = 120L
private const val DEFAULT_REWARD_REVEAL_DELAY_MS = 1_000L
private const val DEFAULT_SAVE_ANIMATION_MS = 320L
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
    val failureDieValue: Int? = null,
    val isLatestSavedValueHidden: Boolean = false
)

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
    private val loadGameSessionUseCase: LoadGameSessionUseCase =
        LoadGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val saveGameSessionUseCase: SaveGameSessionUseCase =
        SaveGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val getPendingMainGameSnapshotUseCase: GetPendingMainGameSnapshotUseCase =
        GetPendingMainGameSnapshotUseCase(GameSessionRepositoryProvider.provide()),
    private val clearGameSessionUseCase: ClearGameSessionUseCase =
        ClearGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val rewardRevealDelayMs: Long = DEFAULT_REWARD_REVEAL_DELAY_MS,
    private val saveAnimationMs: Long = DEFAULT_SAVE_ANIMATION_MS,
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
    private var hideLatestSavedJob: Job? = null
    private var pendingRollJob: Job? = null

    init {
        val session = loadGameSessionUseCase.execute()
        val snapshot = (session as? SavedSession.Minigame)
            ?.takeIf { it.minigameType == MinigameType.SEQUENCE }
            ?.minigameSnapshot as? MinigameSnapshot.Sequence
        if (snapshot != null) {
            if (snapshot.isComplete) {
                clearGameSessionUseCase.execute()
            } else {
                restoreFromSnapshot(snapshot)
            }
        }
    }

    fun onEvent(event: SequenceGameUiEvent) {
        when (event) {
            SequenceGameUiEvent.StartGame -> startGame()
            SequenceGameUiEvent.SaveRoll -> handleSave()
            SequenceGameUiEvent.DiscardRoll -> handleDiscard()
        }
    }

    fun saveSession() {
        if (_uiState.value.isComplete) {
            clearGameSessionUseCase.execute()
            return
        }
        val mainSnapshot = resolveMainGameSnapshot() ?: return
        val snapshot = buildSnapshot()
        saveGameSessionUseCase.execute(
            SavedSession.Minigame(
                minigameType = MinigameType.SEQUENCE,
                minigameSnapshot = snapshot,
                mainGameSnapshot = mainSnapshot
            )
        )
    }

    private fun startGame() {
        rollJob?.cancel()
        hideLatestSavedJob?.cancel()
        pendingRollJob?.cancel()
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
                failureDieValue = null,
                isLatestSavedValueHidden = false
            )
        }
        startRoll(
            nextRoll = 1,
            savedValues = emptyList(),
            discardCount = 0,
            hideLatestSavedValue = false
        )
    }

    private fun handleSave() {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || !state.isAwaitingDecision) {
            return
        }
        val value = state.diceValue ?: return
        val lastSaved = state.savedValues.lastOrNull()
        if (lastSaved != null && value < lastSaved) {
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
            discardCount = state.discardCount,
            hideLatestSavedValue = true,
            rollDelayMs = saveAnimationMs
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
            discardCount = updatedDiscard,
            hideLatestSavedValue = false,
            rollDelayMs = 0L
        )
    }

    private fun advanceOrComplete(
        nextRoll: Int,
        savedValues: List<Int>,
        discardCount: Int,
        hideLatestSavedValue: Boolean,
        rollDelayMs: Long
    ) {
        val state = _uiState.value
        val remainingRolls = (state.totalRolls - nextRoll + 1).coerceAtLeast(0)
        val maxSavesByRounds = savedValues.size + remainingRolls
        val lastSaved = savedValues.lastOrNull()
        val maxSavesByValue = if (lastSaved == null) {
            state.targetSequence
        } else {
            savedValues.size + remainingRolls
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
        if (rollDelayMs > 0L) {
            startDelayedRoll(
                nextRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                hideLatestSavedValue = hideLatestSavedValue,
                rollDelayMs = rollDelayMs
            )
        } else {
            startRoll(
                nextRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                hideLatestSavedValue = hideLatestSavedValue
            )
        }
    }

    private fun startDelayedRoll(
        nextRoll: Int,
        savedValues: List<Int>,
        discardCount: Int,
        hideLatestSavedValue: Boolean,
        rollDelayMs: Long
    ) {
        pendingRollJob?.cancel()
        hideLatestSavedJob?.cancel()
        _uiState.update {
            it.copy(
                currentRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                isRolling = false,
                isAwaitingDecision = false,
                pendingRewardCards = emptyList(),
                failureReason = null,
                failureDieValue = null,
                isLatestSavedValueHidden = hideLatestSavedValue
            )
        }
        if (hideLatestSavedValue) {
            hideLatestSavedJob = viewModelScope.launch(dispatcher) {
                delay(rollDelayMs)
                _uiState.update { current ->
                    current.copy(isLatestSavedValueHidden = false)
                }
            }
        }
        pendingRollJob = viewModelScope.launch(dispatcher) {
            delay(rollDelayMs)
            startRoll(
                nextRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                hideLatestSavedValue = false
            )
        }
    }

    private fun startRoll(
        nextRoll: Int,
        savedValues: List<Int>,
        discardCount: Int,
        hideLatestSavedValue: Boolean
    ) {
        rollJob?.cancel()
        hideLatestSavedJob?.cancel()
        pendingRollJob?.cancel()
        _uiState.update {
            it.copy(
                currentRoll = nextRoll,
                savedValues = savedValues,
                discardCount = discardCount,
                isRolling = true,
                isAwaitingDecision = false,
                diceValue = if (nextRoll == 1) null else it.diceValue,
                pendingRewardCards = emptyList(),
                failureReason = null,
                failureDieValue = null,
                isLatestSavedValueHidden = hideLatestSavedValue
            )
        }
        if (hideLatestSavedValue) {
            hideLatestSavedJob = viewModelScope.launch(dispatcher) {
                delay(saveAnimationMs)
                _uiState.update { current ->
                    current.copy(isLatestSavedValueHidden = false)
                }
            }
        }
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            var finalRoll: Int? = _uiState.value.diceValue
            repeat(steps) { step ->
                val roll = rollSequenceUseCase.execute().value
                finalRoll = roll
                _uiState.update { current ->
                    current.copy(diceValue = roll)
                }
                val shouldDelay = rollAnimationMs > 0L && step < steps - 1
                if (shouldDelay) {
                    delay(tickMs)
                }
            }
            _uiState.update {
                it.copy(
                    isRolling = false,
                    isAwaitingDecision = true,
                    diceValue = finalRoll,
                    isLatestSavedValueHidden = false
                )
            }
        }
    }

    private fun completeSuccess(savedValues: List<Int>, discardCount: Int) {
        rollJob?.cancel()
        hideLatestSavedJob?.cancel()
        pendingRollJob?.cancel()
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
                failureDieValue = null,
                isLatestSavedValueHidden = false
            )
        }
        clearGameSessionUseCase.execute()
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
        hideLatestSavedJob?.cancel()
        pendingRollJob?.cancel()
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
                failureDieValue = failureDieValue,
                isLatestSavedValueHidden = false
            )
        }
        clearGameSessionUseCase.execute()
    }

    private fun resolveRewardCards(): List<CardUiModel> {
        val rewardIds = selectMinigameRewardCardsUseCase.execute()
        addCardsToInventoryUseCase.execute(rewardIds)
        return rewardIds.mapNotNull { rewardId ->
            cardUiModels.firstOrNull { it.id == rewardId }?.copy(count = 1)
        }
    }

    private fun restoreFromSnapshot(snapshot: MinigameSnapshot.Sequence) {
        _uiState.update {
            it.copy(
                isStarted = snapshot.isStarted,
                isRolling = snapshot.isRolling,
                isAwaitingDecision = snapshot.isAwaitingDecision,
                currentRoll = snapshot.currentRoll,
                totalRolls = snapshot.totalRolls,
                targetSequence = snapshot.targetSequence,
                maxDiscards = snapshot.maxDiscards,
                discardCount = snapshot.discardCount,
                savedValues = snapshot.savedValues,
                diceValue = snapshot.diceValue,
                isComplete = snapshot.isComplete,
                rewardCards = mapRewardCards(snapshot.rewardCardIds),
                pendingRewardCards = mapRewardCards(snapshot.pendingRewardCardIds),
                failureReason = snapshot.failureReason,
                failureDieValue = snapshot.failureDieValue,
                isLatestSavedValueHidden = snapshot.isLatestSavedValueHidden
            )
        }
    }

    private fun buildSnapshot(): MinigameSnapshot.Sequence {
        val state = _uiState.value
        return MinigameSnapshot.Sequence(
            isStarted = state.isStarted,
            isRolling = state.isRolling,
            isAwaitingDecision = state.isAwaitingDecision,
            currentRoll = state.currentRoll,
            totalRolls = state.totalRolls,
            targetSequence = state.targetSequence,
            maxDiscards = state.maxDiscards,
            discardCount = state.discardCount,
            savedValues = state.savedValues,
            diceValue = state.diceValue,
            isComplete = state.isComplete,
            rewardCardIds = state.rewardCards.map { it.id },
            pendingRewardCardIds = state.pendingRewardCards.map { it.id },
            failureReason = state.failureReason,
            failureDieValue = state.failureDieValue,
            isLatestSavedValueHidden = state.isLatestSavedValueHidden
        )
    }

    private fun mapRewardCards(cardIds: List<CardId>): List<CardUiModel> {
        return cardIds.mapNotNull { rewardId ->
            cardUiModels.firstOrNull { it.id == rewardId }?.copy(count = 1)
        }
    }

    private fun resolveMainGameSnapshot(): MainGameSnapshot? {
        return getPendingMainGameSnapshotUseCase.execute()
            ?: when (val session = loadGameSessionUseCase.execute()) {
                is SavedSession.Minigame -> session.mainGameSnapshot
                is SavedSession.MainGame -> session.snapshot
                null -> null
            }
    }
}
