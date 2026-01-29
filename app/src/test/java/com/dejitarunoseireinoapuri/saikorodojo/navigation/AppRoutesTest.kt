package com.dejitarunoseireinoapuri.saikorodojo.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppRoutesTest {
    @Test
    fun `play destination points to blackjack game`() {
        assertEquals(AppRoutes.BlackjackGame, AppRoutes.PlayDestination)
    }
}
