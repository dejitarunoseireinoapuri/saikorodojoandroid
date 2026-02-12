package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

internal val MinigameButtonPrimaryColor = Color(0xFFFFA726)
internal val MinigameButtonPrimaryDisabledColor = Color(0xFFE87400)
private const val DisabledContainerAlpha = 0.65f
private const val DisabledContentAlpha = 0.75f

@Composable
internal fun minigameButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MinigameButtonPrimaryColor,
    contentColor = Color.White,
    disabledContainerColor = MinigameButtonPrimaryDisabledColor.copy(alpha = DisabledContainerAlpha),
    disabledContentColor = Color.White.copy(alpha = DisabledContentAlpha)
)
