package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain

private const val ACE_VALUE = 1
private const val ACE_BONUS = 10
private const val BLACKJACK_LIMIT = 21

class CalculateBlackjackScoreUseCase {
    fun execute(values: List<Int>): Int {
        val baseTotal = values.sum()
        val aceCount = values.count { it == ACE_VALUE }
        var total = baseTotal
        repeat(aceCount) {
            if (total + ACE_BONUS <= BLACKJACK_LIMIT) {
                total += ACE_BONUS
            }
        }
        return total
    }
}
