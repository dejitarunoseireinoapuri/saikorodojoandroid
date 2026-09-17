package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/** One shared feedback player per roll, rather than one motor pulse per die. */
internal class DiceRollHaptics(private val view: View) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        view.context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        view.context.getSystemService(Vibrator::class.java)
    }
    private val legacyAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    @Suppress("DEPRECATION")
    fun playRoll(durationMs: Long) {
        val motor = vibrator
        if (motor == null || !motor.hasVibrator()) {
            view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
            return
        }
        val pattern = diceRollHapticPattern(durationMs, motor.hasAmplitudeControl())
        val effect = VibrationEffect.createWaveform(pattern.timingsMs, pattern.amplitudes, -1)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                motor.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_MEDIA)
                )
            } else {
                motor.vibrate(effect, legacyAttributes)
            }
        } catch (_: SecurityException) {
            view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
            )
        }
    }

    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (_: SecurityException) {
            // A denied motor cannot leave an app-owned vibration running.
        }
    }
}
