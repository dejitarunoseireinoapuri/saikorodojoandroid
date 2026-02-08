package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumParityCondition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldShowSelectedSumTest {
    @Test
    fun `sum parity objective enables selected sum`() {
        val conditions = listOf(SumParityCondition(shouldBeEven = true))

        val result = shouldShowSelectedSum(conditions)

        assertTrue(result)
    }

    @Test
    fun `non sum objective does not enable selected sum`() {
        val conditions = listOf(AllDistinctCondition)

        val result = shouldShowSelectedSum(conditions)

        assertFalse(result)
    }
}
