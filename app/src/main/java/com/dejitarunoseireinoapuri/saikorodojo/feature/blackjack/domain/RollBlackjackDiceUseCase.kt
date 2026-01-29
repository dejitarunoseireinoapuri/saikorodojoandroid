package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

private const val DEFAULT_MIN_DIE = 1
private const val DEFAULT_MAX_DIE = 10

class RollBlackjackDiceUseCase(
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {
    fun execute(count: Int, range: IntRange = DEFAULT_MIN_DIE..DEFAULT_MAX_DIE): List<Int> {
        return List(count.coerceAtLeast(0)) { diceRoller.roll(range) }
    }
}
