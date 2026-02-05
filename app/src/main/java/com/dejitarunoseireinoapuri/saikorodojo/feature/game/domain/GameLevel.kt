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
    fun execute(levelNumber: Int, diceTypes: List<DiceType>, seedBase: Long): LevelObjective {
        val stage = stageForLevel(levelNumber)
        val random = Random(seedBase + levelNumber * 13L)
        val diceCount = diceTypes.size.coerceAtLeast(1)
        val maxDieValue = diceTypes.maxOfOrNull { it.sides } ?: DiceType.D6.sides
        val minSelectable = minimumSelectionCountForLevel(
            diceCount = diceCount,
            stage = stage
        ).coerceIn(1, diceCount)
        val maxSelectable = diceCount
        val minimumPossibleSum = minSelectable
        val maximumPossibleSum = maxSelectable * maxDieValue
        val randomValuesPool = List(maxDieValue) { it + 1 }
        val minStraightLength = minOf(3, maxDieValue)
        val maxStraightLength = minOf(maxDieValue, maxSelectable)

        val candidates = mutableListOf<ObjectiveCondition>()
        val sumDifficultyFactor = (stage - 1).coerceAtLeast(0)

        val exactTarget = buildRandomExactTarget(
            minimumPossibleSum = minimumPossibleSum,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )
        val atLeastThreshold = buildRandomAtLeastThreshold(
            minimumPossibleSum = minimumPossibleSum,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )
        val rangeCondition = buildRandomRangeCondition(
            minimumPossibleSum = minimumPossibleSum,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )

        candidates.add(SumExactCondition(target = exactTarget))
        candidates.add(SumAtLeastCondition(threshold = atLeastThreshold))
        candidates.add(rangeCondition)
        candidates.add(SumParityCondition(shouldBeEven = random.nextBoolean()))

        if (stage >= 1) {
            candidates.add(AllDistinctCondition)
            candidates.add(HasPairCondition(requiredPairs = 1))
            val containsCount = minOf(2 + stage / 2, maxSelectable).coerceAtLeast(1)
            candidates.add(
                ContainsValuesCondition(
                    values = randomValuesPool.shuffled(random).take(containsCount)
                )
            )
        }

        if (stage >= 2 && maxDieValue >= 3) {
            val straightLength = random.nextInt(
                from = minStraightLength,
                until = (maxStraightLength + 1).coerceAtLeast(minStraightLength + 1)
            )
            candidates.add(StraightCondition(length = straightLength))
        }

        if (stage >= 3) {
            candidates.add(HasPairCondition(requiredPairs = 2))
            candidates.add(HasThreeOfKindCondition(required = true))
            val multiplicityTargetValue = randomValuesPool.random(random)
            val secondaryValue = randomValuesPool.random(random)
            candidates.add(
                ContainsValuesWithMultiplicityCondition(
                    values = listOf(
                        multiplicityTargetValue,
                        multiplicityTargetValue,
                        secondaryValue
                    )
                )
            )
        }

        if (stage >= 4) {
            candidates.add(HasFourOfKindCondition(required = true))
            candidates.add(FullHouseCondition)
            val forbiddenCount = minOf(stage - 2, maxDieValue - 1).coerceAtLeast(1)
            val forbiddenValues = randomValuesPool.shuffled(random).take(forbiddenCount)
            candidates.add(ForbidValuesCondition(values = forbiddenValues))
        }

        val selectedConditionsCount = when {
            stage <= 2 -> 1
            stage <= 4 -> 2
            else -> 3
        }
        val sumCandidates = candidates.filter {
            it is SumExactCondition || it is SumAtLeastCondition || it is SumInRangeCondition
        }
        val primaryCondition = sumCandidates.random(random)
        val selectedConditions = buildList {
            add(primaryCondition)
            addAll(
                candidates
                    .filterNot { it == primaryCondition }
                    .shuffled(random)
                    .take((selectedConditionsCount - 1).coerceAtLeast(0))
            )
        }

        val minimumSelectionCount = minimumSelectionCountForLevel(
            diceCount = diceCount,
            stage = stage
        )
        val enrichedConditions = selectedConditions.toMutableList().apply {
            add(MinSelectedDiceCondition(minimumSelectionCount))
        }
        return LevelObjective(conditions = enrichedConditions.distinct())
    }
}




private fun buildRandomExactTarget(
    minimumPossibleSum: Int,
    maximumPossibleSum: Int,
    sumDifficultyFactor: Int,
    random: Random
): Int {
    val span = (maximumPossibleSum - minimumPossibleSum).coerceAtLeast(1)
    val lowerFactor = (0.25f + 0.08f * sumDifficultyFactor).coerceAtMost(0.85f)
    val upperFactor = (lowerFactor + 0.3f).coerceAtMost(1f)
    val lowerBound = (minimumPossibleSum + span * lowerFactor)
        .toInt()
        .coerceIn(minimumPossibleSum, maximumPossibleSum)
    val upperBound = (minimumPossibleSum + span * upperFactor)
        .toInt()
        .coerceIn(lowerBound, maximumPossibleSum)
    return if (upperBound > lowerBound) {
        random.nextInt(lowerBound, upperBound + 1)
    } else {
        lowerBound
    }
}

