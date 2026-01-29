package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.CalculateBlackjackScoreUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DetermineBlackjackOutcomeUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.RollBlackjackDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.SelectBlackjackRewardCardUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DEFAULT_INITIAL_PLAYER_DICE = 2
private const val DEFAULT_INITIAL_DEALER_DICE = 1
private const val DEFAULT_ROLL_ANIMATION_MS = 2_000L
private const val DEFAULT_TICK_MS = 120L
private const val DEFAULT_RESULT_DELAY_MS = 1_500L
private const val DEFAULT_BUST_HIGHLIGHT_MS = 1_500L
private const val DEFAULT_DEALER_STAND_TOTAL = 17
private const val BLACKJACK_LIMIT = 21

data class BlackjackGameUiState(
    val isStarted: Boolean = false,
    val isRolling: Boolean = false,
    val isPlayerTurn: Boolean = false,
    val isDealerTurn: Boolean = false,
    val isAwaitingDecision: Boolean = false,
    val playerDice: List<Int> = emptyList(),
    val dealerDice: List<Int> = emptyList(),
    val playerTotal: Int = 0,
    val dealerTotal: Int = 0,
    val showPlayerBust: Boolean = false,
    val showDealerBust: Boolean = false,
    val result: BlackjackOutcome? = null,
    val rewardCard: CardUiModel? = null,
    val isComplete: Boolean = false
)

sealed interface BlackjackGameUiEvent {
    data object StartGame : BlackjackGameUiEvent
    data object Hit : BlackjackGameUiEvent
    data object Stand : BlackjackGameUiEvent
}

