package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class ObjectiveLineTextTest {
    @Test
    fun `selected dice progress clamps to minimum`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val (textRes, args) = objectiveLineText(condition, selectedCount = 5)

        assertEquals(R.string.objective_selected_progress, textRes)
        assertEquals(listOf(3, 3), args)
    }

    @Test
    fun `selected dice progress keeps current count when below minimum`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val (_, args) = objectiveLineText(condition, selectedCount = 2)

        assertEquals(listOf(2, 3), args)
    }
}
