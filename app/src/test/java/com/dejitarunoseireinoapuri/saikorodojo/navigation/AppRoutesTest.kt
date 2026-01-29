package com.dejitarunoseireinoapuri.saikorodojo.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRoutesTest {
    @Test
    fun `play destination points to blackjack game`() {
        assertEquals(AppRoutes.BlackjackGame, AppRoutes.PlayDestination)
    }
}
