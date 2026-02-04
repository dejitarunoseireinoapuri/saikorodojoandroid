package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateObjectiveUseCaseTest {
    private val useCase = GenerateObjectiveUseCase()

    @Test
    fun `generated objective is satisfied by full dice selection`() {
        val diceValues = listOf(2, 2, 5, 6, 3)

        val objective = useCase.execute(
            levelNumber = 1,
            diceValues = diceValues,
            seedBase = 4L
        )

        val isSatisfied = objective.conditions.all { it.isMet(diceValues) }

        assertTrue(isSatisfied)
    }

    @Test
    fun `range objectives always have a span`() {
        val diceValues = listOf(1, 3, 4, 6, 6)

        val objective = useCase.execute(
            levelNumber = 2,
            diceValues = diceValues,
            seedBase = 12L
        )

        val hasRange = objective.conditions.filterIsInstance<SumInRangeCondition>()

        assertTrue(hasRange.all { it.min < it.max })
    }

    @Test
    fun `minimum selection uses all non forbidden dice`() {
        val diceValues = listOf(1, 2, 2, 3, 4)
        val forbiddenValues = listOf(2)

        val minimum = minimumSelectionCountForForbidden(
            diceValues = diceValues,
            forbiddenValues = forbiddenValues,
            stage = 2
        )

        assertEquals(3, minimum)
    }

    @Test
    fun `minimum selection requires all dice on hard stages when allowed`() {
        val diceValues = listOf(1, 2, 3, 4, 5)
        val forbiddenValues = listOf(6)

        val minimum = minimumSelectionCountForForbidden(
            diceValues = diceValues,
            forbiddenValues = forbiddenValues,
            stage = 4
        )

        assertEquals(5, minimum)
    }
}
