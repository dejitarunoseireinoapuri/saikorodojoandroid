package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.min
import kotlin.random.Random

class GenerateObjectiveUseCaseTest {
    @Test
    fun `generated objectives always require a minimum dice selection`() {
        val useCase = GenerateObjectiveUseCase()
        val diceTypes = List(5) { DiceType.D6 }
        val levels = listOf(1, 3, 5, 7)

        levels.forEach { level ->
            val objective = useCase.execute(levelNumber = level, diceTypes = diceTypes, seedBase = 42L)

            assertTrue(objective.conditions.any { it is MinSelectedDiceCondition })
        }
    }

    @Test
    fun `stage one objectives include pair and double pair candidates only`() {
        val candidates = buildObjectiveCandidates(
            stage = 1,
            diceTypes = List(5) { DiceType.D6 },
            minSelectable = 3,
            maxSelectable = 5,
            maxDieValue = 6,
            supportedValues = (1..6).toList(),
            valueSupportCounts = (1..6).associateWith { 5 },
            exactTarget = 10,
            atLeastThreshold = 12,
            rangeCondition = SumInRangeCondition(min = 5, max = 9),
            random = Random(1)
        )

        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 1 })
        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 2 })
        assertFalse(candidates.any { it is HasPairCondition && it.requiredPairs == 3 })
    }

    @Test
    fun `stage two objectives include pair and one trio candidates only`() {
        val candidates = buildObjectiveCandidates(
            stage = 2,
            diceTypes = List(5) { DiceType.D6 },
            minSelectable = 3,
            maxSelectable = 5,
            maxDieValue = 6,
            supportedValues = (1..6).toList(),
            valueSupportCounts = (1..6).associateWith { 5 },
            exactTarget = 10,
            atLeastThreshold = 12,
            rangeCondition = SumInRangeCondition(min = 5, max = 9),
            random = Random(2)
        )

        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 1 })
        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 2 })
        assertFalse(candidates.any { it is HasPairCondition && it.requiredPairs == 3 })
        assertTrue(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 1 })
        assertTrue(
            candidates.any {
                it is AtLeastParityCountCondition && it.minCount == minimumParityCountForObjective(5)
            }
        )
        assertFalse(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 2 })
    }

    @Test
    fun `minimum parity count is at least half dice plus one`() {
        assertEquals(3, minimumParityCountForObjective(diceCount = 5))
        assertEquals(4, minimumParityCountForObjective(diceCount = 6))
        assertEquals(5, minimumParityCountForObjective(diceCount = 8))
    }

    @Test
    fun `stage three objectives include advanced pair and trio candidates`() {
        val candidates = buildObjectiveCandidates(
            stage = 3,
            diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D8, DiceType.D8, DiceType.D10, DiceType.D10, DiceType.D10),
            minSelectable = 6,
            maxSelectable = 8,
            maxDieValue = 8,
            supportedValues = (1..8).toList(),
            valueSupportCounts = (1..8).associateWith { 8 },
            exactTarget = 18,
            atLeastThreshold = 21,
            rangeCondition = SumInRangeCondition(min = 10, max = 16),
            random = Random(3)
        )

        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 3 })
        assertTrue(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 2 })
    }

    @Test
    fun `candidates exclude conditions requiring more dice than available`() {
        val candidates = buildObjectiveCandidates(
            stage = 3,
            diceTypes = List(5) { DiceType.D6 },
            minSelectable = 3,
            maxSelectable = 5,
            maxDieValue = 6,
            supportedValues = (1..6).toList(),
            valueSupportCounts = (1..6).associateWith { 5 },
            exactTarget = 10,
            atLeastThreshold = 12,
            rangeCondition = SumInRangeCondition(min = 5, max = 9),
            random = Random(4)
        )

        assertFalse(candidates.any { minimumRequiredDice(it) > 5 })
        assertFalse(candidates.any { it is HasPairCondition && it.requiredPairs == 3 })
        assertFalse(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 2 })
    }

    @Test
    fun `primary objective can be a non-sum condition`() {
        val candidates = listOf(
            SumExactCondition(target = 10),
            HasPairCondition(requiredPairs = 1),
            SumInRangeCondition(min = 4, max = 8)
        )

        val selected = selectPrimaryCondition(
            candidates = candidates,
            random = FixedIndexRandom(fixedIndex = 1)
        )

        assertTrue(selected is HasPairCondition)
    }

    @Test
    fun `maximum possible sum uses real sides per die`() {
        val sum = maximumPossibleSum(listOf(DiceType.D6, DiceType.D6, DiceType.D10))

        assertEquals(22, sum)
    }

    @Test
    fun `build value support counts reflects mixed dice capabilities`() {
        val supportCounts = buildValueSupportCounts(listOf(DiceType.D6, DiceType.D6, DiceType.D10))

        assertEquals(3, supportCounts[1])
        assertEquals(3, supportCounts[6])
        assertEquals(1, supportCounts[10])
    }

    @Test
    fun `all distinct is excluded when minimum selection exceeds distinct values`() {
        val candidates = buildObjectiveCandidates(
            stage = 4,
            diceTypes = List(12) { DiceType.D10 },
            minSelectable = 12,
            maxSelectable = 12,
            maxDieValue = 10,
            supportedValues = (1..10).toList(),
            valueSupportCounts = (1..10).associateWith { 12 },
            exactTarget = 20,
            atLeastThreshold = 24,
            rangeCondition = SumInRangeCondition(min = 15, max = 25),
            random = Random(9)
        )

        assertFalse(candidates.any { it is AllDistinctCondition })
    }

    @Test
    fun `value based candidates use only supported values and multiplicity`() {
        val supportedValues = (1..10).toList()
        val supportCounts = buildMap {
            put(1, 3)
            put(2, 3)
            put(3, 3)
            put(4, 3)
            put(5, 3)
            put(6, 3)
            put(7, 1)
            put(8, 1)
            put(9, 1)
            put(10, 1)
        }
        val candidates = buildObjectiveCandidates(
            stage = 3,
            diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D10, DiceType.D10),
            minSelectable = 8,
            maxSelectable = 8,
            maxDieValue = 10,
            supportedValues = supportedValues,
            valueSupportCounts = supportCounts,
            exactTarget = 18,
            atLeastThreshold = 20,
            rangeCondition = SumInRangeCondition(min = 12, max = 18),
            random = Random(10)
        )

        val trio = candidates.filterIsInstance<ThreeOfKindWithValueCondition>().single()
        assertTrue(trio.requiredValue in 1..6)

        val multiplicity = candidates.filterIsInstance<ContainsValuesWithMultiplicityCondition>().single()
        val multiplicityCounts = multiplicity.values.groupingBy { it }.eachCount()
        multiplicityCounts.forEach { (value, requiredCount) ->
            assertTrue(requiredCount <= (supportCounts[value] ?: 0))
        }
    }

    @Test
    fun `compatibility check rejects contradictory conditions`() {
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(AllDistinctCondition),
                candidate = HasPairCondition(requiredPairs = 1)
            )
        )
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(ForbidValuesCondition(values = listOf(4))),
                candidate = ContainsValuesCondition(values = listOf(4, 5))
            )
        )
        assertTrue(
            areConditionsCompatible(
                selectedConditions = listOf(ContainsValuesCondition(values = listOf(2, 3))),
                candidate = ForbidValuesCondition(values = listOf(6))
            )
        )
    }



    @Test
    fun `compatibility rejects opposite parity for exact sum`() {
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(SumExactCondition(target = 9)),
                candidate = SumParityCondition(shouldBeEven = true)
            )
        )
    }

    @Test
    fun `compatibility rejects non multiple exact sum`() {
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(SumExactCondition(target = 10)),
                candidate = SumMultipleCondition(factor = 3)
            )
        )
    }

    @Test
    fun `compatibility rejects impossible sum bounds`() {
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(SumAtLeastCondition(threshold = 20), SumInRangeCondition(min = 1, max = 10)),
                candidate = SumAtMostCondition(threshold = 12)
            )
        )
    }

    @Test
    fun `compatibility rejects straight blocked by forbidden values`() {
        assertFalse(
            areConditionsCompatible(
                selectedConditions = listOf(StraightCondition(length = 4)),
                candidate = ForbidValuesCondition(values = listOf(1, 2, 3, 4, 5, 6)),
                diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D6)
            )
        )
    }

    @Test
    fun `global satisfiability validates objective with min selected dice`() {
        val satisfiable = isObjectiveSetSatisfiable(
            conditions = listOf(
                SumExactCondition(target = 9),
                HasPairCondition(requiredPairs = 1),
                MinSelectedDiceCondition(minCount = 3)
            ),
            diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D6)
        )

        assertTrue(satisfiable)
    }

    @Test
    fun `global satisfiability rejects impossible objective`() {
        val satisfiable = isObjectiveSetSatisfiable(
            conditions = listOf(
                SumExactCondition(target = 5),
                ContainsValuesCondition(values = listOf(6)),
                MinSelectedDiceCondition(minCount = 3)
            ),
            diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6)
        )

        assertFalse(satisfiable)
    }

    @Test
    fun `candidate pre feasibility rejects impossible four of a kind on mixed dice`() {
        val feasible = isConditionTheoreticallyFeasible(
            condition = HasFourOfKindCondition(required = true),
            diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D8),
            minSelectable = 3,
            maxSelectable = 4,
            valueSupportCounts = buildValueSupportCounts(listOf(DiceType.D6, DiceType.D6, DiceType.D6, DiceType.D8))
        )

        assertFalse(feasible)
    }

    @Test
    fun `generated objectives are satisfiable for many levels and seeds`() {
        val objectiveUseCase = GenerateObjectiveUseCase()
        val levelUseCase = GenerateLevelUseCase()

        for (level in 1..120) {
            for (seed in 1L..150L) {
                val levelDefinition = levelUseCase.execute(levelNumber = level, seedBase = seed)
                val objective = objectiveUseCase.execute(
                    levelNumber = level,
                    diceTypes = levelDefinition.diceTypes,
                    seedBase = seed
                )

                assertTrue(
                    "Objective must be satisfiable for level=$level seed=$seed conditions=${objective.conditions}",
                    isObjectiveSetSatisfiable(objective.conditions, levelDefinition.diceTypes)
                )

                val expectedMinSelected = minimumSelectionCountForLevel(
                    diceCount = levelDefinition.diceCount,
                    stage = stageForLevel(level)
                )
                val minSelectedCondition = objective.conditions.filterIsInstance<MinSelectedDiceCondition>().single()
                assertEquals(min(expectedMinSelected, levelDefinition.diceCount), minSelectedCondition.minCount)
            }
        }
    }

}

private class FixedIndexRandom(
    private val fixedIndex: Int
) : Random() {
    override fun nextBits(bitCount: Int): Int = fixedIndex

    override fun nextInt(until: Int): Int = fixedIndex
}
