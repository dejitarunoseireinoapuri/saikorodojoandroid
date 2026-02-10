package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
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
            maxSelectable = 5,
            maxDieValue = 6,
            randomValuesPool = (1..6).toList(),
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
            maxSelectable = 5,
            maxDieValue = 6,
            randomValuesPool = (1..6).toList(),
            exactTarget = 10,
            atLeastThreshold = 12,
            rangeCondition = SumInRangeCondition(min = 5, max = 9),
            random = Random(2)
        )

        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 1 })
        assertTrue(candidates.any { it is HasPairCondition && it.requiredPairs == 2 })
        assertFalse(candidates.any { it is HasPairCondition && it.requiredPairs == 3 })
        assertTrue(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 1 })
        assertFalse(candidates.any { it is HasThreeOfKindCondition && it.requiredTrios == 2 })
    }

    @Test
    fun `stage three objectives include advanced pair and trio candidates`() {
        val candidates = buildObjectiveCandidates(
            stage = 3,
            maxSelectable = 8,
            maxDieValue = 8,
            randomValuesPool = (1..8).toList(),
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
            maxSelectable = 5,
            maxDieValue = 6,
            randomValuesPool = (1..6).toList(),
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
}

private class FixedIndexRandom(
    private val fixedIndex: Int
) : Random() {
    override fun nextBits(bitCount: Int): Int = fixedIndex

    override fun nextInt(until: Int): Int = fixedIndex
}
