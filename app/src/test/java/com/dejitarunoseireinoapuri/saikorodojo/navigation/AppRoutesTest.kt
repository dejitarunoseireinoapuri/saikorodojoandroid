package com.dejitarunoseireinoapuri.saikorodojo.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {
    @Test
    fun `play destination points to main game`() {
        assertEquals(AppRoutes.GAME, AppRoutes.PLAY_DESTINATION)
    }

    @Test
    fun `rules route is stable`() {
        assertEquals("rules", AppRoutes.RULES)
    }

    @Test
    fun `settings route is stable`() {
        assertEquals("settings", AppRoutes.SETTINGS)
    }
}
