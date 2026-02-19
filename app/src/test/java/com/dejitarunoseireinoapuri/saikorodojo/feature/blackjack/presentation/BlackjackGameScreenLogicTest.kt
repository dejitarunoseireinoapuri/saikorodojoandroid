package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlackjackGameScreenLogicTest {

    @Test
    fun blackjackDieNumberHorizontalPadding_returnsOneDp() {
        assertEquals(1.dp, blackjackDieNumberHorizontalPadding())
    }
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
    fun shouldPlayOutcomeSound_returnsTrue_whenWinAppearsAfterRoundStart() {
        assertTrue(
            shouldPlayOutcomeSound(
                previousOutcome = null,
                currentOutcome = BlackjackOutcome.PLAYER_WIN,
                isStarted = true
            )
        )
    }

    @Test
    fun shouldPlayOutcomeSound_returnsFalse_whenGameIsNotStarted() {
        assertFalse(
            shouldPlayOutcomeSound(
                previousOutcome = null,
                currentOutcome = BlackjackOutcome.PLAYER_LOSE,
                isStarted = false
            )
        )
    }

    @Test
    fun shouldPlayOutcomeSound_returnsFalse_whenOutcomeIsUnchanged() {
        assertFalse(
            shouldPlayOutcomeSound(
                previousOutcome = BlackjackOutcome.PLAYER_WIN,
                currentOutcome = BlackjackOutcome.PLAYER_WIN,
                isStarted = true
            )
        )
    }

    @Test
    fun shouldPlayOutcomeSound_returnsTrue_whenLossAppearsAfterRoundStart() {
        assertTrue(
            shouldPlayOutcomeSound(
                previousOutcome = null,
                currentOutcome = BlackjackOutcome.PLAYER_LOSE,
                isStarted = true
            )
        )
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
