package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceOptionNumberColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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

    @Test
    fun `packed positions keep corners and shuffle remaining cells by seed`() {
        val diceSize = 20.dp
        val spacing = 4.dp
        val columns = 3
        val rows = 3
        val availableWidth = diceSize * columns + spacing * (columns - 1)
        val availableHeight = diceSize * rows + spacing * (rows - 1)

        val positions = calculatePackedDicePositions(
            diceCount = 6,
            availableWidth = availableWidth,
            availableHeight = availableHeight,
            diceSize = diceSize,
            spacing = spacing,
            columns = columns,
            rows = rows,
            seed = 42L
        )

        val step = diceSize + spacing
        val cornerPositions = setOf(
            DicePosition(0.dp, 0.dp),
            DicePosition(step * 2, 0.dp),
            DicePosition(0.dp, step * 2),
            DicePosition(step * 2, step * 2)
        )

        assertEquals(6, positions.size)
        cornerPositions.forEach { corner ->
            assertTrue(positions.contains(corner))
        }
    }

    @Test
    fun `dice option number color uses mat darkest tone`() {
        assertEquals(DiceOptionNumberColor, diceOptionNumberColor())
    }

    @Test
    fun `move sound is suppressed during roll`() {
        assertEquals(false, shouldPlayMoveSound(isRolling = true))
        assertEquals(true, shouldPlayMoveSound(isRolling = false))
    }

    @Test
    fun `selection border ignores preselected dice outside reroll selection`() {
        assertTrue(
            shouldShowDiceSelectionBorder(
                isAwaitingRerollSelected = true,
                isAwaitingRerollSingle = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                isRerollSelected = true,
                isAdjustmentSelected = false,
                isSetValueSelected = false,
                isRerollSingleSelected = false,
                isFlipSelected = false
            )
        )
        assertEquals(
            false,
            shouldShowDiceSelectionBorder(
                isAwaitingRerollSelected = false,
                isAwaitingRerollSingle = true,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                isRerollSelected = false,
                isAdjustmentSelected = false,
                isSetValueSelected = false,
                isRerollSingleSelected = false,
                isFlipSelected = false
            )
        )
        assertTrue(
            shouldShowDiceSelectionBorder(
                isAwaitingRerollSelected = false,
                isAwaitingRerollSingle = true,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                isRerollSelected = false,
                isAdjustmentSelected = false,
                isSetValueSelected = false,
                isRerollSingleSelected = true,
                isFlipSelected = false
            )
        )
    }

    @Test
    fun `background die applies only to unselected d6 d8 and d10`() {
        assertTrue(shouldUseBackgroundDie(R.drawable.six_sides, isSelected = false))
        assertTrue(shouldUseBackgroundDie(R.drawable.eigth_sides, isSelected = false))
        assertTrue(shouldUseBackgroundDie(R.drawable.ten_sides, isSelected = false))
        assertFalse(shouldUseBackgroundDie(R.drawable.six_sides, isSelected = true))
        assertFalse(shouldUseBackgroundDie(R.drawable.eigth_sides, isSelected = true))
        assertFalse(shouldUseBackgroundDie(R.drawable.ten_sides, isSelected = true))
        assertFalse(shouldUseBackgroundDie(R.drawable.six_sides_selected, isSelected = false))
    }
}
