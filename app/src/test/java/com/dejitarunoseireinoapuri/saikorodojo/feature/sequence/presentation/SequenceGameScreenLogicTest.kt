package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SequenceGameScreenLogicTest {
    @Test
    fun `continue button is visible only for shown end states`() {
        assertTrue(shouldShowSequenceContinueButton(hasReward = true, hasLoss = false))
        assertTrue(shouldShowSequenceContinueButton(hasReward = false, hasLoss = true))
        assertFalse(shouldShowSequenceContinueButton(hasReward = false, hasLoss = false))
    }


    @Test
    fun `latest saved die stays hidden while transition is active`() {
        val hidden = sequenceSavedDiceUiState(
            savedValues = listOf(2, 5, 8),
            isLatestSavedValueHidden = true
        )

        assertEquals(listOf(true, true, false), hidden.map { it.isVisible })

        val shown = sequenceSavedDiceUiState(
            savedValues = listOf(2, 5, 8),
            isLatestSavedValueHidden = false
        )

        assertEquals(listOf(true, true, true), shown.map { it.isVisible })
    }

    @Test
    fun `dice number y offset keeps values centered`() {
        assertEquals(0.dp, sequenceDiceNumberYOffset())
    }
}
