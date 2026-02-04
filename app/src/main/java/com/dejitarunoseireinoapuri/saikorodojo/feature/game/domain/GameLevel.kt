package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import kotlin.math.min
import kotlin.random.Random

data class LevelDefinition(
    val levelNumber: Int,
    val diceCount: Int,
    val diceTypes: List<DiceType>
)

data class LevelObjective(
    val conditions: List<ObjectiveCondition>
)

enum class MinigameType {
    ODD_EVEN,
    SEQUENCE,
    BLACKJACK,
    HIGHER_LOWER
}

sealed interface ObjectiveCondition {
    fun isMet(diceValues: List<Int>): Boolean
}

data class SumAtLeastCondition(val threshold: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        return diceValues.sum() >= threshold
    }
}

data class SumInRangeCondition(val min: Int, val max: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val sum = diceValues.sum()
        return sum in min..max
    }
}

data class SumExactCondition(val target: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        return diceValues.sum() == target
    }
}

data class SumParityCondition(val shouldBeEven: Boolean) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val sum = diceValues.sum()
        return sum % 2 == 0 == shouldBeEven
    }
}

data class HasPairCondition(val requiredPairs: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val pairs = valueCounts(diceValues).values.count { it >= 2 }
        return pairs >= requiredPairs
    }
}

data class HasThreeOfKindCondition(val required: Boolean = true) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val hasThree = valueCounts(diceValues).values.any { it >= 3 }
        return if (required) hasThree else !hasThree
    }
}

data class HasFourOfKindCondition(val required: Boolean = true) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val hasFour = valueCounts(diceValues).values.any { it >= 4 }
        return if (required) hasFour else !hasFour
    }
}

data object FullHouseCondition : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val counts = valueCounts(diceValues).values.sortedDescending()
        return counts.size >= 2 && counts[0] >= 3 && counts[1] >= 2
    }
}

data object AllDistinctCondition : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        return diceValues.distinct().size == diceValues.size
    }
}

data class StraightCondition(val length: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        if (length <= 1) return true
        val sorted = diceValues.distinct().sorted()
        if (sorted.size < length) return false
        var streak = 1
        for (index in 1 until sorted.size) {
            if (sorted[index] == sorted[index - 1] + 1) {
                streak += 1
                if (streak >= length) return true
            } else {
                streak = 1
            }
        }
        return false
    }
}

data class ContainsValuesCondition(val values: List<Int>) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val diceSet = diceValues.toSet()
        return values.all { value -> value in diceSet }
    }
}

data class ContainsValuesWithMultiplicityCondition(val values: List<Int>) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val counts = valueCounts(diceValues)
        val requiredCounts = valueCounts(values)
        return requiredCounts.all { (value, required) ->
            counts.getOrDefault(value, 0) >= required
        }
    }
}

data class CollectionPartialCondition(
    val values: List<Int>,
    val requiredCount: Int
) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val diceSet = diceValues.toSet()
        val presentCount = values.distinct().count { it in diceSet }
        return presentCount >= requiredCount
    }
}

data class ForbidValuesCondition(val values: List<Int>) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        val diceSet = diceValues.toSet()
        return values.none { it in diceSet }
    }
}

data class MinSelectedDiceCondition(val minCount: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>): Boolean {
        return diceValues.size >= minCount
    }
}

class GenerateLevelUseCase {
    fun execute(levelNumber: Int, seedBase: Long): LevelDefinition {
        val stage = stageForLevel(levelNumber)
        val diceCount = min(MIN_DICE + DICE_INCREMENT * (stage - 1), MAX_DICE)
        val random = Random(seedBase + levelNumber)
        val diceTypes = generateDiceTypes(stage, diceCount, random)
        return LevelDefinition(
            levelNumber = levelNumber,
            diceCount = diceCount,
            diceTypes = diceTypes
        )
    }

    private fun generateDiceTypes(stage: Int, diceCount: Int, random: Random): List<DiceType> {
        if (stage == 1) {
            return List(diceCount) { DiceType.D6 }
        }
        val weights = when (stage) {
            2 -> listOf(DiceType.D6 to 0.8f, DiceType.D8 to 0.2f)
            3 -> listOf(DiceType.D6 to 0.6f, DiceType.D8 to 0.3f, DiceType.D10 to 0.1f)
            4 -> listOf(DiceType.D6 to 0.4f, DiceType.D8 to 0.35f, DiceType.D10 to 0.25f)
            else -> listOf(DiceType.D6 to 0.3f, DiceType.D8 to 0.35f, DiceType.D10 to 0.35f)
        }
        return List(diceCount) { weightedDiceType(weights, random) }
    }

