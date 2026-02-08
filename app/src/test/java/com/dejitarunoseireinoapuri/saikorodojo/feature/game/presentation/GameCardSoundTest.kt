package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
