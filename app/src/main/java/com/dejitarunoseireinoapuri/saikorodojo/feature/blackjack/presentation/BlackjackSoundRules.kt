package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome

internal fun shouldPlayDiceRollSound(
    previousPlayerCount: Int,
    currentPlayerCount: Int,
    previousDealerCount: Int,
    currentDealerCount: Int,
    isRolling: Boolean
): Boolean {
    val playerAddedDie = currentPlayerCount > previousPlayerCount
    val dealerAddedDie = currentDealerCount > previousDealerCount
    return isRolling && (playerAddedDie || dealerAddedDie)
}

internal fun shouldPlayOutcomeSound(
    previousOutcome: BlackjackOutcome?,
    currentOutcome: BlackjackOutcome?,
    isStarted: Boolean
): Boolean {
    return isStarted && previousOutcome == null && currentOutcome == BlackjackOutcome.PLAYER_WIN
}
