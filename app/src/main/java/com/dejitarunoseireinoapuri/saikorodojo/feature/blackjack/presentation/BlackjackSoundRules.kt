package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome

internal fun shouldPlayInitialDealDiceRoll(
    previousPlayerCount: Int,
    currentPlayerCount: Int,
    previousDealerCount: Int,
    currentDealerCount: Int,
    isRolling: Boolean
): Boolean {
    return isRolling &&
        previousPlayerCount == 0 &&
        previousDealerCount == 0 &&
        currentPlayerCount > 0 &&
        currentDealerCount > 0
}

internal fun shouldPlayOutcomeSound(
    previousOutcome: BlackjackOutcome?,
    currentOutcome: BlackjackOutcome?,
    isStarted: Boolean
): Boolean {
    return isStarted && previousOutcome == null && currentOutcome != null
}
