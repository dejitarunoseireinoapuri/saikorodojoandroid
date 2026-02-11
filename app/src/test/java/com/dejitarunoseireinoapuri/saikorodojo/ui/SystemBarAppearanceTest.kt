package com.dejitarunoseireinoapuri.saikorodojo.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SystemBarAppearanceTest {

    @Test
    fun `isLightColor returns false for dark app background`() {
        assertFalse(isLightColor(0xFF1B1236.toInt()))
    }

    @Test
    fun `isLightColor returns true for light background`() {
        assertTrue(isLightColor(0xFFF2F6FA.toInt()))
    }

    @Test
    fun `resolveSystemBarAppearance keeps color and computes icon mode`() {
        val appearance = resolveSystemBarAppearance(0xFFF2F6FA.toInt())

        assertEquals(0xFFF2F6FA.toInt(), appearance.backgroundColor)
        assertTrue(appearance.useDarkIcons)
    }
}
