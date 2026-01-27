package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeColorsTest {
    @Test
    fun gradientColorsUsesLightPaletteWhenNotDarkTheme() {
        val colors = gradientColors(darkTheme = false)

        assertEquals(LightMenuGameGradientTop, colors.menuGameTop)
        assertEquals(LightMenuGameGradientMiddle, colors.menuGameMiddle)
        assertEquals(LightMenuGameGradientBottom, colors.menuGameBottom)
    }

    @Test
    fun gradientColorsUsesDarkPaletteWhenDarkTheme() {
        val colors = gradientColors(darkTheme = true)

        assertEquals(DarkMenuGameGradientTop, colors.menuGameTop)
        assertEquals(DarkMenuGameGradientMiddle, colors.menuGameMiddle)
        assertEquals(DarkMenuGameGradientBottom, colors.menuGameBottom)
    }
}
