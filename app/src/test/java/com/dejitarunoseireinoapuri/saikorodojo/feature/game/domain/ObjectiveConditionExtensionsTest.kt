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
    fun `exact selected dice requires exact count`() {
        val condition = ExactSelectedDiceCondition(count = 4)

        assertTrue(condition.isMet(listOf(1, 2, 3, 4)))
        assertFalse(condition.isMet(listOf(1, 2, 3)))
    }

    @Test
    fun `exact two pairs rejects three of a kind`() {
        val condition = ExactTwoPairsCondition

        assertTrue(condition.isMet(listOf(2, 2, 5, 5)))
        assertFalse(condition.isMet(listOf(3, 3, 3, 5)))
    }

    @Test
    fun `satisfy and avoid combines both conditions`() {
        val condition = SatisfyAndAvoidCondition(
            required = SumAtLeastCondition(threshold = 9),
            forbidden = ContainsValuesCondition(values = listOf(6))
        )

        assertTrue(condition.isMet(listOf(4, 5)))
        assertFalse(condition.isMet(listOf(4, 5, 6)))
    }
}
