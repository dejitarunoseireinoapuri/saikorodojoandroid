package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class MinigameButtonColorsTest {
    @Test
    fun `primary button color matches set-value dice orange`() {
        assertEquals(Color(0xFFFFA726), MinigameButtonPrimaryColor)
    }

    @Test
    fun `disabled button base color matches set-value dice inner orange`() {
        assertEquals(Color(0xFFE87400), MinigameButtonPrimaryDisabledColor)
    }
}
