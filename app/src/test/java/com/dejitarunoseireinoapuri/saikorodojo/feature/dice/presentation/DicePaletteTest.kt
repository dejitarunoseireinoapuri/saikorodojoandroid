package com.dejitarunoseireinoapuri.saikorodojo.feature.dice.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DicePaletteTest {
    @Test
    fun `numbers remain readable across all dice types`() {
        for (type in DiceType.entries) {
            val palette = dicePalette(type)
            assertTrue("$type lit face", contrast(palette.ink, palette.light) >= 4.5f)
            assertTrue("$type shaded face", contrast(palette.ink, palette.dark) >= 4.5f)
        }
        val whitePalette = dicePalette(DiceType.D6)
        assertEquals(whitePalette, dicePalette(DiceType.D8))
        assertEquals(whitePalette, dicePalette(DiceType.D10))
    }

    @Test
    fun `numbers keep their size and only the settled result becomes gold`() {
        for (step in 0..99) assertEquals(0f, diceResultGoldAlpha(step / 100f), 0f)
        assertEquals(1f, diceResultGoldAlpha(1f), 0f)
        assertEquals(1f, diceResultGoldAlpha(2f), 0f)
        assertEquals(1.5, diceNumberScale(), 0.0)
    }

    @Test
    fun `option dice show their displayed face in gold`() {
        assertEquals(1f, diceResultGoldAlpha(progress = 1f, highlightResult = true), 0f)
    }

    private fun contrast(a: Color, b: Color): Float =
        (maxOf(a.luminance(), b.luminance()) + 0.05f) /
            (minOf(a.luminance(), b.luminance()) + 0.05f)
}
