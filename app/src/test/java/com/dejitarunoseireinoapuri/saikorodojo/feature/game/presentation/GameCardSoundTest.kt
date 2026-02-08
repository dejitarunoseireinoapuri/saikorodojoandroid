package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCardSoundTest {
    @Test
    fun `play card draw sound when selecting and interaction enabled`() {
        assertTrue(shouldPlayCardDrawSound(isInteractionEnabled = true))
    }

    @Test
    fun `do not play card draw sound when interaction is disabled`() {
        assertFalse(shouldPlayCardDrawSound(isInteractionEnabled = false))
    }
}