private fun buildRandomAtLeastThreshold(
    minimumPossibleSum: Int,
    maximumPossibleSum: Int,
    sumDifficultyFactor: Int,
    random: Random
): Int {
    val span = (maximumPossibleSum - minimumPossibleSum).coerceAtLeast(1)
    val lowerFactor = (0.4f + 0.08f * sumDifficultyFactor).coerceAtMost(0.9f)
    val upperFactor = (lowerFactor + 0.25f).coerceAtMost(1f)
    val lowerBound = (minimumPossibleSum + span * lowerFactor)
        .toInt()
        .coerceIn(minimumPossibleSum, maximumPossibleSum)
    val upperBound = (minimumPossibleSum + span * upperFactor)
        .toInt()
        .coerceIn(lowerBound, maximumPossibleSum)
    return if (upperBound > lowerBound) {
        random.nextInt(lowerBound, upperBound + 1)
    } else {
        lowerBound
    }
}

private fun buildRandomRangeCondition(
    minimumPossibleSum: Int,
    maximumPossibleSum: Int,
    sumDifficultyFactor: Int,
    random: Random
): SumInRangeCondition {
    val span = (maximumPossibleSum - minimumPossibleSum).coerceAtLeast(1)
    val minRangeWidth = 2
    val maxRangeWidth = (span / (2 + sumDifficultyFactor)).coerceAtLeast(minRangeWidth)
    val rangeWidth = random.nextInt(
        from = minRangeWidth,
        until = (maxRangeWidth + 1).coerceAtLeast(minRangeWidth + 1)
    )
    val maxStart = (maximumPossibleSum - rangeWidth).coerceAtLeast(minimumPossibleSum)
    val start = if (maxStart > minimumPossibleSum) {
        random.nextInt(minimumPossibleSum, maxStart + 1)
    } else {
        minimumPossibleSum
    }
    val end = (start + rangeWidth).coerceAtMost(maximumPossibleSum)
    return SumInRangeCondition(min = start, max = end)
}

fun stageForLevel(levelNumber: Int): Int {
    if (levelNumber <= FIRST_STAGE_END_LEVEL) {
        return 1
    }
    return ((levelNumber - (FIRST_STAGE_END_LEVEL + 1)) / LEVELS_PER_STAGE_AFTER_FIRST) + 2
}

private const val FIRST_STAGE_END_LEVEL = 15
private const val LEVELS_PER_STAGE_AFTER_FIRST = 25
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

private fun pickExactSumTarget(
    diceValues: List<Int>,
    minimumSelectionCount: Int,
    random: Random
): Int {
    val candidates = possibleSumsAtLeastCount(diceValues, minimumSelectionCount)
    return candidates.random(random)
}

private fun buildSumRange(
    diceValues: List<Int>,
    minimumSelectionCount: Int,
    random: Random
): SumInRangeCondition {
    val totalSum = diceValues.sum()
    val minSum = minimumSelectionSum(diceValues, minimumSelectionCount)
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

internal fun minimumSelectionCountForLevel(
    diceCount: Int,
    stage: Int
): Int {
    val baseMinimum = (diceCount - 2).coerceAtLeast(1)
    return if (stage >= 4) {
        diceCount.coerceAtLeast(baseMinimum)
    } else {
        baseMinimum
    }
}

private fun minimumSelectionSum(
    diceValues: List<Int>,
    minimumSelectionCount: Int
): Int {
    val sorted = diceValues.sorted()
    return sorted.take(minimumSelectionCount.coerceAtMost(sorted.size)).sum()
}

internal fun possibleSumsAtLeastCount(
    diceValues: List<Int>,
    minimumSelectionCount: Int
): List<Int> {
    val maxSum = diceValues.sum()
    val maxCount = diceValues.size
    val dp = Array(maxCount + 1) { BooleanArray(maxSum + 1) }
    dp[0][0] = true
    diceValues.forEach { value ->
        for (count in maxCount downTo 1) {
            for (sum in maxSum downTo value) {
                if (dp[count - 1][sum - value]) {
                    dp[count][sum] = true
                }
            }
        }
    }
    val sums = mutableSetOf<Int>()
    for (count in minimumSelectionCount..maxCount) {
        for (sum in 0..maxSum) {
            if (dp[count][sum]) {
                sums.add(sum)
            }
        }
    }
    return sums.ifEmpty { setOf(maxSum) }.toList()
}
