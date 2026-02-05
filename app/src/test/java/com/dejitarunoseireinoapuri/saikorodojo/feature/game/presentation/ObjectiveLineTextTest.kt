package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumMultipleCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObjectiveLineTextTest {
    @Test
    fun `min selection objective includes current progress`() {
        val condition = MinSelectedDiceCondition(minCount = 4)

        val (textRes, args) = objectiveLineText(condition, selectedCount = 2)

        assertEquals(R.string.objective_selected_progress, textRes)
        assertEquals(listOf(2, 4), args)
    }

    @Test
    fun `selected sum visibility matches sum objectives`() {
        assertTrue(shouldShowSelectedSum(listOf(SumExactCondition(target = 8))))
        assertTrue(shouldShowSelectedSum(listOf(SumMultipleCondition(factor = 3))))
        assertFalse(shouldShowSelectedSum(listOf(AllDistinctCondition)))
    }

    @Test
    fun `exact selection objective text uses exact resource`() {
        val (textRes, args) = objectiveLineText(
            condition = com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactSelectedDiceCondition(
                count = 5
            ),
            selectedCount = 2
        )

        assertEquals(R.string.objective_selected_exact, textRes)
        assertEquals(listOf(2, 5), args)
    }
}
