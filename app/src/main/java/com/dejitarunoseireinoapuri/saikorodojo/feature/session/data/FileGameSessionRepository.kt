package com.dejitarunoseireinoapuri.saikorodojo.feature.session.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateObjectiveUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.SequenceFailureReason
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MinigameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import java.io.File
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FileGameSessionRepository(
    private val file: File,
    private val json: Json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
) : GameSessionRepository {

    override fun saveSession(session: SavedSession) {
        val updated = readStore().copy(savedSession = session.toStored())
        writeStore(updated)
    }

    override fun loadSession(): SavedSession? {
        return readStore().savedSession?.toDomain()
    }

    override fun clearSession() {
        if (file.exists()) {
            file.delete()
        }
        writeStore(StoredGameSession())
    }

    override fun hasSession(): Boolean {
        return readStore().savedSession != null
    }

    override fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot) {
        val updated = readStore().copy(pendingMainGameSnapshot = snapshot.toStored())
        writeStore(updated)
    }

    override fun getPendingMainGameSnapshot(): MainGameSnapshot? {
        return readStore().pendingMainGameSnapshot?.toDomain()
    }

    private fun readStore(): StoredGameSession {
        if (!file.exists()) return StoredGameSession()
        val content = file.readText()
        if (content.isBlank()) return StoredGameSession()
        return runCatching {
            json.decodeFromString(StoredGameSession.serializer(), content)
        }.getOrElse { StoredGameSession() }
    }

    private fun writeStore(store: StoredGameSession) {
        file.parentFile?.mkdirs()
        val content = json.encodeToString(StoredGameSession.serializer(), store)
        file.writeText(content)
    }
}

@Serializable
internal data class StoredGameSession(
    val savedSession: StoredSavedSession? = null,
    val pendingMainGameSnapshot: StoredMainGameSnapshot? = null
)

@Serializable
internal sealed interface StoredSavedSession {
    @Serializable
    @SerialName("main_game")
    data class MainGame(val snapshot: StoredMainGameSnapshot) : StoredSavedSession

    @Serializable
    @SerialName("minigame")
    data class Minigame(
        val minigameType: String,
        val minigameSnapshot: StoredMinigameSnapshot,
        val mainGameSnapshot: StoredMainGameSnapshot
    ) : StoredSavedSession
}

@Serializable
internal data class StoredMainGameSnapshot(
    val uiSnapshot: StoredGameUiSnapshot,
    val baseSeed: Long,
    val initialRollSnapshot: StoredGameRollSnapshot? = null,
    val hasObjective: Boolean = false
)

@Serializable
internal data class StoredGameRollSnapshot(
    val diceValues: List<Int>,
    val diceTypes: List<String>,
    val layoutSeed: Long
)

@Serializable
internal data class StoredGameUiSnapshot(
    val diceValues: List<Int>,
    val diceCount: Int,
    val diceType: String,
    val diceTypes: List<String>,
    val layoutSeed: Long,
    val isRolling: Boolean,
    val isAwaitingRerollSingle: Boolean,
    val isAwaitingRerollSelected: Boolean,
    val isAwaitingFlipFace: Boolean,
    val isAwaitingAdjustPlusMinus: Boolean,
    val isAwaitingSetValue: Boolean,
    val selectedDice: List<Int>,
    val selectedRerollDice: List<Int>,
    val selectedRerollSingleDieIndex: Int?,
    val selectedFlipDieIndex: Int?,
    val selectedAdjustmentDieIndex: Int?,
    val selectedSetValueDieIndex: Int?,
    val selectedDiceSum: Int,
    val shouldShowSelectedSum: Boolean,
    val cardCounts: Map<String, Int>,
    val selectedCardIndex: Int?,
    val lastAppliedCardId: String?,
    val levelNumber: Int,
    val isLevelComplete: Boolean,
    val showLevelCompleteMessage: Boolean,
    val minigamesAvailable: Int,
    val minigamesPlayedSinceInterstitial: Int = 0
)

@Serializable
internal sealed interface StoredMinigameSnapshot {
    @Serializable
    @SerialName("odd_even")
    data class OddEven(
        val isStarted: Boolean,
        val currentRound: Int,
        val totalRounds: Int,
        val correctCount: Int,
        val wrongCount: Int,
        val targetCorrect: Int,
        val selectedChoice: String?,
        val diceValue: Int?,
        val isRolling: Boolean,
        val showFireworks: Boolean,
        val showFailure: Boolean,
        val isComplete: Boolean,
        val rewardCardIds: List<String>
    ) : StoredMinigameSnapshot

