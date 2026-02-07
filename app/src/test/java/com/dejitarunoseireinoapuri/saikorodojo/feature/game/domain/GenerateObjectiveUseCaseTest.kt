package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

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
            assertTrue(objective.conditions.none { it is ExactSelectedDiceCondition })
        }
    }

    @Test
    fun `stage one objectives include pair and double pair candidates`() {
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
        assertTrue(candidates.any { it is ExactTwoPairsCondition })
    }

    @Test
    fun `stage two objectives include pair and three of a kind candidates`() {
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
        assertTrue(candidates.any { it is ExactTwoPairsCondition })
        assertTrue(candidates.any { it is HasThreeOfKindCondition && it.required })
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
