package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain

class RollSequenceUseCase(
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {
    fun execute(range: IntRange = 1..8): SequenceRoll {
        val value = diceRoller.roll(range)
        return SequenceRoll(value = value)
    }
}
