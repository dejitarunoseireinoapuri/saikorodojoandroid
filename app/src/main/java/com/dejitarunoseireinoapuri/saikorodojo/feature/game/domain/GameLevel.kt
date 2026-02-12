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
        val pairs = valueCounts(diceValues).values.sumOf { it / 2 }
        return pairs >= requiredPairs
    }
}

data class HasThreeOfKindCondition(val requiredTrios: Int = 1) : ObjectiveCondition {
    override fun isMet(diceValues: List<Int>, diceSides: List<Int>): Boolean {
        val trios = valueCounts(diceValues).values.sumOf { it / 3 }
        return trios >= requiredTrios
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
        val valueSupportCounts = buildValueSupportCounts(diceTypes)
        val supportedValues = valueSupportCounts.keys.sorted()
        val minSelectable = minimumSelectionCountForLevel(
            diceCount = diceCount,
            stage = stage
        ).coerceIn(1, diceCount)
        val minimumPossibleSum = minSelectable
        val maximumPossibleSum = maximumPossibleSum(diceTypes)
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
        val candidates = buildObjectiveCandidates(
            stage = stage,
            diceTypes = diceTypes,
            minSelectable = minSelectable,
            maxSelectable = diceCount,
            maxDieValue = maxDieValue,
            supportedValues = supportedValues,
            valueSupportCounts = valueSupportCounts,
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
        val minimumSelectionCount = minimumSelectionCountForLevel(
            diceCount = diceCount,
            stage = stage
        )
        repeat(OBJECTIVE_GENERATION_MAX_ATTEMPTS) {
            val primaryCondition = selectPrimaryCondition(candidates, random)
            val selectedConditions = buildList {
                add(primaryCondition)
                candidates
                    .filterNot { it == primaryCondition }
                    .shuffled(random)
                    .forEach { condition ->
                        if (size < selectedConditionsCount && areConditionsCompatible(this, condition, diceTypes)) {
                            add(condition)
                        }
                    }
            }

            val enrichedConditions = selectedConditions.toMutableList().apply {
                add(MinSelectedDiceCondition(minimumSelectionCount))
            }.distinct()
            if (isObjectiveSetSatisfiable(enrichedConditions, diceTypes)) {
                return LevelObjective(conditions = enrichedConditions)
            }
        }
        return LevelObjective(
            conditions = buildDeterministicFallbackObjective(
                minimumSelectionCount = minimumSelectionCount,
                diceCount = diceCount
            )
        )
    }
}

private fun buildDeterministicFallbackObjective(
    minimumSelectionCount: Int,
    diceCount: Int
): List<ObjectiveCondition> {
    val safeSelection = minimumSelectionCount.coerceIn(1, diceCount.coerceAtLeast(1))
    return listOf(
        SumExactCondition(target = safeSelection),
        MinSelectedDiceCondition(minCount = safeSelection)
    )
}

internal fun selectPrimaryCondition(
    candidates: List<ObjectiveCondition>,
    random: Random
): ObjectiveCondition {
    return candidates.random(random)
}

internal fun buildObjectiveCandidates(
    stage: Int,
    diceTypes: List<DiceType>,
    minSelectable: Int,
    maxSelectable: Int,
    maxDieValue: Int,
    supportedValues: List<Int>,
    valueSupportCounts: Map<Int, Int>,
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
        if (minSelectable <= supportedValues.size) {
            candidates.add(AllDistinctCondition)
        }
        candidates.add(HasPairCondition(requiredPairs = 1))
        candidates.add(HasPairCondition(requiredPairs = 2))
        candidates.add(ExactlyDistinctValuesCondition(distinctCount = minOf(3, maxSelectable)))
        val containsValues = supportedValues.shuffled(random).take(
            minOf(2 + stage / 2, maxSelectable, supportedValues.size).coerceAtLeast(1)
        )
        if (containsValues.isNotEmpty()) {
            candidates.add(
                ContainsValuesCondition(
                    values = containsValues
                )
            )
        }
    }

