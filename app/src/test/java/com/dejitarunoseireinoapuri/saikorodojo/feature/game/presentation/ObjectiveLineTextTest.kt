package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import org.junit.Assert.assertEquals
import org.junit.Test

class ObjectiveLineTextTest {
    @Test
    fun `selected dice progress uses minimum count text`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition, selectedCount = 5)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_selected_progress,
            formatArgs = listOf(3)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `selected dice progress ignores current selection count`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition, selectedCount = 2)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_selected_progress,
            formatArgs = listOf(3)
        )

        assertEquals(expected, result)
    }
}
