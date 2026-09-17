package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import android.animation.ValueAnimator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.presentation.LocalHapticsEnabled
import kotlin.math.PI
import kotlin.math.sin

/** One clock keeps the visual motion and the continuous rolling texture synchronized. */
@Composable
internal fun rememberDiceRollMotion(
    isRolling: Boolean,
    rollKey: Any = Unit,
    durationMs: Long = 1_500L
): State<Float> {
    val progress = remember { Animatable(1f) }
    val view = LocalView.current
    val haptics = remember(view) { DiceRollHaptics(view) }
    val isHapticsEnabled = LocalHapticsEnabled.current
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val isVisible = lifecycleState.isAtLeast(Lifecycle.State.STARTED)
    LaunchedEffect(isRolling, rollKey, isVisible, durationMs, haptics, isHapticsEnabled) {
        if (!isVisible || !isRolling || durationMs <= 0L || !ValueAnimator.areAnimatorsEnabled()) {
            haptics.cancel()
            progress.snapTo(1f)
        } else {
            progress.snapTo(0f)
            try {
                if (isHapticsEnabled) {
                    haptics.playRoll(durationMs)
                } else {
                    haptics.cancel()
                }
                progress.animateTo(
                    1f,
                    tween(durationMs.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(), easing = LinearEasing)
                )
            } finally {
                haptics.cancel()
            }
        }
    }
    return progress.asState()
}

internal fun calculateRollPlaybackMs(durationMs: Long, tickMs: Long, omitLastDelay: Boolean = false): Long {
    if (durationMs <= 0L) return 0L
    val tick = tickMs.coerceAtLeast(1L)
    val steps = (durationMs / tick).coerceAtLeast(1L)
    return (steps - if (omitLastDelay) 1L else 0L) * tick
}

internal fun diceTravelFraction(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return if (p == 0f || p == 1f) 0f else sin(PI * p).toFloat() * (1f - p)
}

/** Later bounces lose energy as the dice settle. */
internal fun diceBounceHeight(progress: Float): Float {
    val p = progress.coerceIn(0f, 1f)
    return when {
        p < 0.44f -> sin(PI * p / 0.44f).toFloat() * 0.16f
        p < 0.74f -> sin(PI * (p - 0.44f) / 0.30f).toFloat() * 0.075f
        p < 0.90f -> sin(PI * (p - 0.74f) / 0.16f).toFloat() * 0.028f
        else -> 0f
    }.coerceAtLeast(0f)
}