    @Serializable
    @SerialName("sequence")
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
        val pendingSavedValue: Int? = null,
        val pendingFailureDieValue: Int? = null,
        val isAwaitingSaveAnimationConfirmation: Boolean = false,
        val diceValue: Int?,
        val isComplete: Boolean,
        val rewardCardIds: List<String>,
        val pendingRewardCardIds: List<String>,
        val failureReason: String?,
        val failureDieValue: Int?
    ) : StoredMinigameSnapshot

    @Serializable
    @SerialName("blackjack")
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
        val result: String?,
        val rewardCardIds: List<String>,
        val isComplete: Boolean
    ) : StoredMinigameSnapshot

    @Serializable
    @SerialName("higher_lower")
    data class HigherLower(
        val isStarted: Boolean,
        val currentRound: Int,
        val totalRounds: Int,
        val correctStreak: Int,
        val targetCorrect: Int,
        val selectedChoice: String?,
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
        val rewardCardIds: List<String>
    ) : StoredMinigameSnapshot
}

private fun SavedSession.toStored(): StoredSavedSession {
    return when (this) {
        is SavedSession.MainGame -> StoredSavedSession.MainGame(snapshot.toStored())
        is SavedSession.Minigame -> StoredSavedSession.Minigame(
            minigameType = minigameType.name,
            minigameSnapshot = minigameSnapshot.toStored(),
            mainGameSnapshot = mainGameSnapshot.toStored()
        )
    }
}

private fun StoredSavedSession.toDomain(): SavedSession {
    return when (this) {
        is StoredSavedSession.MainGame -> SavedSession.MainGame(snapshot.toDomain())
        is StoredSavedSession.Minigame -> SavedSession.Minigame(
            minigameType = MinigameType.valueOf(minigameType),
            minigameSnapshot = minigameSnapshot.toDomain(),
            mainGameSnapshot = mainGameSnapshot.toDomain()
        )
    }
}

private fun MainGameSnapshot.toStored(): StoredMainGameSnapshot {
    return StoredMainGameSnapshot(
        uiSnapshot = uiSnapshot.toStored(),
        baseSeed = baseSeed,
        initialRollSnapshot = initialRollSnapshot?.toStored(),
        hasObjective = currentObjective != null
    )
}

private fun StoredMainGameSnapshot.toDomain(): MainGameSnapshot {
    val uiSnapshot = uiSnapshot.toDomain()
    val objective = if (hasObjective) {
        GenerateObjectiveUseCase().execute(
            levelNumber = uiSnapshot.levelNumber,
            diceTypes = uiSnapshot.diceTypes,
            seedBase = baseSeed
        )
    } else {
        null
    }
    return MainGameSnapshot(
        uiSnapshot = uiSnapshot,
        baseSeed = baseSeed,
        currentObjective = objective,
        initialRollSnapshot = initialRollSnapshot?.toDomain()
    )
}

private fun GameRollSnapshot.toStored(): StoredGameRollSnapshot {
    return StoredGameRollSnapshot(
        diceValues = diceValues,
        diceTypes = diceTypes.map { it.name },
        layoutSeed = layoutSeed
    )
}

private fun StoredGameRollSnapshot.toDomain(): GameRollSnapshot {
    return GameRollSnapshot(
        diceValues = diceValues,
        diceTypes = diceTypes.map { DiceType.valueOf(it) },
        layoutSeed = layoutSeed
    )
}

private fun GameUiSnapshot.toStored(): StoredGameUiSnapshot {
    return StoredGameUiSnapshot(
        diceValues = diceValues,
        diceCount = diceCount,
        diceType = diceType.name,
        diceTypes = diceTypes.map { it.name },
        layoutSeed = layoutSeed,
        isRolling = isRolling,
        isAwaitingRerollSingle = isAwaitingRerollSingle,
        isAwaitingRerollSelected = isAwaitingRerollSelected,
        isAwaitingFlipFace = isAwaitingFlipFace,
        isAwaitingAdjustPlusMinus = isAwaitingAdjustPlusMinus,
        isAwaitingSetValue = isAwaitingSetValue,
        selectedDice = selectedDice.toList(),
        selectedRerollDice = selectedRerollDice.toList(),
        selectedRerollSingleDieIndex = selectedRerollSingleDieIndex,
        selectedFlipDieIndex = selectedFlipDieIndex,
        selectedAdjustmentDieIndex = selectedAdjustmentDieIndex,
        selectedSetValueDieIndex = selectedSetValueDieIndex,
        selectedDiceSum = selectedDiceSum,
        shouldShowSelectedSum = shouldShowSelectedSum,
        cardCounts = cardCounts.mapKeys { it.key.name },
        selectedCardIndex = selectedCardIndex,
        lastAppliedCardId = lastAppliedCardId?.name,
        levelNumber = levelNumber,
        isLevelComplete = isLevelComplete,
        showLevelCompleteMessage = showLevelCompleteMessage,
        minigamesAvailable = minigamesAvailable,
        minigamesPlayedSinceInterstitial = minigamesPlayedSinceInterstitial
    )
}

