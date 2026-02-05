package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectiveLineTextTest {
    @Test
    fun `min selection objective includes current progress`() {
        val condition = MinSelectedDiceCondition(minCount = 4)

        val (textRes, args) = objectiveLineText(condition, selectedCount = 2)

        assertEquals(R.string.objective_min_selected, textRes)
        assertEquals(listOf(4, 2), args)
    }

    @Test
    fun `selected sum visibility matches sum objectives`() {
        assertTrue(shouldShowSelectedSum(listOf(SumExactCondition(target = 8))))
        assertFalse(shouldShowSelectedSum(listOf(AllDistinctCondition)))
    }
}
