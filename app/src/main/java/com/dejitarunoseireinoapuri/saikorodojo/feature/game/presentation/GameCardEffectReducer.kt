package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId

internal object GameCardEffectReducer {
    fun reduce(
        state: GameUiState,
        cardId: CardId,
        minigamesRewardAmount: Int
    ): GameUiState {
        return when (cardId) {
            CardId.REROLL_SINGLE -> resetInteractionState(state).copy(
                selectedCardIndex = null,
                interactionMode = DiceInteractionMode.AwaitingRerollSingle
            )

            CardId.FLIP_FACE -> resetInteractionState(state).copy(
                selectedCardIndex = null,
                interactionMode = DiceInteractionMode.AwaitingFlipFace
            )

            CardId.ADJUST_PLUS_MINUS_ONE -> resetInteractionState(state).copy(
                selectedCardIndex = null,
                interactionMode = DiceInteractionMode.AwaitingAdjustPlusMinus
            )

            CardId.SET_VALUE -> resetInteractionState(state).copy(
                selectedCardIndex = null,
                interactionMode = DiceInteractionMode.AwaitingSetValue
            )

            CardId.REROLL_ALL -> resetInteractionState(state).copy(
                selectedCardIndex = null,
                interactionMode = DiceInteractionMode.AwaitingRerollSelected,
                selectedRerollDice = emptySet()
            )

            CardId.REPEAT_LAST -> state.copy(selectedCardIndex = null)
            CardId.MINIGAMES -> state.copy(
                selectedCardIndex = null,
                minigamesAvailable = clampMinigamesAvailable(state.minigamesAvailable + minigamesRewardAmount),
                showMinigamesAdPrompt = false
            )
        }
    }

    fun resetInteractionState(state: GameUiState): GameUiState {
        return state.copy(
            interactionMode = DiceInteractionMode.Normal,
            selectedRerollSingleDieIndex = null,
            selectedFlipDieIndex = null,
            selectedAdjustmentDieIndex = null,
            selectedSetValueDieIndex = null
        )
    }
}
