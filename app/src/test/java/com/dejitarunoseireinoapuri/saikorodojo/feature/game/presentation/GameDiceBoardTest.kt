package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertTrue

class GameDiceBoardTest {
    @Test
    fun positionsStayWithinBoardBounds() {
        val availableWidth = 240.dp
        val availableHeight = 200.dp
        val diceSize = 40.dp
        val positions = calculateRandomDicePositions(
            seed = 42L,
            diceCount = 6,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceSize = diceSize,
            minSpacing = 4.dp
        )

        assertTrue(positions.isNotEmpty())
        positions.forEach { position ->
            assertTrue(position.x >= 0.dp)
            assertTrue(position.y >= 0.dp)
            assertTrue(position.x <= availableWidth - diceSize)
            assertTrue(position.y <= availableHeight - diceSize)
        }
    }
}
