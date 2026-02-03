package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

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
}
