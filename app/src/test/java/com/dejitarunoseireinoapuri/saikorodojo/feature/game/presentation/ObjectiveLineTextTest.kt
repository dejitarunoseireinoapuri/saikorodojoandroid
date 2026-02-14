package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AtLeastParityCountCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactlyDistinctValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasThreeOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasPairCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.StraightCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumAtLeastCondition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class ObjectiveLineTextTest {
    @Test
    fun `selected dice progress uses minimum count text`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_selected_progress,
            formatArgs = listOf(3)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `selected dice progress ignores current selection count`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_selected_progress,
            formatArgs = listOf(3)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `distinct values objective uses string resource`() {
        val condition = ExactlyDistinctValuesCondition(distinctCount = 4)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_exact_distinct_values,
            formatArgs = listOf(4)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `parity count objective uses string resource`() {
        val condition = AtLeastParityCountCondition(minCount = 2, even = true)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_at_least_even_values,
            formatArgs = listOf(2)
        )

        assertEquals(expected, result)
    }


    @Test
    fun `three pairs objective uses dedicated string resource`() {
        val condition = HasPairCondition(requiredPairs = 3)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(resId = R.string.objective_three_pairs)

        assertEquals(expected, result)
    }

    @Test
    fun `two three of a kind objective uses dedicated string resource`() {
        val condition = HasThreeOfKindCondition(requiredTrios = 2)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(resId = R.string.objective_two_three_of_kind)

        assertEquals(expected, result)
    }

    @Test
    fun `sum at least objective explains required total`() {
        val condition = SumAtLeastCondition(threshold = 12)

        val result = objectiveLineExplainText(condition, selectedCount = 5)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_least_explain,
            formatArgs = listOf(12)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `straight objective uses string resource and not plurals`() {
        val condition = StraightCondition(length = 5)

        val result = objectiveLineText(condition)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_straight,
            formatArgs = listOf(5)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `straight objective explain uses dedicated explain string`() {
        val condition = StraightCondition(length = 5)

        val result = objectiveLineExplainText(condition, selectedCount = 5)

        val expected = ObjectiveLineText.StringRes(
            resId = R.string.objective_straight_explain,
            formatArgs = listOf(5)
        )

        assertEquals(expected, result)
    }


    @Test
    fun `three pairs objective explain uses dedicated string resource`() {
        val condition = HasPairCondition(requiredPairs = 3)

        val result = objectiveLineExplainText(condition, selectedCount = 6)

        val expected = ObjectiveLineText.StringRes(resId = R.string.objective_three_pairs_explain)

        assertEquals(expected, result)
    }

    @Test
    fun `selected dice progress has no explain text`() {
        val condition = MinSelectedDiceCondition(minCount = 3)

        val result = objectiveLineExplainText(condition, selectedCount = 2)

        assertNull(result)
    }

    @Test
    fun `format multiplicity uses natural sentence in english`() {
        withDefaultLocale(Locale.ENGLISH) {
            val result = formatMultiplicity(listOf(1, 4, 4))

            assertEquals("1 die showing 1 and 2 dice showing 4", result)
        }
    }

    @Test
    fun `format multiplicity uses natural sentence in spanish`() {
        withDefaultLocale(Locale("es")) {
            val result = formatMultiplicity(listOf(1, 4, 4))

            assertEquals("1 dado de 1 y 2 dados de 4", result)
        }
    }

    @Test
    fun `format multiplicity uses natural sentence in catalan`() {
        withDefaultLocale(Locale("ca")) {
            val result = formatMultiplicity(listOf(1, 4, 4))

            assertEquals("1 dau de 1 i 2 daus de 4", result)
        }
    }

    @Test
    fun `format values keeps singular noun when only one value is required`() {
        withDefaultLocale(Locale.ENGLISH) {
            val result = formatValues(listOf(2, 2, 2))

            assertEquals("1 die showing 2", result)
        }
    }

    private fun withDefaultLocale(locale: Locale, block: () -> Unit) {
        val previous = Locale.getDefault()
        Locale.setDefault(locale)
        try {
            block()
        } finally {
            Locale.setDefault(previous)
        }
    }

}