class BlackjackGameViewModel(
    private val rollBlackjackDiceUseCase: RollBlackjackDiceUseCase = RollBlackjackDiceUseCase(),
    private val calculateBlackjackScoreUseCase: CalculateBlackjackScoreUseCase =
        CalculateBlackjackScoreUseCase(),
    private val determineBlackjackOutcomeUseCase: DetermineBlackjackOutcomeUseCase =
        DetermineBlackjackOutcomeUseCase(),
    private val selectBlackjackRewardCardUseCase: SelectBlackjackRewardCardUseCase =
        SelectBlackjackRewardCardUseCase(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollAnimationMs: Long = DEFAULT_ROLL_ANIMATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val resultDelayMs: Long = DEFAULT_RESULT_DELAY_MS,
    private val bustHighlightMs: Long = DEFAULT_BUST_HIGHLIGHT_MS,
    private val dealerStandTotal: Int = DEFAULT_DEALER_STAND_TOTAL,
    private val initialPlayerDice: Int = DEFAULT_INITIAL_PLAYER_DICE,
    private val initialDealerDice: Int = DEFAULT_INITIAL_DEALER_DICE,
    private val cardUiModels: List<CardUiModel> = defaultCardUiModels()
) : ViewModel() {
    private val _uiState = MutableStateFlow(BlackjackGameUiState())
    val uiState: StateFlow<BlackjackGameUiState> = _uiState

    private var rollJob: Job? = null
    private var dealerJob: Job? = null

    fun onEvent(event: BlackjackGameUiEvent) {
        when (event) {
            BlackjackGameUiEvent.StartGame -> startGame()
            BlackjackGameUiEvent.Hit -> handleHit()
            BlackjackGameUiEvent.Stand -> handleStand()
        }
    }

    private fun startGame() {
        rollJob?.cancel()
        dealerJob?.cancel()
        _uiState.update {
            it.copy(
                isStarted = true,
                isRolling = false,
                isPlayerTurn = false,
                isDealerTurn = false,
                isAwaitingDecision = false,
                playerDice = emptyList(),
                dealerDice = emptyList(),
                playerTotal = 0,
                dealerTotal = 0,
                showPlayerBust = false,
                showDealerBust = false,
                result = null,
                rewardCard = null,
                isComplete = false
            )
        }
        startInitialRoll()
    }

    private fun startInitialRoll() {
        rollJob?.cancel()
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            var playerValues = emptyList<Int>()
            var dealerValues = emptyList<Int>()
            repeat(steps) {
                playerValues = rollBlackjackDiceUseCase.execute(initialPlayerDice)
                dealerValues = rollBlackjackDiceUseCase.execute(initialDealerDice)
                updateDiceState(
                    playerValues = playerValues,
                    dealerValues = dealerValues,
                    isRolling = true,
                    isAwaitingDecision = false,
                    updateTotals = false
                )
                if (rollAnimationMs > 0L) {
                    delay(tickMs)
                }
            }
            updateDiceState(
                playerValues = playerValues,
                dealerValues = dealerValues,
                isRolling = false,
                isAwaitingDecision = true,
                updateTotals = true,
                isPlayerTurn = true
            )
        }
    }

    private fun handleHit() {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || !state.isAwaitingDecision) {
            return
        }
        rollJob?.cancel()
        rollJob = viewModelScope.launch(dispatcher) {
            val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
            val dice = state.playerDice.toMutableList().apply { add(1) }
            repeat(steps) {
                val value = rollBlackjackDiceUseCase.execute(1).firstOrNull() ?: 1
                dice[dice.lastIndex] = value
                updateDiceState(
                    playerValues = dice.toList(),
                    dealerValues = state.dealerDice,
                    isRolling = true,
                    isAwaitingDecision = false,
                    updateTotals = false
                )
                if (rollAnimationMs > 0L) {
                    delay(tickMs)
                }
            }
            val playerTotal = calculateBlackjackScoreUseCase.execute(dice)
            val isBust = playerTotal > BLACKJACK_LIMIT
            _uiState.update {
                it.copy(
                    playerDice = dice.toList(),
                    playerTotal = playerTotal,
                    dealerTotal = it.dealerTotal,
                    isRolling = false,
                    isAwaitingDecision = !isBust,
                    isPlayerTurn = !isBust,
                    showPlayerBust = isBust
                )
            }
            if (isBust) {
                scheduleResult(
                    delayMs = bustHighlightMs,
                    playerTotal = playerTotal,
                    dealerTotal = state.dealerTotal,
                    isPlayerBust = true,
                    isDealerBust = false
                )
            }
        }
    }

    private fun handleStand() {
        val state = _uiState.value
        if (!state.isStarted || state.isRolling || state.isComplete || !state.isAwaitingDecision) {
            return
        }
        _uiState.update {
            it.copy(
                isAwaitingDecision = false,
                isPlayerTurn = false,
                isDealerTurn = true
            )
        }
        startDealerTurn()
    }

    private fun startDealerTurn() {
        dealerJob?.cancel()
        dealerJob = viewModelScope.launch(dispatcher) {
            var dealerValues = _uiState.value.dealerDice
            var dealerTotal = calculateBlackjackScoreUseCase.execute(dealerValues)
            var isDealerBust = dealerTotal > BLACKJACK_LIMIT
            while (dealerTotal < dealerStandTotal && !isDealerBust) {
                dealerValues = animateDealerRoll(dealerValues)
                dealerTotal = calculateBlackjackScoreUseCase.execute(dealerValues)
                isDealerBust = dealerTotal > BLACKJACK_LIMIT
                _uiState.update { current ->
                    current.copy(
                        dealerDice = dealerValues,
                        dealerTotal = dealerTotal,
                        isRolling = false
                    )
                }
            }
            _uiState.update {
                it.copy(
                    dealerDice = dealerValues,
                    dealerTotal = dealerTotal,
                    isDealerTurn = false,
                    isRolling = false,
                    showDealerBust = isDealerBust
                )
            }
            scheduleResult(
                delayMs = if (isDealerBust) bustHighlightMs else resultDelayMs,
                playerTotal = _uiState.value.playerTotal,
                dealerTotal = dealerTotal,
                isPlayerBust = false,
                isDealerBust = isDealerBust
            )
        }
    }

    private suspend fun animateDealerRoll(currentDice: List<Int>): List<Int> {
        rollJob?.cancel()
        val steps = (rollAnimationMs / tickMs).coerceAtLeast(1L).toInt()
        val dice = currentDice.toMutableList().apply { add(1) }
        repeat(steps) {
            val value = rollBlackjackDiceUseCase.execute(1).firstOrNull() ?: 1
            dice[dice.lastIndex] = value
            updateDiceState(
                playerValues = _uiState.value.playerDice,
                dealerValues = dice.toList(),
                isRolling = true,
                isAwaitingDecision = false,
                updateTotals = false,
                isDealerTurn = true
            )
            if (rollAnimationMs > 0L) {
                delay(tickMs)
            }
        }
        return dice.toList()
    }

    private fun updateDiceState(
        playerValues: List<Int>,
        dealerValues: List<Int>,
        isRolling: Boolean,
        isAwaitingDecision: Boolean,
        updateTotals: Boolean,
        isPlayerTurn: Boolean = _uiState.value.isPlayerTurn,
        isDealerTurn: Boolean = _uiState.value.isDealerTurn
    ) {
        val playerTotal = if (updateTotals) {
            calculateBlackjackScoreUseCase.execute(playerValues)
        } else {
            _uiState.value.playerTotal
        }
        val dealerTotal = if (updateTotals) {
            calculateBlackjackScoreUseCase.execute(dealerValues)
        } else {
            _uiState.value.dealerTotal
        }
        _uiState.update {
            it.copy(
                playerDice = playerValues,
                dealerDice = dealerValues,
                playerTotal = playerTotal,
                dealerTotal = dealerTotal,
                isRolling = isRolling,
                isAwaitingDecision = isAwaitingDecision,
                isPlayerTurn = isPlayerTurn,
                isDealerTurn = isDealerTurn
            )
        }
    }

    private fun scheduleResult(
        delayMs: Long,
        playerTotal: Int,
        dealerTotal: Int,
        isPlayerBust: Boolean,
        isDealerBust: Boolean
    ) {
        viewModelScope.launch(dispatcher) {
            if (delayMs > 0L) {
                delay(delayMs)
            }
            val outcome = determineBlackjackOutcomeUseCase.execute(
                playerTotal = playerTotal,
                dealerTotal = dealerTotal,
                isPlayerBust = isPlayerBust,
                isDealerBust = isDealerBust
            )
            val rewardCard = if (outcome == BlackjackOutcome.PLAYER_WIN) {
                resolveRewardCard(playerTotal)
            } else {
                null
            }
            _uiState.update {
                it.copy(
                    result = outcome,
                    rewardCard = rewardCard,
                    isComplete = true
                )
            }
        }
    }

    private fun resolveRewardCard(playerTotal: Int): CardUiModel? {
        val rewardId = if (playerTotal == BLACKJACK_LIMIT) {
            CardId.RETRY
        } else {
            selectBlackjackRewardCardUseCase.execute()
        }
        return cardUiModels.firstOrNull { it.id == rewardId }
    }
}
