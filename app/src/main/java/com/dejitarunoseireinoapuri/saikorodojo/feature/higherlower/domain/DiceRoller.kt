package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain

import kotlin.random.Random

fun interface DiceRoller {
    fun roll(range: IntRange): Int
}

class RandomDiceRoller(
    private val random: Random = Random.Default
) : DiceRoller {
    override fun roll(range: IntRange): Int {
        return random.nextInt(range.first, range.last + 1)
    }
}