    if (stage >= 2 && maxDieValue >= 3) {
        val straightLength = random.nextInt(
            from = minStraightLength,
            until = (maxStraightLength + 1).coerceAtLeast(minStraightLength + 1)
        )
        if (isStraightFeasible(diceTypes = diceTypes, length = straightLength, forbiddenValues = emptySet())) {
            candidates.add(StraightCondition(length = straightLength))
        }
        candidates.add(
            AtLeastParityCountCondition(
                minCount = minimumParityCountForObjective(maxSelectable),
                even = random.nextBoolean()
            )
        )
        candidates.add(HasThreeOfKindCondition(requiredTrios = 1))
    }

    if (stage >= 3) {
        candidates.add(HasPairCondition(requiredPairs = 3))
        candidates.add(HasThreeOfKindCondition(requiredTrios = 2))
        val trioEligibleValues = supportedValues.filter { value ->
            (valueSupportCounts[value] ?: 0) >= 3
        }
        if (trioEligibleValues.isNotEmpty()) {
            candidates.add(ThreeOfKindWithValueCondition(requiredValue = trioEligibleValues.random(random)))
        }
        val multiplicityTargetValues = supportedValues.filter { value ->
            (valueSupportCounts[value] ?: 0) >= 2
        }
        if (multiplicityTargetValues.isNotEmpty() && supportedValues.isNotEmpty()) {
            val multiplicityTargetValue = multiplicityTargetValues.random(random)
            val secondaryValue = supportedValues.random(random)
            val multiplicityConditionValues = listOf(
                multiplicityTargetValue,
                multiplicityTargetValue,
                secondaryValue
            )
            if (isMultiplicityConditionSupported(multiplicityConditionValues, valueSupportCounts)) {
                candidates.add(
                    ContainsValuesWithMultiplicityCondition(
                        values = multiplicityConditionValues
                    )
                )
            }
        }
    }

    if (stage >= 4) {
        candidates.add(HasFourOfKindCondition(required = true))
        candidates.add(FullHouseCondition)
        val forbiddenCount = minOf(stage - 2, supportedValues.size - 1).coerceAtLeast(1)
        val forbiddenValues = supportedValues.shuffled(random).take(forbiddenCount)
        if (forbiddenValues.isNotEmpty()) {
            candidates.add(ForbidValuesCondition(values = forbiddenValues))
        }
    }

    return candidates.filter { condition ->
        minimumRequiredDice(condition) <= maxSelectable &&
            isConditionTheoreticallyFeasible(
                condition = condition,
                diceTypes = diceTypes,
                minSelectable = minSelectable,
                maxSelectable = maxSelectable,
                valueSupportCounts = valueSupportCounts
            )
    }
}

internal fun areConditionsCompatible(
    selectedConditions: List<ObjectiveCondition>,
    candidate: ObjectiveCondition,
    diceTypes: List<DiceType> = emptyList()
): Boolean {
    if (selectedConditions.any { existing -> areConditionsIncompatible(existing, candidate, diceTypes) }) {
        return false
    }
    return areSumConditionsFeasible(selectedConditions + candidate)
}

internal fun isObjectiveSetSatisfiable(
    conditions: List<ObjectiveCondition>,
    diceTypes: List<DiceType>
): Boolean {
    if (conditions.isEmpty() || diceTypes.isEmpty()) return false
    val minSelected = conditions
        .filterIsInstance<MinSelectedDiceCondition>()
        .maxOfOrNull { it.minCount }
        ?.coerceAtLeast(1)
        ?: 1
    if (minSelected > diceTypes.size) return false
    if (!areSumConditionsFeasible(conditions)) return false

    val sortedDice = diceTypes.sortedByDescending { it.sides }
    for (selectionSize in minSelected..sortedDice.size) {
        val selectedSides = sortedDice.take(selectionSize).map { it.sides }
        if (isSelectionSatisfiable(conditions, selectedSides)) {
            return true
        }
    }
    return false
}