    private fun weightedDiceType(
        weights: List<Pair<DiceType, Float>>,
        random: Random
    ): DiceType {
        val target = random.nextFloat()
        var total = 0f
        for ((type, weight) in weights) {
            total += weight
            if (target <= total) return type
        }
        return weights.last().first
    }

}

class GenerateObjectiveUseCase {
    fun execute(levelNumber: Int, diceValues: List<Int>, seedBase: Long): LevelObjective {
        val stage = stageForLevel(levelNumber)
        val random = Random(seedBase + levelNumber * 13L)
        val diceCounts = valueCounts(diceValues)
        val distinctValues = diceCounts.keys.sorted()
        val maxValue = distinctValues.maxOrNull() ?: 1
        val totalSum = diceValues.sum()
        val candidates = mutableListOf<ObjectiveCondition>()

        fun addIf(condition: ObjectiveCondition, predicate: Boolean) {
            if (predicate) {
                candidates.add(condition)
            }
        }

        when (stage) {
            1 -> {
                addIf(HasPairCondition(requiredPairs = 1), diceCounts.values.any { it >= 2 })
                addIf(AllDistinctCondition, diceValues.distinct().size == diceValues.size)
                val containsValues = distinctValues.shuffled(random).take(2).ifEmpty { distinctValues }
                if (containsValues.isNotEmpty()) {
                    candidates.add(ContainsValuesCondition(containsValues))
                }
                candidates.add(SumExactCondition(pickExactSumTarget(diceValues, random)))
                candidates.add(buildSumRange(diceValues, random))
                val forbidValues = buildForbiddenValues(distinctValues, maxValue, random)
                if (forbidValues.isNotEmpty() && hasValueOutside(diceValues, forbidValues)) {
                    candidates.add(ForbidValuesCondition(forbidValues))
                }
            }
            2 -> {
                addIf(HasPairCondition(requiredPairs = 2), diceCounts.values.count { it >= 2 } >= 2)
                addIf(FullHouseCondition, canFullHouse(diceCounts))
                val partialValues = distinctValues.shuffled(random).take(4).ifEmpty { distinctValues }
                if (partialValues.isNotEmpty()) {
                    candidates.add(
                        CollectionPartialCondition(
                            values = partialValues,
                            requiredCount = min(3, partialValues.size)
                        )
                    )
                }
                addIf(StraightCondition(length = 3), canFormStraight(distinctValues, 3))
                candidates.add(SumExactCondition(pickExactSumTarget(diceValues, random)))
                candidates.add(buildSumRange(diceValues, random))
            }
            3 -> {
                addIf(HasFourOfKindCondition(), diceCounts.values.any { it >= 4 })
                addIf(FullHouseCondition, canFullHouse(diceCounts))
                addIf(StraightCondition(length = 4), canFormStraight(distinctValues, 4))
                val multiplicity = buildMultiplicityValues(diceCounts, random)
                if (multiplicity.isNotEmpty()) {
                    candidates.add(ContainsValuesWithMultiplicityCondition(multiplicity))
                }
                candidates.add(SumExactCondition(pickExactSumTarget(diceValues, random)))
                candidates.add(buildSumRange(diceValues, random))
            }
            4 -> {
                candidates.add(SumParityCondition(shouldBeEven = totalSum % 2 == 0))
                candidates.add(SumAtLeastCondition(threshold = totalSum))
                candidates.add(buildSumRange(diceValues, random))
            }
            else -> {
                addIf(HasPairCondition(requiredPairs = 2), diceCounts.values.count { it >= 2 } >= 2)
                addIf(StraightCondition(length = 4), canFormStraight(distinctValues, 4))
                candidates.add(SumExactCondition(pickExactSumTarget(diceValues, random)))
                candidates.add(buildSumRange(diceValues, random))
                candidates.add(SumAtLeastCondition(threshold = totalSum))
                val forbidValues = buildForbiddenValues(distinctValues, maxValue, random)
                if (forbidValues.isNotEmpty() && hasValueOutside(diceValues, forbidValues)) {
                    candidates.add(ForbidValuesCondition(forbidValues))
                }
            }
        }

        if (candidates.isEmpty()) {
            return LevelObjective(conditions = listOf(SumExactCondition(totalSum)))
        }

        val selectedConditions = if (stage == 4 || stage >= 5) {
            val first = candidates.random(random)
            val second = candidates.filterNot { it == first }.ifEmpty { candidates }.random(random)
            listOf(first, second).distinct()
        } else {
            listOf(candidates.random(random))
        }
        val enrichedConditions = selectedConditions.toMutableList()
        val forbidCondition = selectedConditions.filterIsInstance<ForbidValuesCondition>().firstOrNull()
        if (forbidCondition != null) {
            val minimumCount = minimumSelectionCountForForbidden(
                diceValues = diceValues,
                forbiddenValues = forbidCondition.values,
                stage = stage
            )
            enrichedConditions.add(MinSelectedDiceCondition(minimumCount))
        }
        return LevelObjective(conditions = enrichedConditions.distinct())
    }
}

