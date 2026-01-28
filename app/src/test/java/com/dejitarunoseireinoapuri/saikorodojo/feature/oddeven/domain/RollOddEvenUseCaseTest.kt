package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RollOddEvenUseCaseTest {
    @Test
    fun `roll use case returns even parity`() {
        val useCase = RollOddEvenUseCase(
            diceRoller = DiceRoller { 4 }
        )

        val result = useCase.execute()

        assertEquals(4, result.value)
        assertTrue(result.isEven)
    }

    @Test
    fun `roll use case returns odd parity`() {
        val useCase = RollOddEvenUseCase(
            diceRoller = DiceRoller { 5 }
        )

        val result = useCase.execute()

        assertEquals(5, result.value)
        assertFalse(result.isEven)
    }
}
