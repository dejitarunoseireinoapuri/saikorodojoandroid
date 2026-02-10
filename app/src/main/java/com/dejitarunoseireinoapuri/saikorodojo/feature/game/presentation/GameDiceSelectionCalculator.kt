package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

internal fun calculateSelectedDiceSum(
    diceValues: List<Int>,
    selectedDice: Set<Int>
): Int {
    return selectedDice.sumOf { index -> diceValues.getOrNull(index) ?: 0 }
}
