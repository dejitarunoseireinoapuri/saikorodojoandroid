package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun gradientColorsUsesAppPalette() {
        val colors = gradientColors()

        assertEquals(LightMenuGameGradientTop, colors.menuGameTop)
        assertEquals(LightMenuGameGradientMiddle, colors.menuGameMiddle)
        assertEquals(LightMenuGameGradientBottom, colors.menuGameBottom)
    }
}
