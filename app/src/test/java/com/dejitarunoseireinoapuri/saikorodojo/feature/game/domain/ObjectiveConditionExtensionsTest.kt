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
}
