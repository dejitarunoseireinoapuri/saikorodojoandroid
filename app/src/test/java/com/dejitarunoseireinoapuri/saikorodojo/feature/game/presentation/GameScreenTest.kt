package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R

class GameScreenTest {
    @Test
    fun `select dice face drawables is deterministic for the same seed`() {
        val faces = listOf(10, 20, 30)

        val first = selectDiceFaceDrawables(seed = 42L, diceCount = 5, faces = faces)
        val second = selectDiceFaceDrawables(seed = 42L, diceCount = 5, faces = faces)

        assertEquals(first, second)
    }

    @Test
    fun `select dice face drawables returns empty when count is zero`() {
        val faces = listOf(10, 20, 30)

        val result = selectDiceFaceDrawables(seed = 42L, diceCount = 0, faces = faces)

        assertEquals(emptyList<Int>(), result)
    }

    @Test
    fun `dice number y offset is applied for eight sides`() {
        assertEquals(6.dp, diceNumberYOffset(R.drawable.eigth_sides))
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
            minSpacing = minSpacing,
            columns = 2
        )

        assertTrue(positions.all { position ->
            positions.filterNot { it == position }.all {
                !overlaps(position, it, diceSize, minSpacing)
            }
        })
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
