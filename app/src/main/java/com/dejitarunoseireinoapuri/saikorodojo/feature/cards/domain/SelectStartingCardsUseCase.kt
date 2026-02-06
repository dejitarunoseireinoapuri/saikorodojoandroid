package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class SelectStartingCardsUseCase(
    private val randomProvider: RewardCardsRandomProvider = DefaultRewardCardsRandomProvider()
) {
    fun execute(count: Int = DEFAULT_STARTING_CARDS): List<CardId> {
        if (count <= 0) return emptyList()
        val availableCards = rewardCardIds()
        return List(count) {
            val roll = randomProvider.nextFloat().coerceIn(0f, 0.9999f)
            val index = (roll * availableCards.size).toInt().coerceIn(0, availableCards.lastIndex)
            availableCards[index]
        }
    }

    companion object {
        private const val DEFAULT_STARTING_CARDS = 3
    }
}
