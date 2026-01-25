package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
