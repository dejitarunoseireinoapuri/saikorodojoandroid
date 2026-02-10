package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AllDistinctCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.AtLeastParityCountCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ContainsValuesWithMultiplicityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ExactlyDistinctValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ForbidValuesCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.FullHouseCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasFourOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasPairCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.HasThreeOfKindCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinSelectedDiceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ObjectiveCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.StraightCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumAtLeastCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumAtMostCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumExactCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumInRangeCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumMaxDifferenceCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumMultipleCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.SumParityCondition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.ThreeOfKindWithValueCondition

internal fun shouldShowSelectedSum(conditions: List<ObjectiveCondition>): Boolean {
    return conditions.any { condition ->
        condition is SumAtLeastCondition ||
            condition is SumAtMostCondition ||
            condition is SumExactCondition ||
            condition is SumInRangeCondition ||
            condition is SumMultipleCondition ||
            condition is SumMaxDifferenceCondition ||
            condition is SumParityCondition
    }
}

internal fun objectiveLineText(condition: ObjectiveCondition): ObjectiveLineText {
    return when (condition) {
        is SumAtLeastCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_least,
            formatArgs = listOf(condition.threshold)
        )
        is SumAtMostCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_most,
            formatArgs = listOf(condition.threshold)
        )
        is SumExactCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_exact,
            formatArgs = listOf(condition.target)
        )
        is SumMultipleCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_multiple,
            formatArgs = listOf(condition.factor)
        )
        is SumMaxDifferenceCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_max_difference,
            formatArgs = listOf(condition.target, condition.maxDifference)
        )
        is SumInRangeCondition -> {
            if (condition.min == condition.max) {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_sum_exact,
                    formatArgs = listOf(condition.min)
                )
            } else {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_sum_in_range,
                    formatArgs = listOf(condition.min, condition.max)
                )
            }
        }

        is SumParityCondition -> {
            if (condition.shouldBeEven) {
                ObjectiveLineText.StringRes(resId = R.string.objective_sum_even)
            } else {
                ObjectiveLineText.StringRes(resId = R.string.objective_sum_odd)
            }
        }

        is HasPairCondition -> {
            if (condition.requiredPairs >= 2) {
                ObjectiveLineText.StringRes(resId = R.string.objective_two_pairs)
            } else {
                ObjectiveLineText.StringRes(resId = R.string.objective_pair)
            }
        }

        is HasThreeOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_three_of_kind)
        is ThreeOfKindWithValueCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_three_of_kind_with_value,
            formatArgs = listOf(condition.requiredValue)
        )

        is HasFourOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_four_of_kind)
        is FullHouseCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_full_house)
        is AllDistinctCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_all_distinct)
        is ExactlyDistinctValuesCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_exact_distinct_values,
            formatArgs = listOf(condition.distinctCount)
        )

        is StraightCondition -> {
            if (condition.length == 0) {
                ObjectiveLineText.StringRes(resId = R.string.objective_straight)
            } else {
                ObjectiveLineText.PluralRes(
                    resId = R.plurals.objective_straight_length,
                    quantity = condition.length,
                    formatArgs = listOf(condition.length)
                )
            }
        }

        is ContainsValuesCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_contains_values,
                formatArgs = listOf(formatValues(condition.values))
            )
        }

        is ContainsValuesWithMultiplicityCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_contains_values,
                formatArgs = listOf(formatMultiplicity(condition.values))
            )
        }

        is ForbidValuesCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_forbid_values,
                formatArgs = listOf(formatValues(condition.values))
            )
        }

        is MinSelectedDiceCondition -> {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_selected_progress,
                formatArgs = listOf(condition.minCount)
            )
        }

        is AtLeastParityCountCondition -> {
            if (condition.even) {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_at_least_even_values,
                    formatArgs = listOf(condition.minCount)
                )
            } else {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_at_least_odd_values,
                    formatArgs = listOf(condition.minCount)
                )
            }
        }
    }
}

internal fun objectiveLineExplainText(
    condition: ObjectiveCondition,
    selectedCount: Int
): ObjectiveLineText? {
    return when (condition) {
        is SumAtLeastCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_least_explain,
            formatArgs = listOf(condition.threshold)
        )

        is SumAtMostCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_at_most_explain,
            formatArgs = listOf(condition.threshold)
        )

        is SumExactCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_exact_explain,
            formatArgs = listOf(condition.target)
        )

        is SumMultipleCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_multiple_explain,
            formatArgs = listOf(condition.factor)
        )

        is SumMaxDifferenceCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_sum_max_difference_explain,
            formatArgs = listOf(condition.target, condition.maxDifference)
        )

        is SumInRangeCondition -> if (condition.min == condition.max) {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_sum_exact_explain,
                formatArgs = listOf(condition.min)
            )
        } else {
            ObjectiveLineText.StringRes(
                resId = R.string.objective_sum_in_range_explain,
                formatArgs = listOf(condition.min, condition.max)
            )
        }

        is SumParityCondition -> if (condition.shouldBeEven) {
            ObjectiveLineText.StringRes(resId = R.string.objective_sum_even_explain)
        } else {
            ObjectiveLineText.StringRes(resId = R.string.objective_sum_odd_explain)
        }

        is HasPairCondition -> if (condition.requiredPairs >= 2) {
            ObjectiveLineText.StringRes(resId = R.string.objective_two_pairs_explain)
        } else {
            ObjectiveLineText.StringRes(resId = R.string.objective_pair_explain)
        }

        is HasThreeOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_three_of_kind_explain)
        is ThreeOfKindWithValueCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_three_of_kind_with_value_explain,
            formatArgs = listOf(condition.requiredValue)
        )

        is HasFourOfKindCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_four_of_kind_explain)
        is FullHouseCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_full_house_explain)
        is AllDistinctCondition -> ObjectiveLineText.StringRes(resId = R.string.objective_all_distinct_explain)
        is ExactlyDistinctValuesCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_exact_distinct_values_explain,
            formatArgs = listOf(condition.distinctCount)
        )

        is StraightCondition -> null
        is ContainsValuesCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_contains_values_explain,
            formatArgs = listOf(formatValues(condition.values))
        )

        is ContainsValuesWithMultiplicityCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_contains_values_explain,
            formatArgs = listOf(formatMultiplicity(condition.values))
        )

        is ForbidValuesCondition -> ObjectiveLineText.StringRes(
            resId = R.string.objective_forbid_values_explain,
            formatArgs = listOf(formatValues(condition.values))
        )

        is MinSelectedDiceCondition -> null

        is AtLeastParityCountCondition -> {
            if (condition.even) {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_at_least_even_values_explain,
                    formatArgs = listOf(condition.minCount)
                )
            } else {
                ObjectiveLineText.StringRes(
                    resId = R.string.objective_at_least_odd_values_explain,
                    formatArgs = listOf(condition.minCount)
                )
            }
        }
    }
}

internal fun formatValues(values: List<Int>): String {
    return values.distinct().sorted().joinToString(", ")
}

internal fun formatMultiplicity(values: List<Int>): String {
    val counts = values.groupingBy { it }.eachCount().toSortedMap()
    return counts.entries.joinToString(", ") { (value, count) -> "${count}x$value" }
}
