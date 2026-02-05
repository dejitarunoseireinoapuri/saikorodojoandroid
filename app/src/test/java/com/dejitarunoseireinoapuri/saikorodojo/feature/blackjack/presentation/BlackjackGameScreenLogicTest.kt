package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import androidx.compose.ui.graphics.Color
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertEquals
import org.junit.Test

class BlackjackGameScreenLogicTest {
    @Test
    fun blackjackResultTextColor_returnsFailureColor_whenResultIsLoss() {
        val color = blackjackResultTextColor(
            result = BlackjackOutcome.PLAYER_LOSE,
            defaultColor = Color.Blue
        )

        assertEquals(FailureMatBackground, color)
    }

    @Test
    fun blackjackResultTextColor_returnsVictoryColor_whenResultIsWin() {
        val color = blackjackResultTextColor(
            result = BlackjackOutcome.PLAYER_WIN,
            defaultColor = Color.Blue
        )

        assertEquals(VictoryMatBackground, color)
    }

    @Test
    fun blackjackResultTextColor_returnsDefaultColor_whenResultIsNull() {
        val color = blackjackResultTextColor(
            result = null,
            defaultColor = Color.Blue
        )

        assertEquals(Color.Blue, color)
    }
}
