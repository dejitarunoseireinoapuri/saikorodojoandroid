package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerRoll
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.RollHigherLowerUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
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

private const val DEFAULT_TOTAL_ROUNDS = 3
private const val DEFAULT_TARGET_CORRECT = 3
private const val DEFAULT_ROLL_ANIMATION_MS = 1_500L
private const val DEFAULT_TICK_MS = 120L
private const val DEFAULT_RESULT_DELAY_MS = 1_500L
private const val DEFAULT_TRANSITION_MS = 750L
private const val DEFAULT_SUCCESS_HIGHLIGHT_MS = 1_000L
private const val DEFAULT_SUCCESS_RESULT_DELAY_MS = 1_000L
private const val DEFAULT_POST_TRANSITION_HOLD_MS = 250L
private const val MAX_NON_TIE_REROLLS = 3

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
    val isCurrentDiceHidden: Boolean = false,
    val isCurrentDiceAnchoredUp: Boolean = false,
    val isRolling: Boolean = false,
    val isChoiceVisible: Boolean = false,
    val isTransitioning: Boolean = false,
    val isSuccessHighlighting: Boolean = false,
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
    private val resultDelayMs: Long = DEFAULT_RESULT_DELAY_MS,
    private val transitionMs: Long = DEFAULT_TRANSITION_MS,
    private val successHighlightMs: Long = DEFAULT_SUCCESS_HIGHLIGHT_MS,
    private val successResultDelayMs: Long = DEFAULT_SUCCESS_RESULT_DELAY_MS,
    private val postTransitionHoldMs: Long = DEFAULT_POST_TRANSITION_HOLD_MS,
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

    init {
        val session = loadGameSessionUseCase.execute()
        val snapshot = (session as? SavedSession.Minigame)
            ?.takeIf { it.minigameType == MinigameType.HIGHER_LOWER }
            ?.minigameSnapshot as? MinigameSnapshot.HigherLower
        if (snapshot != null) {
            restoreFromSnapshot(snapshot)
        }
    }

    fun onEvent(event: HigherLowerGameUiEvent) {
        when (event) {
            HigherLowerGameUiEvent.StartGame -> startGame()
            is HigherLowerGameUiEvent.SelectChoice -> handleChoice(event.choice)
        }
    }

    fun saveSession() {
        val mainSnapshot = resolveMainGameSnapshot() ?: return
        val snapshot = buildSnapshot()
        saveGameSessionUseCase.execute(
            SavedSession.Minigame(
                minigameType = MinigameType.HIGHER_LOWER,
                minigameSnapshot = snapshot,
                mainGameSnapshot = mainSnapshot
            )
        )
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
                isCurrentDiceHidden = true,
                isCurrentDiceAnchoredUp = false,
                isRolling = true,
                isChoiceVisible = false,
                isTransitioning = false,
                isSuccessHighlighting = false,
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
                        isChoiceVisible = true,
                        isCurrentDiceHidden = true
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
                isChoiceVisible = false,
                currentDiceValues = emptyList(),
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = false
            )
        }
        startRoll(
            onTick = { roll -> updateCurrentDice(roll) },
            onComplete = { roll ->
                val baseSum = DiceSum(state.baseDiceValues.sum())
                val resolvedRoll = resolveNonTieRoll(baseSum = baseSum, initialRoll = roll)
                val newSum = DiceSum(resolvedRoll.sum)
                if (isCorrectGuess(choice, baseSum, newSum)) {
                    resolveCorrectGuess(resolvedRoll.values)
                } else {
                    resolveLoss(resolvedRoll.values)
                }
            }
        )
    }

    private fun resolveNonTieRoll(baseSum: DiceSum, initialRoll: HigherLowerRoll): HigherLowerRoll {
        if (initialRoll.sum != baseSum.value) return initialRoll
        repeat(MAX_NON_TIE_REROLLS) {
            val reroll = rollHigherLowerUseCase.execute()
            if (reroll.sum != baseSum.value) {
                return reroll
            }
        }
        return initialRoll
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
            _uiState.update { it.copy(correctStreak = updatedStreak) }
            resolveWin()
            return
        }
        rollJob = viewModelScope.launch(dispatcher) {
            _uiState.update {
                it.copy(
                    correctStreak = updatedStreak,
                    currentRound = it.currentRound + 1,
                    selectedChoice = null,
                    isRolling = false,
                    isTransitioning = false,
                    isChoiceVisible = false,
                    isSuccessHighlighting = true
                )
            }
            if (successHighlightMs > 0L) {
                delay(successHighlightMs)
            }
            _uiState.update {
                it.copy(
                    isTransitioning = true,
                    isSuccessHighlighting = false
                )
            }
            if (transitionMs > 0L) {
                delay(transitionMs)
            }
            val finalCurrentDice = _uiState.value.currentDiceValues
            _uiState.update {
                it.copy(
                    baseDiceValues = when {
                        finalCurrentDice.isNotEmpty() -> finalCurrentDice
                        newValues.isNotEmpty() -> newValues
                        else -> it.baseDiceValues
                    },
                    isCurrentDiceHidden = false,
                    isCurrentDiceAnchoredUp = true,
                    isTransitioning = false,
                    isChoiceVisible = false
                )
            }
            if (postTransitionHoldMs > 0L) {
                delay(postTransitionHoldMs)
            }
            _uiState.update {
                it.copy(
                    isCurrentDiceHidden = false,
                    isCurrentDiceAnchoredUp = true,
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
                    isSuccessHighlighting = false,
                    isComplete = true,
                    hasLoss = true
                )
            }
        }
    }

    private fun resolveWin() {
        rollJob = viewModelScope.launch(dispatcher) {
            _uiState.update {
                it.copy(
                    isRolling = false,
                    isChoiceVisible = false,
                    isTransitioning = false,
                    isSuccessHighlighting = true
                )
            }
            if (successResultDelayMs > 0L) {
                delay(successResultDelayMs)
            }
            val rewardCards = resolveRewardCards()
            _uiState.update {
                it.copy(
                    isRolling = false,
                    isChoiceVisible = false,
                    isTransitioning = false,
                    isSuccessHighlighting = false,
                    isComplete = true,
                    rewardCards = rewardCards
                )
            }
        }
    }

    private fun resolveRewardCards(): List<CardUiModel> {
        val rewardIds = selectMinigameRewardCardsUseCase.execute()
        addCardsToInventoryUseCase.execute(rewardIds)
        return rewardIds.mapNotNull { rewardId ->
            cardUiModels.firstOrNull { it.id == rewardId }?.copy(count = 1)
        }
    }

    private fun restoreFromSnapshot(snapshot: MinigameSnapshot.HigherLower) {
        _uiState.update {
            it.copy(
                isStarted = snapshot.isStarted,
                currentRound = snapshot.currentRound,
                totalRounds = snapshot.totalRounds,
                correctStreak = snapshot.correctStreak,
                targetCorrect = snapshot.targetCorrect,
                selectedChoice = snapshot.selectedChoice,
                baseDiceValues = snapshot.baseDiceValues,
                currentDiceValues = snapshot.currentDiceValues,
                isCurrentDiceHidden = snapshot.isCurrentDiceHidden,
                isCurrentDiceAnchoredUp = snapshot.isCurrentDiceAnchoredUp,
                isRolling = snapshot.isRolling,
                isChoiceVisible = snapshot.isChoiceVisible,
                isTransitioning = snapshot.isTransitioning,
                isSuccessHighlighting = snapshot.isSuccessHighlighting,
                isComplete = snapshot.isComplete,
                hasLoss = snapshot.hasLoss,
                rewardCards = mapRewardCards(snapshot.rewardCardIds)
            )
        }
    }

    private fun buildSnapshot(): MinigameSnapshot.HigherLower {
        val state = _uiState.value
        return MinigameSnapshot.HigherLower(
            isStarted = state.isStarted,
            currentRound = state.currentRound,
            totalRounds = state.totalRounds,
            correctStreak = state.correctStreak,
            targetCorrect = state.targetCorrect,
            selectedChoice = state.selectedChoice,
            baseDiceValues = state.baseDiceValues,
            currentDiceValues = state.currentDiceValues,
            isCurrentDiceHidden = state.isCurrentDiceHidden,
            isCurrentDiceAnchoredUp = state.isCurrentDiceAnchoredUp,
            isRolling = state.isRolling,
            isChoiceVisible = state.isChoiceVisible,
            isTransitioning = state.isTransitioning,
            isSuccessHighlighting = state.isSuccessHighlighting,
            isComplete = state.isComplete,
            hasLoss = state.hasLoss,
            rewardCardIds = state.rewardCards.map { it.id }
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
