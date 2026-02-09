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
    fun isMet(diceValues: List<Int>, diceSides: List<Int> = emptyList()): Boolean
}

data class SumAtLeastCondition(val threshold: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.sum() >= threshold
    }
}

data class SumAtMostCondition(val threshold: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.sum() <= threshold
    }
}

data class SumInRangeCondition(val min: Int, val max: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val sum = diceValues.sum()
        return sum in min..max
    }
}

data class SumExactCondition(val target: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.sum() == target
    }
}

data class SumMultipleCondition(val factor: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        if (factor <= 0) return false
        return diceValues.sum() % factor == 0
    }
}

data class SumMaxDifferenceCondition(
    val target: Int,
    val maxDifference: Int
) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return kotlin.math.abs(diceValues.sum() - target) <= maxDifference
    }
}

data class SumParityCondition(val shouldBeEven: Boolean) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val sum = diceValues.sum()
        return sum % 2 == 0 == shouldBeEven
    }
}

data class HasPairCondition(val requiredPairs: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val pairs = valueCounts(diceValues).values.count { it >= 2 }
        return pairs >= requiredPairs
    }
}

data class HasThreeOfKindCondition(val required: Boolean = true) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val hasThree = valueCounts(diceValues).values.any { it >= 3 }
        return if (required) hasThree else !hasThree
    }
}

data class ThreeOfKindWithValueCondition(val requiredValue: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return valueCounts(diceValues).getOrDefault(requiredValue, 0) >= 3
    }
}

data class HasFourOfKindCondition(val required: Boolean = true) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val hasFour = valueCounts(diceValues).values.any { it >= 4 }
        return if (required) hasFour else !hasFour
    }
}

data object FullHouseCondition : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val counts = valueCounts(diceValues).values.sortedDescending()
        return counts.size >= 2 && counts[0] >= 3 && counts[1] >= 2
    }
}

data object AllDistinctCondition : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.distinct().size == diceValues.size
    }
}

data class ExactlyDistinctValuesCondition(val distinctCount: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.distinct().size == distinctCount
    }
}

data class StraightCondition(val length: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
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
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val diceSet = diceValues.toSet()
        return values.all { value -> value in diceSet }
    }
}

data class ContainsValuesWithMultiplicityCondition(val values: List<Int>) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val counts = valueCounts(diceValues)
        val requiredCounts = valueCounts(values)
        return requiredCounts.all { (value, required) ->
            counts.getOrDefault(value, 0) >= required
        }
    }
}

data class ForbidValuesCondition(val values: List<Int>) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val diceSet = diceValues.toSet()
        return values.none { it in diceSet }
    }
}

data class MinSelectedDiceCondition(val minCount: Int) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        return diceValues.size >= minCount
    }
}

data class AtLeastParityCountCondition(
    val minCount: Int,
    val even: Boolean
) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val parityCount = diceValues.count { value -> (value % 2 == 0) == even }
        return parityCount >= minCount
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
        val maximumPossibleSum = diceCount * maxDieValue
        val randomValuesPool = List(maxDieValue) { it + 1 }
        val sumDifficultyFactor = (stage - 1).coerceAtLeast(0)
        val exactTarget = buildRandomExactTarget(
            minimumPossibleSum = minSelectable,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )
        val atLeastThreshold = buildRandomAtLeastThreshold(
            minimumPossibleSum = minSelectable,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )
        val rangeCondition = buildRandomRangeCondition(
            minimumPossibleSum = minSelectable,
            maximumPossibleSum = maximumPossibleSum,
            sumDifficultyFactor = sumDifficultyFactor,
            random = random
        )
        val candidates = buildObjectiveCandidates(
            stage = stage,
            maxSelectable = diceCount,
            maxDieValue = maxDieValue,
            randomValuesPool = randomValuesPool,
            exactTarget = exactTarget,
            atLeastThreshold = atLeastThreshold,
            rangeCondition = rangeCondition,
            random = random
        )

        val selectedConditionsCount = when {
            stage <= 2 -> 1
            stage <= 4 -> 2
            else -> 3
        }
        val primaryCondition = selectPrimaryCondition(candidates, random)
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

internal fun selectPrimaryCondition(
    candidates: List<ObjectiveCondition>,
    random: Random
): ObjectiveCondition {
    return candidates.random(random)
}

internal fun buildObjectiveCandidates(
    stage: Int,
    maxSelectable: Int,
    maxDieValue: Int,
    randomValuesPool: List<Int>,
    exactTarget: Int,
    atLeastThreshold: Int,
    rangeCondition: SumInRangeCondition,
    random: Random
): List<ObjectiveCondition> {
    val minStraightLength = minOf(3, maxDieValue)
    val maxStraightLength = minOf(maxDieValue, maxSelectable)

    val candidates = mutableListOf<ObjectiveCondition>()
    candidates.add(SumExactCondition(target = exactTarget))
    candidates.add(SumAtLeastCondition(threshold = atLeastThreshold))
    candidates.add(SumAtMostCondition(threshold = exactTarget + random.nextInt(0, 4)))
    candidates.add(rangeCondition)
    candidates.add(SumMultipleCondition(factor = listOf(3, 5).random(random)))
    candidates.add(
        SumMaxDifferenceCondition(
            target = exactTarget,
            maxDifference = (4 - stage.coerceAtMost(3)).coerceAtLeast(1)
        )
    )
    candidates.add(SumParityCondition(shouldBeEven = random.nextBoolean()))

    if (stage >= 1) {
        candidates.add(AllDistinctCondition)
        candidates.add(HasPairCondition(requiredPairs = 1))
        candidates.add(HasPairCondition(requiredPairs = 2))
        candidates.add(ExactlyDistinctValuesCondition(distinctCount = minOf(3, maxSelectable)))
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
        candidates.add(AtLeastParityCountCondition(minCount = 2, even = random.nextBoolean()))
        candidates.add(HasThreeOfKindCondition(required = true))
    }

    if (stage >= 3) {
        candidates.add(ThreeOfKindWithValueCondition(requiredValue = randomValuesPool.random(random)))
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

    return candidates
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
