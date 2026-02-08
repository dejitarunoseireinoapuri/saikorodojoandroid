package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class ObjectiveLineTextTest {
    @Test
    fun `selected dice progress clamps to minimum`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition, selectedCount = 5)

        val expected = ObjectiveLineText.PluralRes(
            resId = R.plurals.objective_selected_progress,
            quantity = 3,
            formatArgs = listOf(3, 3)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `selected dice progress keeps current count when below minimum`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition, selectedCount = 2)

        val expected = ObjectiveLineText.PluralRes(
            resId = R.plurals.objective_selected_progress,
            quantity = 2,
            formatArgs = listOf(2, 3)
        )

        assertEquals(expected, result)
    }
}
