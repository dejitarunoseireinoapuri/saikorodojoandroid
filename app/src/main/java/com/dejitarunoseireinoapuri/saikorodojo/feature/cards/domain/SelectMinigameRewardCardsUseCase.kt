package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

import kotlin.random.Random

fun interface RewardCardsRandomProvider {
    fun nextFloat(): Float
}

class DefaultRewardCardsRandomProvider(
    private val random: Random = Random.Default
) : RewardCardsRandomProvider {
    override fun nextFloat(): Float = random.nextFloat()
}

class SelectMinigameRewardCardsUseCase(
    private val randomProvider: RewardCardsRandomProvider = DefaultRewardCardsRandomProvider()
) {
    private val standardRewards = rewardCardIds()
    private val standardWeight = (1f - MINIGAMES_WEIGHT) / standardRewards.size

    fun execute(): List<CardId> {
        val rewardCount = if (randomProvider.nextFloat() < TWO_CARD_THRESHOLD) {
            MIN_REWARD_CARDS
        } else {
            MAX_REWARD_CARDS
        }
        val rewards = mutableListOf<CardId>()
        var minigamesAvailable = true
        repeat(rewardCount) {
            val reward = selectReward(minigamesAvailable)
            rewards.add(reward)
            if (reward == CardId.MINIGAMES) {
                minigamesAvailable = false
            }
        }
        return rewards
    }

    private fun selectReward(minigamesAvailable: Boolean): CardId {
        val weightedRewards = if (minigamesAvailable) {
            buildList {
                add(WeightedReward(CardId.MINIGAMES, MINIGAMES_WEIGHT))
                standardRewards.forEach { id ->
                    add(WeightedReward(id, standardWeight))
                }
            }
        } else {
            val fallbackWeight = 1f / standardRewards.size
            standardRewards.map { id ->
                WeightedReward(id, fallbackWeight)
            }
        }
        val totalWeight = weightedRewards.sumOf { it.weight.toDouble() }.toFloat()
        val roll = randomProvider.nextFloat() * totalWeight
        var cumulative = 0f
        for (reward in weightedRewards) {
            cumulative += reward.weight
            if (roll < cumulative) {
                return reward.cardId
            }
        }
        return weightedRewards.last().cardId
    }

    private data class WeightedReward(val cardId: CardId, val weight: Float)

    companion object {
        private const val MIN_REWARD_CARDS = 2
        private const val MAX_REWARD_CARDS = 3
        private const val TWO_CARD_THRESHOLD = 0.5f
        private const val MINIGAMES_WEIGHT = 0.07f
    }
}

fun rewardCardIds(): List<CardId> {
    return listOf(
        CardId.ADJUST_PLUS_MINUS_ONE,
        CardId.FLIP_FACE,
        CardId.REROLL_SINGLE,
        CardId.REROLL_ALL,
        CardId.SET_VALUE,
        CardId.REPEAT_LAST
    )
}