private fun isSelectionSatisfiable(
    conditions: List<ObjectiveCondition>,
    selectedSides: List<Int>
): Boolean {
    val sumLowerBound = resolveSumLowerBound(conditions)
    val sumUpperBound = resolveSumUpperBound(conditions)
    val requiredExactSum = conditions.filterIsInstance<SumExactCondition>().firstOrNull()?.target
    val nodeBudget = SearchBudget(maxNodes = 250_000)
    val values = IntArray(selectedSides.size)

    fun search(index: Int, currentSum: Int): Boolean {
        if (!nodeBudget.consume()) return false
        if (index == selectedSides.size) {
            val candidateValues = values.toList()
            return conditions.all { it.isMet(candidateValues) }
        }
        val remainingMin = selectedSides.size - (index + 1)
        val remainingMax = selectedSides.drop(index + 1).sum()
        for (value in 1..selectedSides[index]) {
            val nextSum = currentSum + value
            if (nextSum + remainingMin > sumUpperBound) continue
            if (nextSum + remainingMax < sumLowerBound) continue
            if (requiredExactSum != null) {
                if (nextSum + remainingMin > requiredExactSum) continue
                if (nextSum + remainingMax < requiredExactSum) continue
            }
            values[index] = value
            if (search(index + 1, nextSum)) return true
        }
        return false
    }

    return search(index = 0, currentSum = 0)
}

private data class SearchBudget(
    val maxNodes: Int,
    var nodes: Int = 0
) {
    fun consume(): Boolean {
        nodes += 1
        return nodes <= maxNodes
    }
}

private fun resolveSumLowerBound(conditions: List<ObjectiveCondition>): Int {
    var lowerBound = 0
    conditions.filterIsInstance<SumAtLeastCondition>().forEach { lowerBound = maxOf(lowerBound, it.threshold) }
    conditions.filterIsInstance<SumInRangeCondition>().forEach { lowerBound = maxOf(lowerBound, it.min) }
    conditions.filterIsInstance<SumMaxDifferenceCondition>().forEach {
        lowerBound = maxOf(lowerBound, it.target - it.maxDifference)
    }
    conditions.filterIsInstance<SumExactCondition>().firstOrNull()?.let { lowerBound = maxOf(lowerBound, it.target) }
    return lowerBound
}

private fun resolveSumUpperBound(conditions: List<ObjectiveCondition>): Int {
    var upperBound = Int.MAX_VALUE
    conditions.filterIsInstance<SumAtMostCondition>().forEach { upperBound = minOf(upperBound, it.threshold) }
    conditions.filterIsInstance<SumInRangeCondition>().forEach { upperBound = minOf(upperBound, it.max) }
    conditions.filterIsInstance<SumMaxDifferenceCondition>().forEach {
        upperBound = minOf(upperBound, it.target + it.maxDifference)
    }
    conditions.filterIsInstance<SumExactCondition>().firstOrNull()?.let { upperBound = minOf(upperBound, it.target) }
    return upperBound
}

