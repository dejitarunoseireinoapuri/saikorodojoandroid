package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import kotlin.random.Random

private const val DICE_MIN = 1

enum class DiceType(val sides: Int) {
    D6(6),
    D8(8),
    D10(10);

    val maxExclusive: Int
        get() = sides + 1
}

fun interface DiceRandomProvider {
    fun nextInt(from: Int, until: Int): Int
}

class DefaultDiceRandomProvider : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return Random.nextInt(from, until)
    }
}

class RollDiceUseCase(
    private val randomProvider: DiceRandomProvider = DefaultDiceRandomProvider()
) {
    fun execute(diceTypes: List<DiceType>): List<Int> {
        return diceTypes.map { diceType ->
            randomProvider.nextInt(DICE_MIN, diceType.maxExclusive)
        }
    }

    fun execute(count: Int, diceType: DiceType = DiceType.D6): List<Int> {
        return List(count) { randomProvider.nextInt(DICE_MIN, diceType.maxExclusive) }
    }
}
