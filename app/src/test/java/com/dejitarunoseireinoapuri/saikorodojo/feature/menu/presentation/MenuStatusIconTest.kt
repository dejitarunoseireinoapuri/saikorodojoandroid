package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import android.view.HapticFeedbackConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MenuStatusIconTest {
    @Test
    fun `disabled status icons are dimmed and crossed out`() {
        assertEquals(0.45f, statusIconAlpha(isEnabled = false), 0f)
        assertTrue(shouldShowStatusIconSlash(isEnabled = false))
    }

    @Test
    fun `enabled status icons are fully visible without a slash`() {
        assertEquals(1f, statusIconAlpha(isEnabled = true), 0f)
        assertFalse(shouldShowStatusIconSlash(isEnabled = true))
    }

    @Test
    fun `haptic confirmation plays only when vibration is being enabled`() {
        assertTrue(shouldPlayHapticsActivationFeedback(isCurrentlyEnabled = false))
        assertFalse(shouldPlayHapticsActivationFeedback(isCurrentlyEnabled = true))
        assertEquals(HapticFeedbackConstants.CONTEXT_CLICK, hapticsActivationFeedback)
    }
}