internal fun isConditionTheoreticallyFeasible(
    condition: ObjectiveCondition,
    diceTypes: List<DiceType>,
    minSelectable: Int,
    maxSelectable: Int,
    valueSupportCounts: Map<Int, Int>
): Boolean {
    if (minSelectable > maxSelectable || maxSelectable <= 0) return false
    return when (condition) {
        is HasPairCondition -> valueSupportCounts.values.sumOf { it / 2 } >= condition.requiredPairs
        is HasThreeOfKindCondition -> valueSupportCounts.values.sumOf { it / 3 } >= condition.requiredTrios
        is ThreeOfKindWithValueCondition -> (valueSupportCounts[condition.requiredValue] ?: 0) >= 3
        is HasFourOfKindCondition -> if (condition.required) valueSupportCounts.values.any { it >= 4 } else true
        is FullHouseCondition -> {
            if (maxSelectable < 5) {
                false
            } else {
                val valuesWithAtLeastTwo = valueSupportCounts.values.count { it >= 2 }
                val valuesWithAtLeastThree = valueSupportCounts.values.count { it >= 3 }
                valuesWithAtLeastThree >= 1 && valuesWithAtLeastTwo >= 2
            }
        }
        is ExactlyDistinctValuesCondition -> condition.distinctCount in 1..maxSelectable
        is StraightCondition -> isStraightFeasible(diceTypes, condition.length, forbiddenValues = emptySet())
        is ContainsValuesCondition -> condition.values.all { (valueSupportCounts[it] ?: 0) > 0 }
        is ContainsValuesWithMultiplicityCondition -> condition.values.groupingBy { it }
            .eachCount()
            .all { (value, requiredCount) -> (valueSupportCounts[value] ?: 0) >= requiredCount }
        is ForbidValuesCondition -> {
            val forbidden = condition.values.toSet()
            val availableDice = diceTypes.count { diceType -> (1..diceType.sides).any { it !in forbidden } }
            availableDice >= minSelectable
        }
        is MinSelectedDiceCondition -> condition.minCount in 1..maxSelectable
        else -> true
    }
}

private fun areConditionsIncompatible(
    first: ObjectiveCondition,
    second: ObjectiveCondition,
    diceTypes: List<DiceType>
): Boolean {
    if (first is AllDistinctCondition && second is HasPairCondition) return true
    if (first is AllDistinctCondition && second is HasThreeOfKindCondition) return true
    if (first is AllDistinctCondition && second is HasFourOfKindCondition && second.required) return true
    if (first is AllDistinctCondition && second is FullHouseCondition) return true
    if (second is AllDistinctCondition && first is HasPairCondition) return true
    if (second is AllDistinctCondition && first is HasThreeOfKindCondition) return true
    if (second is AllDistinctCondition && first is HasFourOfKindCondition && first.required) return true
    if (second is AllDistinctCondition && first is FullHouseCondition) return true

    val firstForbidden = (first as? ForbidValuesCondition)?.values?.toSet().orEmpty()
    val secondForbidden = (second as? ForbidValuesCondition)?.values?.toSet().orEmpty()
    val firstRequired = requiredValuesForCompatibility(first)
    val secondRequired = requiredValuesForCompatibility(second)

    if (first is StraightCondition && second is ForbidValuesCondition &&
        !isStraightFeasible(diceTypes, first.length, second.values.toSet())
    ) {
        return true
    }
    if (second is StraightCondition && first is ForbidValuesCondition &&
        !isStraightFeasible(diceTypes, second.length, first.values.toSet())
    ) {
        return true
    }

    return firstForbidden.intersect(secondRequired).isNotEmpty() ||
        secondForbidden.intersect(firstRequired).isNotEmpty()
}

private fun areSumConditionsFeasible(conditions: List<ObjectiveCondition>): Boolean {
    val exactTarget = conditions.filterIsInstance<SumExactCondition>().map { it.target }.distinct()
    if (exactTarget.size > 1) return false

    var lowerBound = Int.MIN_VALUE
    var upperBound = Int.MAX_VALUE

    conditions.filterIsInstance<SumAtLeastCondition>().forEach { lowerBound = maxOf(lowerBound, it.threshold) }
    conditions.filterIsInstance<SumAtMostCondition>().forEach { upperBound = minOf(upperBound, it.threshold) }
    conditions.filterIsInstance<SumInRangeCondition>().forEach {
        lowerBound = maxOf(lowerBound, it.min)
        upperBound = minOf(upperBound, it.max)
    }
    if (lowerBound > upperBound) return false

    val exact = exactTarget.firstOrNull()
    if (exact != null) {
        if (exact !in lowerBound..upperBound) return false
        conditions.filterIsInstance<SumParityCondition>().forEach {
            if ((exact % 2 == 0) != it.shouldBeEven) return false
        }
        conditions.filterIsInstance<SumMultipleCondition>().forEach {
            if (it.factor <= 0 || exact % it.factor != 0) return false
        }
        return true
    }

    val parityConditions = conditions.filterIsInstance<SumParityCondition>().map { it.shouldBeEven }.distinct()
    if (parityConditions.size > 1) return false

    val multipleConditions = conditions.filterIsInstance<SumMultipleCondition>().map { it.factor }
    if (multipleConditions.any { it <= 0 }) return false

    return true
}

