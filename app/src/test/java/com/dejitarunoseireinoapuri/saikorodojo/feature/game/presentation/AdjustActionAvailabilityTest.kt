package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdjustActionAvailabilityTest {
    @Test
    fun `hides increase when value is max`() {
        val availability = adjustActionAvailability(value = 6, diceType = DiceType.D6)

        assertFalse(availability.canIncrease)
        assertTrue(availability.canDecrease)
    }

    @Test
    fun `hides decrease when value is min`() {
        val availability = adjustActionAvailability(value = 1, diceType = DiceType.D6)

        assertTrue(availability.canIncrease)
        assertFalse(availability.canDecrease)
    }

    @Test
    fun `shows both when value is between limits`() {
        val availability = adjustActionAvailability(value = 3, diceType = DiceType.D6)

        assertTrue(availability.canIncrease)
        assertTrue(availability.canDecrease)
    }
}
