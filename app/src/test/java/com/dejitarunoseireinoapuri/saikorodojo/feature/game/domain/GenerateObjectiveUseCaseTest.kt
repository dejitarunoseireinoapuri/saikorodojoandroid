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
        val objective = useCase.execute(
            levelNumber = 35,
            diceTypes = diceTypes,
            seedBase = 51L
        )

        val rangeCondition = objective.conditions.filterIsInstance<SumInRangeCondition>().single()
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
    fun `sum at least threshold grows with stage`() {
        val earlyObjective = useCase.execute(
            levelNumber = 1,
            diceTypes = List(5) { DiceType.D6 },
            seedBase = 22L
        )
        val hardObjective = useCase.execute(
            levelNumber = 70,
            diceTypes = List(5) { DiceType.D6 },
            seedBase = 22L
        )

        val earlyThreshold = earlyObjective.conditions.filterIsInstance<SumAtLeastCondition>().single().threshold
        val hardThreshold = hardObjective.conditions.filterIsInstance<SumAtLeastCondition>().single().threshold

        assertTrue(hardThreshold > earlyThreshold)
    }
}