private fun StoredGameUiSnapshot.toDomain(): GameUiSnapshot {
    return GameUiSnapshot(
        diceValues = diceValues,
        diceCount = diceCount,
        diceType = DiceType.valueOf(diceType),
        diceTypes = diceTypes.map { DiceType.valueOf(it) },
        layoutSeed = layoutSeed,
        isRolling = isRolling,
        isAwaitingRerollSingle = isAwaitingRerollSingle,
        isAwaitingRerollSelected = isAwaitingRerollSelected,
        isAwaitingFlipFace = isAwaitingFlipFace,
        isAwaitingAdjustPlusMinus = isAwaitingAdjustPlusMinus,
        isAwaitingSetValue = isAwaitingSetValue,
        selectedDice = selectedDice.toSet(),
        selectedRerollDice = selectedRerollDice.toSet(),
        selectedRerollSingleDieIndex = selectedRerollSingleDieIndex,
        selectedFlipDieIndex = selectedFlipDieIndex,
        selectedAdjustmentDieIndex = selectedAdjustmentDieIndex,
        selectedSetValueDieIndex = selectedSetValueDieIndex,
        selectedDiceSum = selectedDiceSum,
        shouldShowSelectedSum = shouldShowSelectedSum,
        cardCounts = cardCounts.mapKeys { CardId.valueOf(it.key) },
        selectedCardIndex = selectedCardIndex,
        lastAppliedCardId = lastAppliedCardId?.let { CardId.valueOf(it) },
        levelNumber = levelNumber,
        isLevelComplete = isLevelComplete,
        showLevelCompleteMessage = showLevelCompleteMessage,
        minigamesAvailable = minigamesAvailable,
        minigamesPlayedSinceInterstitial = minigamesPlayedSinceInterstitial
    )
}

private fun MinigameSnapshot.toStored(): StoredMinigameSnapshot {
    return when (this) {
        is MinigameSnapshot.OddEven -> StoredMinigameSnapshot.OddEven(
            isStarted = isStarted,
            currentRound = currentRound,
            totalRounds = totalRounds,
            correctCount = correctCount,
            wrongCount = wrongCount,
            targetCorrect = targetCorrect,
            selectedChoice = selectedChoice?.name,
            diceValue = diceValue,
            isRolling = isRolling,
            showFireworks = showFireworks,
            showFailure = showFailure,
            isComplete = isComplete,
            rewardCardIds = rewardCardIds.map { it.name }
        )
        is MinigameSnapshot.Sequence -> StoredMinigameSnapshot.Sequence(
            isStarted = isStarted,
            isRolling = isRolling,
            isAwaitingDecision = isAwaitingDecision,
            currentRoll = currentRoll,
            totalRolls = totalRolls,
            targetSequence = targetSequence,
            maxDiscards = maxDiscards,
            discardCount = discardCount,
            savedValues = savedValues,
            pendingSavedValue = pendingSavedValue,
            pendingFailureDieValue = pendingFailureDieValue,
            isAwaitingSaveAnimationConfirmation = isAwaitingSaveAnimationConfirmation,
            diceValue = diceValue,
            isComplete = isComplete,
            rewardCardIds = rewardCardIds.map { it.name },
            pendingRewardCardIds = pendingRewardCardIds.map { it.name },
            failureReason = failureReason?.name,
            failureDieValue = failureDieValue
        )
        is MinigameSnapshot.Blackjack -> StoredMinigameSnapshot.Blackjack(
            isStarted = isStarted,
            isRolling = isRolling,
            isPlayerTurn = isPlayerTurn,
            isDealerTurn = isDealerTurn,
            isAwaitingDecision = isAwaitingDecision,
            playerDice = playerDice,
            dealerDice = dealerDice,
            playerTotal = playerTotal,
            dealerTotal = dealerTotal,
            showPlayerBust = showPlayerBust,
            showDealerBust = showDealerBust,
            result = result?.name,
            rewardCardIds = rewardCardIds.map { it.name },
            isComplete = isComplete
        )
        is MinigameSnapshot.HigherLower -> StoredMinigameSnapshot.HigherLower(
            isStarted = isStarted,
            currentRound = currentRound,
            totalRounds = totalRounds,
            correctStreak = correctStreak,
            targetCorrect = targetCorrect,
            selectedChoice = selectedChoice?.name,
            baseDiceValues = baseDiceValues,
            currentDiceValues = currentDiceValues,
            isCurrentDiceHidden = isCurrentDiceHidden,
            isCurrentDiceAnchoredUp = isCurrentDiceAnchoredUp,
            isRolling = isRolling,
            isChoiceVisible = isChoiceVisible,
            isTransitioning = isTransitioning,
            isSuccessHighlighting = isSuccessHighlighting,
            isComplete = isComplete,
            hasLoss = hasLoss,
            rewardCardIds = rewardCardIds.map { it.name }
        )
    }
}

