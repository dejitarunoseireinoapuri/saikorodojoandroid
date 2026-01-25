package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import kotlin.random.Random

private const val DICE_MIN = 1
private const val DICE_MAX_EXCLUSIVE = 7

fun interface DiceRandomProvider {
    fun nextInt(from: Int, until: Int): Int
}

class DefaultDiceRandomProvider : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return Random.Default.nextInt(from, until)
    }
}

class RollDiceUseCase(
    private val randomProvider: DiceRandomProvider = DefaultDiceRandomProvider()
) {
    fun execute(count: Int): List<Int> {
        return List(count) { randomProvider.nextInt(DICE_MIN, DICE_MAX_EXCLUSIVE) }
    }
}
