package com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RollDiceUseCaseTest {
    @Test
    fun `execute returns dice values from random provider`() {
        val useCase = RollDiceUseCase(FixedRandomProvider(4))

        val result = useCase.execute(5)

        assertEquals(List(5) { 4 }, result)
    }
}

private class FixedRandomProvider(private val value: Int) : DiceRandomProvider {
    override fun nextInt(from: Int, until: Int): Int {
        return value
    }
}
