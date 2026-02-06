package com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain

import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SequenceFailureReason

sealed interface SavedSession {
    data class MainGame(val snapshot: MainGameSnapshot) : SavedSession

    data class Minigame(
        val minigameType: MinigameType,
        val minigameSnapshot: MinigameSnapshot,
        val mainGameSnapshot: MainGameSnapshot
    ) : SavedSession
}

data class MainGameSnapshot(
    val uiSnapshot: GameUiSnapshot,
    val baseSeed: Long,
    val currentObjective: LevelObjective?,
    val initialRollSnapshot: GameRollSnapshot?
)

data class GameRollSnapshot(
    val diceValues: List<Int>,
    val diceTypes: List<DiceType>,
    val layoutSeed: Long
)

data class GameUiSnapshot(
    val diceValues: List<Int>,
    val diceCount: Int,
    val diceType: DiceType,
    val diceTypes: List<DiceType>,
    val layoutSeed: Long,
    val isRolling: Boolean,
    val isAwaitingRerollSingle: Boolean,
    val isAwaitingRerollSelected: Boolean,
    val isAwaitingFlipFace: Boolean,
    val isAwaitingAdjustPlusMinus: Boolean,
    val isAwaitingSetValue: Boolean,
    val selectedDice: Set<Int>,
    val selectedRerollDice: Set<Int>,
    val selectedRerollSingleDieIndex: Int?,
    val selectedAdjustmentDieIndex: Int?,
    val selectedSetValueDieIndex: Int?,
    val selectedDiceSum: Int,
    val shouldShowSelectedSum: Boolean,
    val cardCounts: Map<CardId, Int>,
    val selectedCardIndex: Int?,
    val lastAppliedCardId: CardId?,
    val levelNumber: Int,
    val isLevelComplete: Boolean,
    val showLevelCompleteMessage: Boolean
)

sealed interface MinigameSnapshot {
    data class OddEven(
        val isStarted: Boolean,
        val currentRound: Int,
        val totalRounds: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val targetCorrect: Int,
        val selectedChoice: OddEvenChoice?,
        val diceValue: Int?,
        val isRolling: Boolean,
        val showFireworks: Boolean,
        val showFailure: Boolean,
        val isComplete: Boolean,
        val rewardCardIds: List<CardId>
    ) : MinigameSnapshot

    data class Sequence(
        val isStarted: Boolean,
        val isRolling: Boolean,
        val isAwaitingDecision: Boolean,
        val currentRoll: Int,
        val totalRolls: Int,
        val targetSequence: Int,
        val maxDiscards: Int,
        val discardCount: Int,
        val savedValues: List<Int>,
        val diceValue: Int?,
        val isComplete: Boolean,
        val rewardCardIds: List<CardId>,
        val pendingRewardCardIds: List<CardId>,
        val failureReason: SequenceFailureReason?,
        val failureDieValue: Int?,
        val isLatestSavedValueHidden: Boolean
    ) : MinigameSnapshot

    data class Blackjack(
        val isStarted: Boolean,
        val isRolling: Boolean,
        val isPlayerTurn: Boolean,
        val isDealerTurn: Boolean,
        val isAwaitingDecision: Boolean,
        val playerDice: List<Int>,
        val dealerDice: List<Int>,
        val playerTotal: Int,
        val dealerTotal: Int,
        val showPlayerBust: Boolean,
        val showDealerBust: Boolean,
        val result: BlackjackOutcome?,
        val rewardCardIds: List<CardId>,
        val isComplete: Boolean
    ) : MinigameSnapshot

    data class HigherLower(
        val isStarted: Boolean,
        val currentRound: Int,
        val totalRounds: Int,
        val correctStreak: Int,
        val targetCorrect: Int,
        val selectedChoice: HigherLowerChoice?,
        val baseDiceValues: List<Int>,
        val currentDiceValues: List<Int>,
        val isCurrentDiceHidden: Boolean,
        val isCurrentDiceAnchoredUp: Boolean,
        val isRolling: Boolean,
        val isChoiceVisible: Boolean,
        val isTransitioning: Boolean,
        val isSuccessHighlighting: Boolean,
        val isComplete: Boolean,
        val hasLoss: Boolean,
        val rewardCardIds: List<CardId>
    ) : MinigameSnapshot
}
