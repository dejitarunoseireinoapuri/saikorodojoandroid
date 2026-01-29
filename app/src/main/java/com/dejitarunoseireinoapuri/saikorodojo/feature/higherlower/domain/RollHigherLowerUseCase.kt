package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain

class RollHigherLowerUseCase(
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {
    fun execute(range: IntRange = 1..10, diceCount: Int = 2): HigherLowerRoll {
        val values = List(diceCount) { diceRoller.roll(range) }
        return HigherLowerRoll(values = values)
    }
}
