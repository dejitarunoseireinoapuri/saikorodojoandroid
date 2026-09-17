package com.dejitarunoseireinoapuri.saikorodojo.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.compose.ui.graphics.luminance
import org.junit.Test

class ThemeColorsTest {
    @Test
    fun essentialTextAndActionsHaveReadableContrast() {
        val pairs = listOf(
            AppOnPrimary to AppPrimary,
            AppOnSecondary to AppSecondary,
            AppOnSurface to AppSurface,
            AppOnSurfaceVariant to AppSurfaceVariant,
            AppOnBackground to AppBackground,
            SuccessText to AppBackground,
            FailureText to AppBackground
        )
        for ((text, background) in pairs) {
            val contrast = (maxOf(text.luminance(), background.luminance()) + 0.05f) /
                (minOf(text.luminance(), background.luminance()) + 0.05f)
            assertTrue("Insufficient contrast: $contrast", contrast >= 4.5f)
        }
    }

    @Test
    fun appGradientColorsUsesAppPalette() {
        val colors = AppGradientColors

        assertEquals(LightMenuGameGradientTop, colors.menuGameTop)
        assertEquals(LightMenuGameGradientMiddle, colors.menuGameMiddle)
        assertEquals(LightMenuGameGradientBottom, colors.menuGameBottom)
    }
}
