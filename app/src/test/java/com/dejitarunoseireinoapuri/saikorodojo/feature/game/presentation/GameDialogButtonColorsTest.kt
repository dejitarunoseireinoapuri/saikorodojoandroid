package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceSetValueOuterColor
import org.junit.Assert.assertEquals
import org.junit.Test

class GameDialogButtonColorsTest {
    @Test
    fun `returns default colors when accent flags are disabled`() {
        val defaultConfirm = Color(0xFF112233)
        val defaultDismiss = Color(0xFF445566)

        val result = gameDialogButtonColors(
            confirmUsesAccent = false,
            dismissUsesAccent = false,
            defaultConfirmColor = defaultConfirm,
            defaultDismissColor = defaultDismiss
        )

        assertEquals(defaultConfirm, result.confirmColor)
        assertEquals(defaultDismiss, result.dismissColor)
    }

    @Test
    fun `returns accent confirm color when confirm flag is enabled`() {
        val result = gameDialogButtonColors(
            confirmUsesAccent = true,
            dismissUsesAccent = false,
            defaultConfirmColor = Color(0xFF112233),
            defaultDismissColor = Color(0xFF445566)
        )

        assertEquals(DiceSetValueOuterColor, result.confirmColor)
    }

    @Test
    fun `returns accent dismiss color when dismiss flag is enabled`() {
        val result = gameDialogButtonColors(
            confirmUsesAccent = false,
            dismissUsesAccent = true,
            defaultConfirmColor = Color(0xFF112233),
            defaultDismissColor = Color(0xFF445566)
        )

        assertEquals(DiceSetValueOuterColor, result.dismissColor)
    }
}
