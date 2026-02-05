package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain

class RollSequenceUseCase(
    private val diceRoller: DiceRoller = RandomDiceRoller()
) {
    fun execute(range: IntRange = 1..10): SequenceRoll {
        val value = diceRoller.roll(range)
        return SequenceRoll(value = value)
    }
}
