package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

internal fun shouldPlayDiceRollForNewDie(
    previousCount: Int,
    currentCount: Int,
    isRolling: Boolean
): Boolean {
    return isRolling && currentCount > previousCount
}