private fun StoredMinigameSnapshot.toDomain(): MinigameSnapshot {
    return when (this) {
        is StoredMinigameSnapshot.OddEven -> MinigameSnapshot.OddEven(
            isStarted = isStarted,
            currentRound = currentRound,
            totalRounds = totalRounds,
            correctCount = correctCount,
            wrongCount = wrongCount,
            targetCorrect = targetCorrect,
            selectedChoice = selectedChoice?.let { OddEvenChoice.valueOf(it) },
            diceValue = diceValue,
            isRolling = isRolling,
            showFireworks = showFireworks,
            showFailure = showFailure,
            isComplete = isComplete,
            rewardCardIds = rewardCardIds.map { CardId.valueOf(it) }
        )
        is StoredMinigameSnapshot.Sequence -> MinigameSnapshot.Sequence(
            isStarted = isStarted,
            isRolling = isRolling,
            isAwaitingDecision = isAwaitingDecision,
            currentRoll = currentRoll,
            totalRolls = totalRolls,
            targetSequence = targetSequence,
            maxDiscards = maxDiscards,
            discardCount = discardCount,
            savedValues = savedValues,
            pendingSavedValue = pendingSavedValue,
            pendingFailureDieValue = pendingFailureDieValue,
            isAwaitingSaveAnimationConfirmation = isAwaitingSaveAnimationConfirmation,
            diceValue = diceValue,
            isComplete = isComplete,
            rewardCardIds = rewardCardIds.map { CardId.valueOf(it) },
            pendingRewardCardIds = pendingRewardCardIds.map { CardId.valueOf(it) },
            failureReason = failureReason?.let { SequenceFailureReason.valueOf(it) },
            failureDieValue = failureDieValue
        )
        is StoredMinigameSnapshot.Blackjack -> MinigameSnapshot.Blackjack(
            isStarted = isStarted,
            isRolling = isRolling,
            isPlayerTurn = isPlayerTurn,
            isDealerTurn = isDealerTurn,
            isAwaitingDecision = isAwaitingDecision,
            playerDice = playerDice,
            dealerDice = dealerDice,
            playerTotal = playerTotal,
            dealerTotal = dealerTotal,
            showPlayerBust = showPlayerBust,
            showDealerBust = showDealerBust,
            result = result?.let { BlackjackOutcome.valueOf(it) },
            rewardCardIds = rewardCardIds.map { CardId.valueOf(it) },
            isComplete = isComplete
        )
        is StoredMinigameSnapshot.HigherLower -> MinigameSnapshot.HigherLower(
            isStarted = isStarted,
            currentRound = currentRound,
            totalRounds = totalRounds,
            correctStreak = correctStreak,
            targetCorrect = targetCorrect,
            selectedChoice = selectedChoice?.let { HigherLowerChoice.valueOf(it) },
            baseDiceValues = baseDiceValues,
            currentDiceValues = currentDiceValues,
            isCurrentDiceHidden = isCurrentDiceHidden,
            isCurrentDiceAnchoredUp = isCurrentDiceAnchoredUp,
            isRolling = isRolling,
            isChoiceVisible = isChoiceVisible,
            isTransitioning = isTransitioning,
            isSuccessHighlighting = isSuccessHighlighting,
            isComplete = isComplete,
            hasLoss = hasLoss,
            rewardCardIds = rewardCardIds.map { CardId.valueOf(it) }
        )
    }
}
