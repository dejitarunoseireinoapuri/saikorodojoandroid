package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RollSequenceUseCaseTest {
    @Test
    fun `execute uses ten-sided die range by default`() {
        var capturedRange: IntRange? = null
        val useCase = RollSequenceUseCase(
            diceRoller = { range ->
                capturedRange = range
                7
            }
        )

        val result = useCase.execute()

        assertEquals(1..10, capturedRange)
        assertEquals(7, result.value)
    }
}
