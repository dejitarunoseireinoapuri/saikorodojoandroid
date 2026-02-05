package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateObjectiveUseCaseTest {
    private val useCase = GenerateObjectiveUseCase()

    @Test
    fun `generated objective always includes minimum selected dice condition`() {
        val objective = useCase.execute(
            levelNumber = 1,
            diceTypes = List(5) { DiceType.D6 },
            seedBase = 4L
        )

        val minimumSelectionCondition = objective.conditions.filterIsInstance<MinSelectedDiceCondition>()

        assertEquals(1, minimumSelectionCondition.size)
        assertEquals(3, minimumSelectionCondition.single().minCount)
    }

    @Test
    fun `higher stages add more objective conditions`() {
        val earlyObjective = useCase.execute(
            levelNumber = 1,
            diceTypes = List(5) { DiceType.D6 },
            seedBase = 10L
        )
        val hardObjective = useCase.execute(
            levelNumber = 70,
            diceTypes = List(8) { DiceType.D10 },
            seedBase = 10L
        )

        assertEquals(2, earlyObjective.conditions.size)
        assertEquals(3, hardObjective.conditions.filterNot { it is MinSelectedDiceCondition }.size)
    }


    @Test
    fun `sum range objective stays within achievable maximum sum`() {
        val diceTypes = listOf(DiceType.D6, DiceType.D8, DiceType.D10, DiceType.D10)
        val rangeCondition = findCondition<SumInRangeCondition>(
            levelNumber = 35,
            diceTypes = diceTypes,
            seedBaseStart = 51L
        )
        val minimumSelection = minimumSelectionCountForLevel(
            diceCount = diceTypes.size,
            stage = stageForLevel(35)
        )
        val maximumPossibleSum = diceTypes.sumOf { it.sides }

        assertTrue(rangeCondition.min >= minimumSelection)
        assertTrue(rangeCondition.max <= maximumPossibleSum)
        assertTrue(rangeCondition.min < rangeCondition.max)
    }


    @Test
    fun `sum exact target changes with different seeds`() {
        val diceTypes = List(5) { DiceType.D10 }
        val firstTarget = findCondition<SumExactCondition>(
            levelNumber = 20,
            diceTypes = diceTypes,
            seedBaseStart = 31L
        ).target
        val secondTarget = findCondition<SumExactCondition>(
            levelNumber = 20,
            diceTypes = diceTypes,
            seedBaseStart = 77L
        ).target
        val minimumSelection = minimumSelectionCountForLevel(
            diceCount = diceTypes.size,
            stage = stageForLevel(20)
        )
        val maximumPossibleSum = diceTypes.sumOf { it.sides }

        assertTrue(firstTarget in minimumSelection..maximumPossibleSum)
        assertTrue(secondTarget in minimumSelection..maximumPossibleSum)
        assertTrue(firstTarget != secondTarget)
    }

    @Test
    fun `sum at least threshold changes with different seeds`() {
        val diceTypes = List(5) { DiceType.D8 }
        val firstThreshold = findCondition<SumAtLeastCondition>(
            levelNumber = 20,
            diceTypes = diceTypes,
            seedBaseStart = 11L
        ).threshold
        val secondThreshold = findCondition<SumAtLeastCondition>(
            levelNumber = 20,
            diceTypes = diceTypes,
            seedBaseStart = 99L
        ).threshold
        val maximumPossibleSum = diceTypes.sumOf { it.sides }

        assertTrue(firstThreshold in 1..maximumPossibleSum)
        assertTrue(secondThreshold in 1..maximumPossibleSum)
        assertTrue(firstThreshold != secondThreshold)
    }

    @Test
    fun `sum at least threshold grows with stage`() {
        val earlyThreshold = findCondition<SumAtLeastCondition>(
            levelNumber = 1,
            diceTypes = List(5) { DiceType.D6 },
            seedBaseStart = 22L
        ).threshold
        val hardThreshold = findCondition<SumAtLeastCondition>(
            levelNumber = 70,
            diceTypes = List(5) { DiceType.D6 },
            seedBaseStart = 22L
        ).threshold

        assertTrue(hardThreshold > earlyThreshold)
    }

    private inline fun <reified T : ObjectiveCondition> findCondition(
        levelNumber: Int,
        diceTypes: List<DiceType>,
        seedBaseStart: Long
    ): T {
        repeat(200) { attempt ->
            val objective = useCase.execute(
                levelNumber = levelNumber,
                diceTypes = diceTypes,
                seedBase = seedBaseStart + attempt
            )
            objective.conditions.filterIsInstance<T>().firstOrNull()?.let { return it }
        }
        throw AssertionError("Condition ${T::class.simpleName} was not generated")
    }
}
