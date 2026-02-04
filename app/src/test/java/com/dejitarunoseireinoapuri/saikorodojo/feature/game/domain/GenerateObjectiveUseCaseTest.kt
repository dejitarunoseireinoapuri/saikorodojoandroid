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
    fun `minimum selection keeps at least dice count minus two`() {
        val minimum = minimumSelectionCountForLevel(
            diceCount = 6,
            stage = 2
        )

        assertEquals(4, minimum)
    }

    @Test
    fun `minimum selection requires all dice on hard stages`() {
        val minimum = minimumSelectionCountForLevel(
            diceCount = 5,
            stage = 4
        )

        assertEquals(5, minimum)
    }
}
