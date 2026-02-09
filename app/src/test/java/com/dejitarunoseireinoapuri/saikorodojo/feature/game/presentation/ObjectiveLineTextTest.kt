package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AtLeastParityCountCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactlyDistinctValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasPairCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
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

    @Test
    fun `distinct values objective uses string resource`() {
        val condition = ExactlyDistinctValuesCondition(distinctCount = 4)

        val result = objectiveLineText(condition, selectedCount = 4)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_exact_distinct_values,
            formatArgs = listOf(4)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `parity count objective uses string resource`() {
        val condition = AtLeastParityCountCondition(minCount = 2, even = true)

        val result = objectiveLineText(condition, selectedCount = 2)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_at_least_even_values,
            formatArgs = listOf(2)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `sum exact objective explain uses explain string resource`() {
        val condition = SumExactCondition(target = 10)

        val result = objectiveLineExplainText(condition, selectedCount = 0)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_exact_explain,
            formatArgs = listOf(10)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `pair objective explain uses explain string resource`() {
        val condition = HasPairCondition(requiredPairs = 1)

        val result = objectiveLineExplainText(condition, selectedCount = 0)

        val expected = ObjectiveLineText.StringRes(resId = R.string.objective_pair_explain)

        assertEquals(expected, result)
    }

    @Test
    fun `min selected dice objective hides info`() {
        val condition = MinSelectedDiceCondition(minCount = 2)

        val result = shouldShowObjectiveInfo(condition)

        assertEquals(false, result)
    }

    @Test
    fun `non min selected dice objective shows info`() {
        val condition = HasPairCondition(requiredPairs = 1)

        val result = shouldShowObjectiveInfo(condition)

        assertEquals(true, result)
    }
}
