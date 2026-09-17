package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class MinigameButtonColorsTest {
    @Test
    fun `primary button uses gold`() {
        assertEquals(Color(0xFFE6B85C), MinigameButtonPrimaryColor)
    }

    @Test
    fun `disabled button uses a muted slate surface`() {
        assertEquals(Color(0xFF29394D), MinigameButtonPrimaryDisabledColor)
    }
}
