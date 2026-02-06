package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal enum class MinigameMessageType {
    Win,
    WinCards,
    Lose,
    Other
}

internal fun minigameMessageColor(
    messageType: MinigameMessageType,
    titleColor: Color
): Color {
    return when (messageType) {
        MinigameMessageType.Win -> VictoryMatBackground
        MinigameMessageType.Lose -> FailureMatBackground
        MinigameMessageType.WinCards,
        MinigameMessageType.Other -> titleColor
    }
}
