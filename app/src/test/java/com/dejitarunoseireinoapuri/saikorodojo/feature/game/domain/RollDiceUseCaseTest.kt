package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RollDiceUseCaseTest {
    @Test
    fun `execute returns dice values from random provider`() {
        val useCase = RollDiceUseCase(FixedRandomProvider(4))

        val result = useCase.execute(5)

        assertEquals(List(5) { 4 }, result)
    }

    @Test
    fun `execute uses dice type max exclusive for d8`() {
        val provider = RecordingRandomProvider()
        val useCase = RollDiceUseCase(provider)

        useCase.execute(2, DiceType.D8)

        assertEquals(listOf(1, 1), provider.fromValues)
        assertEquals(listOf(9, 9), provider.untilValues)
    }

    @Test
    fun `execute uses dice type max exclusive for d10`() {
        val provider = RecordingRandomProvider()
        val useCase = RollDiceUseCase(provider)

        useCase.execute(3, DiceType.D10)

        assertEquals(listOf(1, 1, 1), provider.fromValues)
        assertEquals(listOf(11, 11, 11), provider.untilValues)
    }

    @Test
    fun `execute uses per dice type bounds`() {
        val provider = RecordingRandomProvider()
        val useCase = RollDiceUseCase(provider)

        useCase.execute(listOf(DiceType.D6, DiceType.D8, DiceType.D10))

        assertEquals(listOf(1, 1, 1), provider.fromValues)
        assertEquals(listOf(7, 9, 11), provider.untilValues)
    }

    @Test
    fun `default random provider returns values within bounds`() {
        val provider = DefaultDiceRandomProvider()

        val value = provider.nextInt(from = 1, until = 7)

        assertTrue(value in 1 until 7)
    }
}

private class FixedRandomProvider(private val value: Int) : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return value
    }
}

private class RecordingRandomProvider : DiceRandomProvider {
    val fromValues = mutableListOf<Int>()
    val untilValues = mutableListOf<Int>()

    override fun nextInt(from: Int, until: Int): Int {
        fromValues.add(from)
        untilValues.add(until)
        return from
    }
}
