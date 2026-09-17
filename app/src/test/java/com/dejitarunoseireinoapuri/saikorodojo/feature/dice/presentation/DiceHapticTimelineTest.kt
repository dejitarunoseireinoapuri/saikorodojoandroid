package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import android.os.VibrationEffect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiceHapticTimelineTest {
    @Test
    fun `roll pattern provides gentle pulses throughout the animation`() {
        val pattern = diceRollHapticPattern(durationMs = 1_500L, hasAmplitudeControl = true)

        val activeAmplitudes = pattern.amplitudes.filter { it > 0 }
        assertEquals(1_500L, pattern.timingsMs.sum())
        assertTrue(activeAmplitudes.size >= 25)
        assertTrue(activeAmplitudes.all { it in 39..72 })
        assertTrue(activeAmplitudes.first() > activeAmplitudes.last())
        assertTrue(pattern.amplitudes.drop(1).withIndex().all { (index, amplitude) ->
            if (index % 2 == 0) amplitude > 0 else amplitude == 0
        })
        assertTrue(pattern.timingsMs.all { it >= 0L })
    }

    @Test
    fun `pattern remains valid for short rolls and motors without amplitude control`() {
        val pattern = diceRollHapticPattern(durationMs = 1L, hasAmplitudeControl = false)

        assertEquals(pattern.timingsMs.size, pattern.amplitudes.size)
        assertEquals(300L, pattern.timingsMs.sum())
        assertTrue(pattern.timingsMs.all { it >= 0L })
        assertTrue(pattern.amplitudes.any { it == VibrationEffect.DEFAULT_AMPLITUDE })
        assertTrue(pattern.amplitudes.any { it == 0 })
        assertTrue(pattern.timingsMs.filterIndexed { index, _ ->
            pattern.amplitudes[index] == VibrationEffect.DEFAULT_AMPLITUDE
        }.all { it <= 32L })
    }
}
