package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureText
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SuccessText

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
        MinigameMessageType.Win -> SuccessText
        MinigameMessageType.Lose -> FailureText
        MinigameMessageType.WinCards,
        MinigameMessageType.Other -> titleColor
    }
}
