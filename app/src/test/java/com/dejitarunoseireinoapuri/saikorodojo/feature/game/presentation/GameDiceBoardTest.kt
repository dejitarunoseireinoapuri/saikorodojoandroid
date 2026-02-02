package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDiceBoardTest {
    @Test
    fun calculateDicePositionsArrangesFiveDiceInTwoOneTwoRows() {
        val availableWidth = 100.dp
        val availableHeight = 120.dp
        val diceSize = 20.dp
        val minSpacing = 0.dp
        val expectedLeftX = 0.dp
        val expectedRightX = 80.dp
        val expectedCenterX = 40.dp
        val expectedRowSpacing = (availableHeight - diceSize * 3) / 2
        val expectedTopY = 0.dp
        val expectedMiddleY = diceSize + expectedRowSpacing
        val expectedBottomY = diceSize * 2 + expectedRowSpacing * 2

        val positions = calculateDicePositions(
            seed = 0L,
            diceCount = 5,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceSize = diceSize,
            minSpacing = minSpacing
        )

        assertEquals(5, positions.size)
        assertEquals(expectedLeftX, positions[0].x)
        assertEquals(expectedTopY, positions[0].y)
        assertEquals(expectedRightX, positions[1].x)
        assertEquals(expectedTopY, positions[1].y)
        assertEquals(expectedCenterX, positions[2].x)
        assertEquals(expectedMiddleY, positions[2].y)
        assertEquals(expectedLeftX, positions[3].x)
        assertEquals(expectedBottomY, positions[3].y)
        assertEquals(expectedRightX, positions[4].x)
        assertEquals(expectedBottomY, positions[4].y)
    }

    @Test
    fun calculateDicePositionsArrangesSixDiceInTwoPerRow() {
        val availableWidth = 100.dp
        val availableHeight = 120.dp
        val diceSize = 20.dp
        val minSpacing = 0.dp
        val expectedLeftX = 0.dp
        val expectedRightX = 80.dp
        val expectedRowSpacing = (availableHeight - diceSize * 3) / 2
        val expectedTopY = 0.dp
        val expectedMiddleY = diceSize + expectedRowSpacing
        val expectedBottomY = diceSize * 2 + expectedRowSpacing * 2

        val positions = calculateDicePositions(
            seed = 0L,
            diceCount = 6,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceSize = diceSize,
            minSpacing = minSpacing
        )

        assertEquals(6, positions.size)
        assertEquals(listOf(expectedLeftX, expectedRightX), positions.take(2).map { it.x })
        assertEquals(listOf(expectedTopY, expectedTopY), positions.take(2).map { it.y })
        assertEquals(listOf(expectedLeftX, expectedRightX), positions.drop(2).take(2).map { it.x })
        assertEquals(listOf(expectedMiddleY, expectedMiddleY), positions.drop(2).take(2).map { it.y })
        assertEquals(listOf(expectedLeftX, expectedRightX), positions.drop(4).map { it.x })
        assertEquals(listOf(expectedBottomY, expectedBottomY), positions.drop(4).map { it.y })
    }

    @Test
    fun diceNumberYOffsetUsesSameOffsetForGreenD8Face() {
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides))
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides_green))
    }
}
