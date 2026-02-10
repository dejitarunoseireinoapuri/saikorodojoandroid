package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectiveConditionExtensionsTest {
    @Test
    fun `sum at most validates upper bound`() {
        val condition = SumAtMostCondition(threshold = 10)

        assertTrue(condition.isMet(listOf(3, 3, 4)))
        assertFalse(condition.isMet(listOf(6, 6)))
    }

    @Test
    fun `sum multiple validates factor`() {
        val condition = SumMultipleCondition(factor = 3)

        assertTrue(condition.isMet(listOf(2, 4)))
        assertFalse(condition.isMet(listOf(2, 5)))
    }

    @Test
    fun `at least parity count validates minimum`() {
        val condition = AtLeastParityCountCondition(minCount = 2, even = true)

        assertTrue(condition.isMet(listOf(2, 4, 5)))
        assertFalse(condition.isMet(listOf(2, 3, 5)))
    }

    @Test
    fun `pair condition counts repeated pairs from the same value`() {
        val condition = HasPairCondition(requiredPairs = 2)

        assertTrue(condition.isMet(listOf(4, 4, 4, 4)))
        assertFalse(condition.isMet(listOf(4, 4, 4)))
    }

    @Test
    fun `three of a kind condition counts repeated trios from the same value`() {
        val condition = HasThreeOfKindCondition(requiredTrios = 2)

        assertTrue(condition.isMet(listOf(6, 6, 6, 6, 6, 6)))
        assertFalse(condition.isMet(listOf(6, 6, 6, 6, 6)))
    }

    @Test
    fun `same die value can satisfy pair and contains objectives simultaneously`() {
        val pairCondition = HasPairCondition(requiredPairs = 1)
        val containsCondition = ContainsValuesCondition(values = listOf(3, 6))
        val diceValues = listOf(3, 3, 6)

        assertTrue(pairCondition.isMet(diceValues))
        assertTrue(containsCondition.isMet(diceValues))
    }

}
