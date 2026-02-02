package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDiceBoardTest {
    @Test
    fun `grid spec keeps 3x3 for 7 to 9 dice`() {
        val availableWidth = 300.dp
        val availableHeight = 300.dp
        val spacing = 4.dp

        val sevenSpec = calculateDiceGridSpec(
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceCount = 7,
            spacing = spacing
        )
        val eightSpec = calculateDiceGridSpec(
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceCount = 8,
            spacing = spacing
        )
        val nineSpec = calculateDiceGridSpec(
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceCount = 9,
            spacing = spacing
        )

        assertEquals(3, sevenSpec.columns)
        assertEquals(3, sevenSpec.rows)
        assertEquals(3, eightSpec.columns)
        assertEquals(3, eightSpec.rows)
        assertEquals(3, nineSpec.columns)
        assertEquals(3, nineSpec.rows)
    }
}
