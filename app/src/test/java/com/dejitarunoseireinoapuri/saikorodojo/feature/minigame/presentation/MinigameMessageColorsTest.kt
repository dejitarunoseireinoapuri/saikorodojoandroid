package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertEquals
import org.junit.Test

class MinigameMessageColorsTest {
    @Test
    fun `returns victory color for win message`() {
        val titleColor = Color(0xFF123456)

        val result = minigameMessageColor(MinigameMessageType.Win, titleColor)

        assertEquals(VictoryMatBackground, result)
    }

    @Test
    fun `returns failure color for lose message`() {
        val titleColor = Color(0xFF654321)

        val result = minigameMessageColor(MinigameMessageType.Lose, titleColor)

        assertEquals(FailureMatBackground, result)
    }

    @Test
    fun `returns title color for win cards message`() {
        val titleColor = Color(0xFFABCDEF)

        val result = minigameMessageColor(MinigameMessageType.WinCards, titleColor)

        assertEquals(titleColor, result)
    }

    @Test
    fun `returns title color for other message`() {
        val titleColor = Color(0xFF0F0F0F)

        val result = minigameMessageColor(MinigameMessageType.Other, titleColor)

        assertEquals(titleColor, result)
    }
}
