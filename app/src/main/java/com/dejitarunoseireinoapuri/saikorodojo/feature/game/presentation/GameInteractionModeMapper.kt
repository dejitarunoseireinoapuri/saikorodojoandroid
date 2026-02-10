package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot

internal fun restoreInteractionMode(snapshot: GameUiSnapshot): DiceInteractionMode {
    return when {
        snapshot.isAwaitingRerollSingle -> DiceInteractionMode.AwaitingRerollSingle
        snapshot.isAwaitingRerollSelected -> DiceInteractionMode.AwaitingRerollSelected
        snapshot.isAwaitingFlipFace -> DiceInteractionMode.AwaitingFlipFace
        snapshot.isAwaitingAdjustPlusMinus -> DiceInteractionMode.AwaitingAdjustPlusMinus
        snapshot.isAwaitingSetValue -> DiceInteractionMode.AwaitingSetValue
        else -> DiceInteractionMode.Normal
    }
}
