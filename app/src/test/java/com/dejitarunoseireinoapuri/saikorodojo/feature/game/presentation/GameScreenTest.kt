package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType

class GameScreenTest {
    @Test
    fun `calculate dice size returns zero when dice count is zero`() {
        val result = calculateDiceSize(
            availableWidth = 100.dp,
            availableHeight = 100.dp,
            diceCount = 0,
            spacing = 4.dp,
            columns = 2
        )

        assertEquals(0.dp, result)
    }

    @Test
    fun `calculate dice size uses the smallest dimension`() {
        val result = calculateDiceSize(
            availableWidth = 120.dp,
            availableHeight = 60.dp,
            diceCount = 4,
            spacing = 4.dp,
            columns = 2
        )

        val expected = (60.dp - 4.dp) / 2

        assertEquals(expected, result)
    }

    @Test
    fun `dice type drawable maps each dice type to its asset`() {
        assertEquals(R.drawable.six_sides, diceTypeDrawable(DiceType.D6))
        assertEquals(R.drawable.eigth_sides, diceTypeDrawable(DiceType.D8))
        assertEquals(R.drawable.ten_sides, diceTypeDrawable(DiceType.D10))
    }

    @Test
    fun `dice number y offset is applied for eight sides`() {
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides))
    }

    @Test
    fun `dice number y offset is zero for other faces`() {
        assertEquals(0.dp, diceNumberYOffset(R.drawable.six_sides))
    }

    @Test
    fun `dice face drawable uses selected asset when selected`() {
        val result = diceFaceDrawable(R.drawable.six_sides, true)

        assertEquals(R.drawable.six_sides_selected, result)
    }

    @Test
    fun `dice face drawable keeps original asset when not selected`() {
        val result = diceFaceDrawable(R.drawable.ten_sides, false)

        assertEquals(R.drawable.ten_sides, result)
    }

    @Test
    fun `dice face drawable keeps original asset for unknown faces`() {
        val result = diceFaceDrawable(faceDrawable = 9999, isSelected = true)

        assertEquals(9999, result)
    }

    @Test
    fun `grid positions returns empty when dice count is zero`() {
        val positions = calculateRandomDicePositions(
            seed = 42L,
            diceCount = 0,
            availableWidth = 200.dp,
            availableHeight = 200.dp,
            diceSize = 50.dp,
            minSpacing = 4.dp
        )

        assertEquals(emptyList<DicePosition>(), positions)
    }

    @Test
    fun `grid positions are deterministic for the same seed`() {
        val diceSize = 40.dp
        val minSpacing = 4.dp

        val first = calculateRandomDicePositions(
            seed = 123L,
            diceCount = 4,
            availableWidth = 200.dp,
            availableHeight = 200.dp,
            diceSize = diceSize,
            minSpacing = minSpacing
        )
        val second = calculateRandomDicePositions(
            seed = 123L,
            diceCount = 4,
            availableWidth = 200.dp,
            availableHeight = 200.dp,
            diceSize = diceSize,
            minSpacing = minSpacing
        )

        assertEquals(first, second)
    }

    @Test
    fun `grid positions keep minimum spacing`() {
        val diceSize = 50.dp
        val minSpacing = 4.dp
        val positions = calculateRandomDicePositions(
            seed = 99L,
            diceCount = 4,
            availableWidth = 200.dp,
            availableHeight = 200.dp,
            diceSize = diceSize,
            minSpacing = minSpacing
        )

        assertEquals(4, positions.size)
        assertTrue(positions.all { position ->
            positions.filterNot { it == position }.all {
                !overlaps(position, it, diceSize, minSpacing)
            }
        })
    }

    @Test
    fun `card stack start x centers when there are fewer card types`() {
        val startX = calculateCardStackStartX(
            cardsCount = 3,
            maxCardTypes = 7,
            maxWidth = 360.dp,
            cardSize = DpSize(180.dp, 240.dp),
            stackSpacing = 40.dp,
            rightPadding = 8.dp
        )

        assertEquals(50.dp, startX)
    }

    @Test
    fun `card stack start x uses right alignment when card types are complete`() {
        val startX = calculateCardStackStartX(
            cardsCount = 7,
            maxCardTypes = 7,
            maxWidth = 500.dp,
            cardSize = DpSize(180.dp, 240.dp),
            stackSpacing = 20.dp,
            rightPadding = 8.dp
        )

        assertEquals(208.dp, startX)
    }
}

private fun overlaps(
    first: DicePosition,
    second: DicePosition,
    diceSize: androidx.compose.ui.unit.Dp,
    minSpacing: androidx.compose.ui.unit.Dp
): Boolean {
    val sizeWithSpacing = diceSize + minSpacing
    val firstRight = first.x + sizeWithSpacing
    val firstBottom = first.y + sizeWithSpacing
    val secondRight = second.x + sizeWithSpacing
    val secondBottom = second.y + sizeWithSpacing
    val overlapsHorizontally = first.x < secondRight && firstRight > second.x
    val overlapsVertically = first.y < secondBottom && firstBottom > second.y
    return overlapsHorizontally && overlapsVertically
}
