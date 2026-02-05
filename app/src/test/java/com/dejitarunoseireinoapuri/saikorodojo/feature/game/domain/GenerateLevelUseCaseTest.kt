package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateLevelUseCaseTest {
    private val useCase = GenerateLevelUseCase()

    @Test
    fun `level one uses five six-sided dice`() {
        val level = useCase.execute(levelNumber = 1, seedBase = 10L)

        assertEquals(5, level.diceCount)
        assertTrue(level.diceTypes.all { it == DiceType.D6 })
    }

    @Test
    fun `first stage ends at level fifteen then increases every twenty five levels`() {
        assertEquals(1, stageForLevel(15))
        assertEquals(2, stageForLevel(16))
        assertEquals(2, stageForLevel(40))
        assertEquals(3, stageForLevel(41))
    }

    @Test
    fun `dice count scales by updated stage thresholds`() {
        val stageTwo = useCase.execute(levelNumber = 16, seedBase = 10L)
        val stageThree = useCase.execute(levelNumber = 41, seedBase = 10L)
        val stageFive = useCase.execute(levelNumber = 91, seedBase = 10L)

        assertEquals(8, stageTwo.diceCount)
        assertEquals(11, stageThree.diceCount)
        assertEquals(17, stageFive.diceCount)
    }
}