private fun isStraightFeasible(
    diceTypes: List<DiceType>,
    length: Int,
    forbiddenValues: Set<Int>
): Boolean {
    if (length <= 1) return true
    if (diceTypes.isEmpty() || diceTypes.size < length) return false
    val maxValue = diceTypes.maxOfOrNull { it.sides } ?: return false
    for (start in 1..(maxValue - length + 1)) {
        val run = (start until start + length).filterNot { it in forbiddenValues }
        if (run.size != length) continue
        if (canAssignDistinctRunValues(run, diceTypes)) return true
    }
    return false
}

private fun canAssignDistinctRunValues(run: List<Int>, diceTypes: List<DiceType>): Boolean {
    val usedDice = BooleanArray(diceTypes.size)
    fun backtrack(index: Int): Boolean {
        if (index == run.size) return true
        val requiredValue = run[index]
        for (diceIndex in diceTypes.indices) {
            if (!usedDice[diceIndex] && diceTypes[diceIndex].sides >= requiredValue) {
                usedDice[diceIndex] = true
                if (backtrack(index + 1)) return true
                usedDice[diceIndex] = false
            }
        }
        return false
    }
    return backtrack(0)
}

private fun requiredValuesForCompatibility(condition: ObjectiveCondition): Set<Int> {
    return when (condition) {
        is ContainsValuesCondition -> condition.values.toSet()
        is ThreeOfKindWithValueCondition -> setOf(condition.requiredValue)
        is ContainsValuesWithMultiplicityCondition -> condition.values.toSet()
        else -> emptySet()
    }
}

internal fun minimumRequiredDice(condition: ObjectiveCondition): Int {
    return when (condition) {
        is HasPairCondition -> condition.requiredPairs * 2
        is HasThreeOfKindCondition -> condition.requiredTrios * 3
        is ThreeOfKindWithValueCondition -> 3
        is HasFourOfKindCondition -> if (condition.required) 4 else 0
        is FullHouseCondition -> 5
        is AllDistinctCondition -> 1
        is ExactlyDistinctValuesCondition -> condition.distinctCount
        is StraightCondition -> condition.length
        is ContainsValuesCondition -> condition.values.distinct().size
        is ContainsValuesWithMultiplicityCondition -> condition.values.size
        is ForbidValuesCondition -> 1
        is MinSelectedDiceCondition -> condition.minCount
        is AtLeastParityCountCondition -> condition.minCount
        else -> 1
    }
}

internal fun minimumParityCountForObjective(diceCount: Int): Int {
    return (diceCount / 2) + 1
}

internal fun buildValueSupportCounts(diceTypes: List<DiceType>): Map<Int, Int> {
    val supportCounts = mutableMapOf<Int, Int>()
    diceTypes.forEach { diceType ->
        for (value in 1..diceType.sides) {
            supportCounts[value] = (supportCounts[value] ?: 0) + 1
        }
    }
    return supportCounts
}

internal fun maximumPossibleSum(diceTypes: List<DiceType>): Int {
    if (diceTypes.isEmpty()) {
        return DiceType.D6.sides
    }
    return diceTypes.sumOf { it.sides }
}

private fun isMultiplicityConditionSupported(
    values: List<Int>,
    valueSupportCounts: Map<Int, Int>
): Boolean {
    return valueCounts(values).all { (value, requiredCount) ->
        (valueSupportCounts[value] ?: 0) >= requiredCount
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
private const val OBJECTIVE_GENERATION_MAX_ATTEMPTS = 40

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
