package com.dejitarunoseireinoapuri.saikorodojo.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class AppRoutesTest {
    @Test
    fun playDestinationStartsNormalGame() {
        assertEquals(AppRoutes.Game, AppRoutes.PlayDestination)
    }
}
