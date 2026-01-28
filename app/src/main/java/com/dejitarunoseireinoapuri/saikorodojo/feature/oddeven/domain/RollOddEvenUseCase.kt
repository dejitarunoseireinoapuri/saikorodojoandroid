package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain

class RollOddEvenUseCase(
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {
    fun execute(range: IntRange = 1..6): OddEvenRoll {
        val value = diceRoller.roll(range)
        return OddEvenRoll(
            value = value,
            isEven = value % 2 == 0
        )
    }
}
