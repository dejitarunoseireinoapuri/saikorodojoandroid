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
    fun `dice count scales by stage`() {
        val stageTwo = useCase.execute(levelNumber = 16, seedBase = 10L)
        val stageThree = useCase.execute(levelNumber = 31, seedBase = 10L)
        val stageFive = useCase.execute(levelNumber = 61, seedBase = 10L)
        val stageSix = useCase.execute(levelNumber = 76, seedBase = 10L)

        assertEquals(8, stageTwo.diceCount)
        assertEquals(11, stageThree.diceCount)
        assertEquals(17, stageFive.diceCount)
        assertEquals(20, stageSix.diceCount)
    }

    @Test
    fun `stage four keeps dice count scaling`() {
        val level = useCase.execute(levelNumber = 46, seedBase = 22L)

        assertEquals(14, level.diceCount)
    }
}
