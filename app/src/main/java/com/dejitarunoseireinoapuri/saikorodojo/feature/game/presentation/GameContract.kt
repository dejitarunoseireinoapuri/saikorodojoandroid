package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType

private const val DEFAULT_DICE_COUNT = 5
private const val DEFAULT_MINIGAMES_AVAILABLE = 3

sealed interface DiceInteractionMode {
    data object Normal : DiceInteractionMode
    data object AwaitingRerollSingle : DiceInteractionMode
    data object AwaitingRerollSelected : DiceInteractionMode
    data object AwaitingFlipFace : DiceInteractionMode
    data object AwaitingAdjustPlusMinus : DiceInteractionMode
    data object AwaitingSetValue : DiceInteractionMode
}

data class GameUiState(
    val diceValues: List<Int> = List(DEFAULT_DICE_COUNT) { 1 },
    val diceCount: Int = DEFAULT_DICE_COUNT,
    val diceType: DiceType = DiceType.D6,
    val diceTypes: List<DiceType> = List(DEFAULT_DICE_COUNT) { DiceType.D6 },
    val layoutSeed: Long = 0L,
    val isRolling: Boolean = false,
    val interactionMode: DiceInteractionMode = DiceInteractionMode.Normal,
    val selectedDice: Set<Int> = emptySet(),
    val selectedRerollDice: Set<Int> = emptySet(),
    val selectedRerollSingleDieIndex: Int? = null,
    val selectedFlipDieIndex: Int? = null,
    val selectedAdjustmentDieIndex: Int? = null,
    val selectedSetValueDieIndex: Int? = null,
    val selectedDiceSum: Int = 0,
    val shouldShowSelectedSum: Boolean = false,
    val cardUiModels: List<CardUiModel> = emptyList(),
    val selectedCardIndex: Int? = null,
    val lastAppliedCardId: CardId? = null,
    val levelNumber: Int = 1,
    val objectiveLines: List<ObjectiveLineUiState> = emptyList(),
    val isLevelComplete: Boolean = false,
    val showLevelCompleteMessage: Boolean = false,
    val minigamesAvailable: Int = DEFAULT_MINIGAMES_AVAILABLE,
    val showMinigamesAdPrompt: Boolean = false,
    val minigamesPlayedSinceInterstitial: Int = 0
) {
    val isAwaitingRerollSingle: Boolean
        get() = interactionMode is DiceInteractionMode.AwaitingRerollSingle

    val isAwaitingRerollSelected: Boolean
        get() = interactionMode is DiceInteractionMode.AwaitingRerollSelected

    val isAwaitingFlipFace: Boolean
        get() = interactionMode is DiceInteractionMode.AwaitingFlipFace

    val isAwaitingAdjustPlusMinus: Boolean
        get() = interactionMode is DiceInteractionMode.AwaitingAdjustPlusMinus

    val isAwaitingSetValue: Boolean
        get() = interactionMode is DiceInteractionMode.AwaitingSetValue
}

sealed interface GameUiEvent {
    data object StartRoll : GameUiEvent
    data object RefreshInventory : GameUiEvent
    data class DiceClicked(val index: Int) : GameUiEvent
    data class SelectCard(val index: Int) : GameUiEvent
    data class ApplyCard(val index: Int) : GameUiEvent
    data class AdjustSelectedDie(val delta: Int) : GameUiEvent
    data class SetSelectedDieValue(val value: Int) : GameUiEvent
    data object RollSelectedDice : GameUiEvent
    data object RollSingleDie : GameUiEvent
    data object FlipSelectedDie : GameUiEvent
    data object DismissSelectedCard : GameUiEvent
    data object IncreaseDiceCount : GameUiEvent
    data object ConfirmSurrender : GameUiEvent
    data object ConfirmExit : GameUiEvent
    data object OpenRandomMinigame : GameUiEvent
    data object ConfirmMinigamesAd : GameUiEvent
    data object DismissMinigamesAdPrompt : GameUiEvent
    data object MinigamesAdCompleted : GameUiEvent
    data object LevelInterstitialAdCompleted : GameUiEvent
}

sealed interface GameUiEffect {
    data class NavigateToMinigame(val minigame: MinigameType) : GameUiEffect
    data class NavigateToMenu(val resetProgress: Boolean) : GameUiEffect
    data object ShowMinigamesRewardedAd : GameUiEffect
    data object ShowLevelInterstitialAd : GameUiEffect
}

data class ObjectiveLineUiState(
    val text: ObjectiveLineText,
    val explainText: ObjectiveLineText?,
    val isMet: Boolean
)

sealed interface ObjectiveLineText {
    data class StringRes(
        val resId: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ObjectiveLineText

    data class PluralRes(
        val resId: Int,
        val quantity: Int,
        val formatArgs: List<Any> = emptyList()
    ) : ObjectiveLineText
}
