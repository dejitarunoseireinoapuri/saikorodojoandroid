package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceRollMotionTest {
    @Test
    fun `playback uses the same duration as each game and has no extra settling phase`() {
        assertEquals(1500L, calculateRollPlaybackMs(1500L, 150L))
        assertEquals(1440L, calculateRollPlaybackMs(1500L, 120L))
        assertEquals(1320L, calculateRollPlaybackMs(1500L, 120L, omitLastDelay = true))
        assertEquals(0L, calculateRollPlaybackMs(0L, 120L))
        assertEquals(0L, calculateRollPlaybackMs(1L, 120L, omitLastDelay = true))
    }

    @Test
    fun `dice settle exactly on their reserved positions`() {
        assertEquals(0f, diceTravelFraction(0f), 0f)
        assertEquals(0f, diceTravelFraction(1f), 0f)
        assertEquals(0f, diceBounceHeight(1f), 0f)
    }

    @Test
    fun `travel stays between the reserved position and its bounded waypoint`() {
        for (step in -10..110) {
            val fraction = diceTravelFraction(step / 100f)
            assertTrue(fraction in 0f..1f)
            assertTrue(diceBounceHeight(step / 100f) in 0f..0.161f)
        }
    }

    @Test
    fun `impacts touch the mat and successive bounces lose height`() {
        assertEquals(0f, diceBounceHeight(0.44f), 0.00001f)
        assertEquals(0f, diceBounceHeight(0.74f), 0.00001f)
        assertEquals(0f, diceBounceHeight(0.90f), 0.00001f)
        assertTrue(diceBounceHeight(0.22f) > diceBounceHeight(0.59f))
        assertTrue(diceBounceHeight(0.59f) > diceBounceHeight(0.82f))
    }
}
