package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDiceBoardTest {
    @Test
    fun calculateRandomDicePositionsCentersGridWithEqualInsets() {
        val availableWidth = 105.dp
        val availableHeight = 95.dp
        val diceSize = 20.dp
        val minSpacing = 0.dp
        val columns = ((availableWidth + minSpacing) / (diceSize + minSpacing)).toInt().coerceAtLeast(1)
        val rows = ((availableHeight + minSpacing) / (diceSize + minSpacing)).toInt().coerceAtLeast(1)
        val totalCells = columns * rows
        val gridWidth = (diceSize * columns) + (minSpacing * (columns - 1).coerceAtLeast(0))
        val gridHeight = (diceSize * rows) + (minSpacing * (rows - 1).coerceAtLeast(0))
        val expectedInsetX = ((availableWidth - gridWidth) / 2f).coerceAtLeast(0.dp)
        val expectedInsetY = ((availableHeight - gridHeight) / 2f).coerceAtLeast(0.dp)

        val positions = calculateRandomDicePositions(
            seed = 0L,
            diceCount = totalCells,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceSize = diceSize,
            minSpacing = minSpacing
        )

        val minX = positions.minOf { it.x.value }
        val maxX = positions.maxOf { it.x.value }
        val minY = positions.minOf { it.y.value }
        val maxY = positions.maxOf { it.y.value }

        assertEquals(expectedInsetX.value, minX, 0.001f)
        assertEquals(expectedInsetY.value, minY, 0.001f)
        assertEquals(expectedInsetX.value + gridWidth.value - diceSize.value, maxX, 0.001f)
        assertEquals(expectedInsetY.value + gridHeight.value - diceSize.value, maxY, 0.001f)
    }

    @Test
    fun diceNumberYOffsetUsesSameOffsetForGreenD8Face() {
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides))
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides_green))
    }
}
