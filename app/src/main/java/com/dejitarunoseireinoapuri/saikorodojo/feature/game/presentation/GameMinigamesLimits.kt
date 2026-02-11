package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

internal const val MAX_MINIGAMES_AVAILABLE = 99

internal fun clampMinigamesAvailable(value: Int): Int {
    return value.coerceIn(0, MAX_MINIGAMES_AVAILABLE)
}
