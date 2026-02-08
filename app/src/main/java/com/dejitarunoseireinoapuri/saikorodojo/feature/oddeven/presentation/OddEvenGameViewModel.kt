package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
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

private const val DEFAULT_TOTAL_ROUNDS = 7
private const val DEFAULT_TARGET_CORRECT = 3
private const val DEFAULT_ROLL_ANIMATION_MS = 750L
private const val DEFAULT_RESULT_ANIMATION_MS = 1_500L
private const val DEFAULT_TICK_MS = 120L
private const val DEFAULT_LOSS_MESSAGE_DELAY_MS = 1_000L

data class OddEvenGameUiState(
    val isStarted: Boolean = false,
    val currentRound: Int = 0,
    val totalRounds: Int = DEFAULT_TOTAL_ROUNDS,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val targetCorrect: Int = DEFAULT_TARGET_CORRECT,
    val selectedChoice: OddEvenChoice? = null,
    val diceValue: Int? = null,
    val isRolling: Boolean = false,
    val showFireworks: Boolean = false,
    val showFailure: Boolean = false,
    val isComplete: Boolean = false,
    val rewardCards: List<CardUiModel> = emptyList()
)

sealed interface OddEvenGameUiEvent {
    data object StartGame : OddEvenGameUiEvent
    data class SelectChoice(val choice: OddEvenChoice) : OddEvenGameUiEvent
}

class OddEvenGameViewModel(
    private val rollOddEvenUseCase: RollOddEvenUseCase = RollOddEvenUseCase(),
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
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val resultAnimationMs: Long = DEFAULT_RESULT_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val lossMessageDelayMs: Long = DEFAULT_LOSS_MESSAGE_DELAY_MS,
    private val totalRounds: Int = DEFAULT_TOTAL_ROUNDS,
    private val targetCorrect: Int = DEFAULT_TARGET_CORRECT,
    private val cardUiModels: List<CardUiModel> = defaultCardUiModels()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        OddEvenGameUiState(
            totalRounds = totalRounds,
            targetCorrect = targetCorrect
        )
    )
    val uiState: StateFlow<OddEvenGameUiState> = _uiState

    private var roundJob: Job? = null

    init {
        val session = loadGameSessionUseCase.execute()
        val snapshot = (session as? SavedSession.Minigame)
            ?.takeIf { it.minigameType == MinigameType.ODD_EVEN }
            ?.minigameSnapshot as? MinigameSnapshot.OddEven
        if (snapshot != null) {
            restoreFromSnapshot(snapshot)
        }
    }

    fun onEvent(event: OddEvenGameUiEvent) {
        when (event) {
            OddEvenGameUiEvent.StartGame -> startGame()
            is OddEvenGameUiEvent.SelectChoice -> handleChoice(event.choice)
        }
    }

    fun saveSession() {
        val mainSnapshot = resolveMainGameSnapshot() ?: return
        val snapshot = buildSnapshot()
        saveGameSessionUseCase.execute(
            SavedSession.Minigame(
                minigameType = MinigameType.ODD_EVEN,
                minigameSnapshot = snapshot,
                mainGameSnapshot = mainSnapshot
            )
        )
    }

    private fun startGame() {
        roundJob?.cancel()
        _uiState.update {
            it.copy(
                isStarted = true,
                currentRound = 1,
                correctCount = 0,
                wrongCount = 0,
                selectedChoice = null,
                diceValue = null,
                isRolling = false,
                showFireworks = false,
                showFailure = false,
                isComplete = false,
                rewardCards = emptyList()
            )
        }
    }

    private fun handleChoice(choice: OddEvenChoice) {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || state.selectedChoice != null) {
            return
        }
        roundJob?.cancel()
        _uiState.update {
            it.copy(
                selectedChoice = choice,
                isRolling = true,
                showFireworks = false,
                showFailure = false
            )
        }
        roundJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            var finalRoll = rollOddEvenUseCase.execute()
            repeat(steps) {
                val roll = rollOddEvenUseCase.execute()
                finalRoll = roll
                _uiState.update { current ->
                    current.copy(diceValue = roll.value)
                }
                if (rollAnimationMs > 0L) {
                    delay(tickMs)
                }
            }
            val isCorrect = finalRoll.isEven == (choice == OddEvenChoice.EVEN)
            val updatedCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
            val updatedWrong = if (isCorrect) state.wrongCount else state.wrongCount + 1
            _uiState.update {
                it.copy(
                    correctCount = updatedCorrect,
                    wrongCount = updatedWrong,
                    isRolling = false,
                    showFireworks = isCorrect,
                    showFailure = !isCorrect
                )
            }
            val nextRound = state.currentRound + 1
            val hasWon = updatedCorrect >= targetCorrect
            val remainingRounds = totalRounds - state.currentRound
            val hasImpossibleWin = updatedCorrect + remainingRounds < targetCorrect
            val hasLossOutcome = !hasWon && (nextRound > totalRounds || hasImpossibleWin)

            if (!hasWon && !hasLossOutcome) {
                _uiState.update {
                    it.copy(
                        selectedChoice = null,
                        currentRound = nextRound
                    )
                }
            }

            if (resultAnimationMs > 0L && (hasWon || hasLossOutcome)) {
                delay(resultAnimationMs)
            }

            if (hasLossOutcome && lossMessageDelayMs > 0L) {
                delay(lossMessageDelayMs)
            }
            val isComplete = hasWon || hasLossOutcome
            val rewardCards = if (hasWon) {
                resolveRewardCards()
            } else {
                emptyList()
            }
            _uiState.update {
                it.copy(
                    selectedChoice = null,
                    isComplete = isComplete,
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

    private fun restoreFromSnapshot(snapshot: MinigameSnapshot.OddEven) {
        _uiState.update {
            it.copy(
                isStarted = snapshot.isStarted,
                currentRound = snapshot.currentRound,
                totalRounds = snapshot.totalRounds,
                correctCount = snapshot.correctCount,
                wrongCount = snapshot.wrongCount,
                targetCorrect = snapshot.targetCorrect,
                selectedChoice = snapshot.selectedChoice,
                diceValue = snapshot.diceValue,
                isRolling = snapshot.isRolling,
                showFireworks = snapshot.showFireworks,
                showFailure = snapshot.showFailure,
                isComplete = snapshot.isComplete,
                rewardCards = mapRewardCards(snapshot.rewardCardIds)
            )
        }
    }

    private fun buildSnapshot(): MinigameSnapshot.OddEven {
        val state = _uiState.value
        return MinigameSnapshot.OddEven(
            isStarted = state.isStarted,
            currentRound = state.currentRound,
            totalRounds = state.totalRounds,
            correctCount = state.correctCount,
            wrongCount = state.wrongCount,
            targetCorrect = state.targetCorrect,
            selectedChoice = state.selectedChoice,
            diceValue = state.diceValue,
            isRolling = state.isRolling,
            showFireworks = state.showFireworks,
            showFailure = state.showFailure,
            isComplete = state.isComplete,
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
}
