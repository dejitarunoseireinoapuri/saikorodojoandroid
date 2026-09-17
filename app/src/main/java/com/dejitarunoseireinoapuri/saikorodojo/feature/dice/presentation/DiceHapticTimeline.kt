package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

internal data class DiceHapticPattern(
    val timingsMs: LongArray,
    val amplitudes: IntArray
)

private const val MIN_ROLL_HAPTIC_DURATION_MS = 300L
private const val AMPLITUDE_CONTROLLED_PULSE_MS = 28L
private const val AMPLITUDE_CONTROLLED_GAP_MS = 22L
private const val BASIC_MOTOR_PULSE_MS = 32L
private const val BASIC_MOTOR_GAP_MS = 38L

/** One motor command provides a light rolling texture for the whole animation. */
internal fun diceRollHapticPattern(durationMs: Long, hasAmplitudeControl: Boolean): DiceHapticPattern {
    val duration = durationMs.coerceAtLeast(MIN_ROLL_HAPTIC_DURATION_MS)
    val pulseDuration = if (hasAmplitudeControl) {
        AMPLITUDE_CONTROLLED_PULSE_MS
    } else {
        BASIC_MOTOR_PULSE_MS
    }
    val gapDuration = if (hasAmplitudeControl) {
        AMPLITUDE_CONTROLLED_GAP_MS
    } else {
        BASIC_MOTOR_GAP_MS
    }
    val timings = mutableListOf(0L)
    val amplitudes = mutableListOf(0)
    var elapsedMs = 0L
    var pulseIndex = 0

    while (elapsedMs < duration) {
        val activeDuration = pulseDuration.coerceAtMost(duration - elapsedMs)
        val progress = elapsedMs.toFloat() / duration.toFloat()
        timings += activeDuration
        amplitudes += if (hasAmplitudeControl) {
            rollingAmplitude(progress, pulseIndex)
        } else {
            android.os.VibrationEffect.DEFAULT_AMPLITUDE
        }
        elapsedMs += activeDuration

        if (elapsedMs < duration) {
            val silenceDuration = gapDuration.coerceAtMost(duration - elapsedMs)
            timings += silenceDuration
            amplitudes += 0
            elapsedMs += silenceDuration
        }
        pulseIndex += 1
    }

    return DiceHapticPattern(
        timingsMs = timings.toLongArray(),
        amplitudes = amplitudes.toIntArray()
    )
}

private fun rollingAmplitude(progress: Float, pulseIndex: Int): Int {
    val baseAmplitude = when {
        progress < 0.55f -> 68
        progress < 0.82f -> 56
        else -> 42
    }
    val textureOffset = when (pulseIndex % 3) {
        0 -> 4
        1 -> -3
        else -> 1
    }
    return baseAmplitude + textureOffset
}
