package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceSetValueOuterColor

internal data class GameDialogButtonColors(
    val confirmColor: Color,
    val dismissColor: Color
)

internal fun gameDialogButtonColors(
    confirmUsesAccent: Boolean = false,
    dismissUsesAccent: Boolean = false,
    defaultConfirmColor: Color,
    defaultDismissColor: Color
): GameDialogButtonColors {
    return GameDialogButtonColors(
        confirmColor = if (confirmUsesAccent) DiceSetValueOuterColor else defaultConfirmColor,
        dismissColor = if (dismissUsesAccent) DiceSetValueOuterColor else defaultDismissColor
    )
}

