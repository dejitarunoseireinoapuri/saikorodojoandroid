package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

internal fun isCardInteractionBlocked(state: GameUiState): Boolean {
    return state.interactionMode != DiceInteractionMode.Normal
}
