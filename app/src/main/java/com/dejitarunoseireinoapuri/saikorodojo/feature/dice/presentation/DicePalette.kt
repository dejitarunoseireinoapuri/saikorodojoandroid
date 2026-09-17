package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceCoralDark
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceCoralLight
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceIvoryDark
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceIvoryLight
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceTurquoiseDark
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceTurquoiseLight
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.Ivory
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.NightInk

internal data class DicePalette(val light: Color, val dark: Color, val ink: Color)

private val IvoryDie = DicePalette(DiceIvoryLight, DiceIvoryDark, NightInk)
private val TurquoiseDie = DicePalette(DiceTurquoiseLight, DiceTurquoiseDark, Ivory)
private val CoralDie = DicePalette(DiceCoralLight, DiceCoralDark, Ivory)

internal fun dicePalette(type: DiceType): DicePalette = when {
    type == DiceType.D6 -> IvoryDie
    type == DiceType.D8 -> TurquoiseDie
    else -> CoralDie
}

internal fun diceResultGoldAlpha(progress: Float, highlightResult: Boolean = true): Float =
    if (highlightResult && progress >= 1f) 1f else 0f

internal fun diceNumberScale(): Double = 1.5