fun stageForLevel(levelNumber: Int): Int {
    return ((levelNumber - 1) / LEVELS_PER_STAGE) + 1
}

private const val LEVELS_PER_STAGE = 15
private const val MIN_DICE = 5
private const val DICE_INCREMENT = 3
private const val MAX_DICE = 20

private fun canFormStraight(values: List<Int>, length: Int): Boolean {
    if (values.size < length) return false
    var streak = 1
    for (index in 1 until values.size) {
        if (values[index] == values[index - 1] + 1) {
            streak += 1
            if (streak >= length) return true
        } else {
            streak = 1
        }
    }
    return false
}

private fun canFullHouse(counts: Map<Int, Int>): Boolean {
    val sorted = counts.values.sortedDescending()
    return sorted.size >= 2 && sorted[0] >= 3 && sorted[1] >= 2
}

private fun buildMultiplicityValues(counts: Map<Int, Int>, random: Random): List<Int> {
    val pairValue = counts.entries.firstOrNull { it.value >= 2 }?.key ?: return emptyList()
    val otherValue = counts.keys.filterNot { it == pairValue }.ifEmpty { listOf(pairValue) }
        .random(random)
    return listOf(pairValue, pairValue, otherValue)
}

private fun pickExactSumTarget(diceValues: List<Int>, random: Random): Int {
    val candidates = mutableListOf<Int>()
    candidates.addAll(diceValues)
    if (diceValues.size >= 2) {
        val shuffled = diceValues.shuffled(random)
        candidates.add(shuffled[0] + shuffled[1])
    }
    return candidates.random(random)
}

private fun buildSumRange(diceValues: List<Int>, random: Random): SumInRangeCondition {
    val totalSum = diceValues.sum()
    val minSum = diceValues.minOrNull() ?: 1
    val maxSum = totalSum
    val spread = maxOf(2, (maxSum - minSum) / 2)
    val base = (totalSum - spread).coerceAtLeast(minSum)
    val top = (totalSum + spread).coerceAtMost(maxSum)
    val start = minOf(base, top - 1)
    val end = maxOf(start + 1, top)
    return SumInRangeCondition(min = start, max = end)
}

private fun buildForbiddenValues(values: List<Int>, maxValue: Int, random: Random): List<Int> {
    val candidates = (1..maxValue).filterNot { it in values }
    return if (candidates.isNotEmpty()) {
        listOf(candidates.random(random))
    } else {
        values.shuffled(random).take(1)
    }
}

private fun hasValueOutside(diceValues: List<Int>, forbidden: List<Int>): Boolean {
    return diceValues.any { it !in forbidden }
}

private fun valueCounts(values: List<Int>): Map<Int, Int> {
    return values.groupingBy { it }.eachCount()
}

internal fun minimumSelectionCountForForbidden(
    diceValues: List<Int>,
    forbiddenValues: List<Int>,
    stage: Int
): Int {
    val forbiddenCount = diceValues.count { it in forbiddenValues }
    val nonForbiddenCount = (diceValues.size - forbiddenCount).coerceAtLeast(0)
    return if (stage >= 4 && forbiddenCount == 0) {
        diceValues.size
    } else {
        nonForbiddenCount.coerceAtLeast(1)
    }
}
