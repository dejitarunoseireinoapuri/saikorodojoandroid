package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

data class LevelDefinition(
    val levelNumber: Int,
    val diceCount: Int,
    val diceTypes: List<DiceType>,
    val objective: LevelObjective
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

class GenerateLevelUseCase {
    fun execute(levelNumber: Int, seedBase: Long): LevelDefinition {
        val stage = ((levelNumber - 1) / LEVELS_PER_STAGE) + 1
        val diceCount = min(MIN_DICE + DICE_INCREMENT * (stage - 1), MAX_DICE)
        val random = Random(seedBase + levelNumber)
        val diceTypes = generateDiceTypes(stage, diceCount, random)
        val objective = generateObjective(stage, diceTypes, random)
        return LevelDefinition(
            levelNumber = levelNumber,
            diceCount = diceCount,
            diceTypes = diceTypes,
            objective = objective
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

    private fun generateObjective(
        stage: Int,
        diceTypes: List<DiceType>,
        random: Random
    ): LevelObjective {
        val maxSides = diceTypes.maxOfOrNull { it.sides } ?: DiceType.D6.sides
        val maxSum = diceTypes.sumOf { it.sides }
        val minSum = diceTypes.size
        return when (stage) {
            1 -> LevelObjective(
                conditions = listOf(
                    when (random.nextInt(5)) {
                        0 -> HasPairCondition(requiredPairs = 1)
                        1 -> AllDistinctCondition
                        2 -> ContainsValuesCondition(randomValues(random, maxSides, 2, 3))
                        3 -> ForbidValuesCondition(randomValues(random, maxSides, 1, 2))
                        else -> {
                            val (rangeStart, rangeEnd) = randomRange(random, minSum, maxSum, 8)
                            SumInRangeCondition(min = rangeStart, max = rangeEnd)
                        }
                    }
                )
            )
            2 -> LevelObjective(
                conditions = listOf(
                    when (random.nextInt(4)) {
                        0 -> HasPairCondition(requiredPairs = 2)
                        1 -> FullHouseCondition
                        2 -> CollectionPartialCondition(
                            values = randomValues(random, maxSides, 4, 5),
                            requiredCount = 3
                        )
                        else -> StraightCondition(length = 3)
                    }
                )
            )
            3 -> LevelObjective(
                conditions = listOf(
                    when (random.nextInt(4)) {
                        0 -> HasFourOfKindCondition()
                        1 -> FullHouseCondition
                        2 -> StraightCondition(length = 4)
                        else -> ContainsValuesWithMultiplicityCondition(
                            values = randomValuesWithMultiplicity(random, maxSides)
                        )
                    }
                )
            )
            4 -> LevelObjective(
                conditions = listOf(
                    SumParityCondition(shouldBeEven = random.nextBoolean()),
                    SumAtLeastCondition(
                        threshold = randomAtLeastTarget(random, minSum, maxSum)
                    )
                )
            )
            else -> {
                val primaryCondition = if (random.nextBoolean()) {
                    HasPairCondition(requiredPairs = 2)
                } else {
                    StraightCondition(length = 4)
                }
                val secondaryCondition = if (random.nextBoolean()) {
                    ForbidValuesCondition(randomValues(random, maxSides, 1, 2))
                } else {
                    SumAtLeastCondition(threshold = randomAtLeastTarget(random, minSum, maxSum))
                }
                LevelObjective(conditions = listOf(primaryCondition, secondaryCondition))
            }
        }
    }

    private fun randomValues(
        random: Random,
        maxSides: Int,
        minCount: Int,
        maxCount: Int
    ): List<Int> {
        val count = random.nextInt(minCount, max(maxCount, minCount) + 1)
        return List(count) { random.nextInt(1, maxSides + 1) }
    }

    private fun randomValuesWithMultiplicity(random: Random, maxSides: Int): List<Int> {
        val first = random.nextInt(1, maxSides + 1)
        val second = random.nextInt(1, maxSides + 1)
        return listOf(first, first, second)
    }

    private fun randomRange(
        random: Random,
        minSum: Int,
        maxSum: Int,
        width: Int
    ): Pair<Int, Int> {
        val adjustedWidth = min(width, max(1, maxSum - minSum))
        val start = random.nextInt(minSum, maxSum - adjustedWidth + 1)
        val end = min(start + adjustedWidth, maxSum)
        return start to end
    }

    private fun randomAtLeastTarget(random: Random, minSum: Int, maxSum: Int): Int {
        val lowerBound = minSum + (maxSum - minSum) / 2
        return random.nextInt(lowerBound.coerceAtLeast(minSum), maxSum + 1)
    }
}

private const val LEVELS_PER_STAGE = 15
private const val MIN_DICE = 5
private const val DICE_INCREMENT = 3
private const val MAX_DICE = 20

private fun valueCounts(values: List<Int>): Map<Int, Int> {
    return values.groupingBy { it }.eachCount()
}
