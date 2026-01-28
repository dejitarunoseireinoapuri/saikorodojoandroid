package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import kotlin.random.Random

fun interface IntRandomProvider {
    fun nextInt(bound: Int): Int
}

class DefaultIntRandomProvider(
    private val random: Random = Random.Default
) : IntRandomProvider {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}

class SelectOddEvenRewardCardUseCase(
    private val randomProvider: IntRandomProvider = DefaultIntRandomProvider()
) {
    fun execute(): CardId {
        val cards = oddEvenRewardCardIds()
        val index = randomProvider.nextInt(cards.size)
        return cards[index]
    }
}

fun oddEvenRewardCardIds(): List<CardId> {
    return listOf(
        CardId.ADJUST_PLUS_MINUS_ONE,
        CardId.FLIP_FACE,
        CardId.REROLL_SINGLE,
        CardId.REROLL_ALL,
        CardId.SET_VALUE,
        CardId.REPEAT_LAST
    )
}
