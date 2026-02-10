package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RunDiceRollTurnUseCaseTest {
    private val useCase = RunDiceRollTurnUseCase()

    @Test
    fun `prepare creates plan for selected indices`() {
        val plan = useCase.prepare(
            durationMs = 1500L,
            tickMs = 150L,
            diceTypes = listOf(DiceType.D6, DiceType.D8, DiceType.D10),
            selectedIndices = listOf(0, 2)
        )

        assertEquals(10, plan.steps)
        assertEquals(listOf(0, 2), plan.selectedIndices)
        assertEquals(listOf(DiceType.D6, DiceType.D10), plan.selectedDiceTypes)
    }

    @Test
    fun `applyValues replaces only selected dice indices`() {
        val updated = useCase.applyValues(
            currentValues = listOf(1, 2, 3, 4),
            selectedIndices = listOf(1, 3),
            rolledValues = listOf(6, 5)
        )

        assertEquals(listOf(1, 6, 3, 5), updated)
    }
}
