package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import kotlin.random.Random

fun interface BlackjackRandomProvider {
    fun nextInt(bound: Int): Int
}

class DefaultBlackjackRandomProvider(
    private val random: Random = Random.Default
) : BlackjackRandomProvider {
    override fun nextInt(bound: Int): Int = random.nextInt(bound)
}

class SelectBlackjackRewardCardUseCase(
    private val randomProvider: BlackjackRandomProvider = DefaultBlackjackRandomProvider()
) {
    fun execute(): CardId {
        val cards = blackjackRewardCardIds()
        val index = randomProvider.nextInt(cards.size)
        return cards[index]
    }
}

fun blackjackRewardCardIds(): List<CardId> {
    return listOf(
        CardId.ADJUST_PLUS_MINUS_ONE,
        CardId.FLIP_FACE,
        CardId.REROLL_SINGLE,
        CardId.REROLL_ALL,
        CardId.SET_VALUE,
        CardId.REPEAT_LAST
    )
}
