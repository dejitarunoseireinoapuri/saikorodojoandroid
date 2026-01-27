package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeColorsTest {
    @Test
    fun gradientColorsUsesAppPalette() {
        val colors = gradientColors()

        assertEquals(LightMenuGameGradientTop, colors.menuGameTop)
        assertEquals(LightMenuGameGradientMiddle, colors.menuGameMiddle)
        assertEquals(LightMenuGameGradientBottom, colors.menuGameBottom)
    }
}
