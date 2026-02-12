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
            isLatestSavedValueHidden = true,
            hasPendingSavedValue = false
        )

        assertEquals(listOf(true, true, false), hidden.map { it.isVisible })

        val shown = sequenceSavedDiceUiState(
            savedValues = listOf(2, 5, 8),
            isLatestSavedValueHidden = false,
            hasPendingSavedValue = false
        )

        assertEquals(listOf(true, true, true), shown.map { it.isVisible })
    }


    @Test
    fun `pending saved value keeps destination slot hidden on first logical frame`() {
        val dice = sequenceSavedDiceUiState(
            savedValues = listOf(4),
            isLatestSavedValueHidden = false,
            hasPendingSavedValue = true
        )

        assertEquals(2, dice.size)
        assertEquals(4, dice[0].value)
        assertTrue(dice[0].isVisible)
        assertFalse(dice[0].isLatest)
        assertEquals(null, dice[1].value)
        assertFalse(dice[1].isVisible)
        assertTrue(dice[1].isLatest)
    }

    @Test
    fun `dice number y offset keeps values centered`() {
        assertEquals(0.dp, sequenceDiceNumberYOffset())
    }

    @Test
    fun `saved die stays hidden while save animation is active`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = true,
            animatingSaveValue = 5,
            isAnimatingToFailure = false,
            value = 5
        )

        assertFalse(hidden)
    }

    @Test
    fun `failure die stays hidden while failure animation is active`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = false,
            animatingSaveValue = 3,
            isAnimatingToFailure = true,
            value = 3
        )

        assertFalse(hidden)
    }

    @Test
    fun `non latest dice remain visible during save animation`() {
        val visible = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = false,
            animatingSaveValue = 7,
            isAnimatingToFailure = false,
            value = 4
        )

        assertTrue(visible)
    }

    @Test
    fun `top die is hidden while transfer animation is active`() {
        assertFalse(
            shouldShowSequenceTopDie(
                isComplete = false,
                failureDieValue = null,
                animatingSaveValue = 6
            )
        )
    }

    @Test
    fun `top die stays hidden when round is complete`() {
        assertFalse(
            shouldShowSequenceTopDie(
                isComplete = true,
                failureDieValue = 2,
                animatingSaveValue = null
            )
        )
    }

    @Test
    fun `top die is visible during active round without transfer animation`() {
        assertTrue(
            shouldShowSequenceTopDie(
                isComplete = false,
                failureDieValue = null,
                animatingSaveValue = null
            )
        )
    }

    @Test
    fun `top die remains visible on complete loss without failure die`() {
        assertTrue(
            shouldShowSequenceTopDie(
                isComplete = true,
                failureDieValue = null,
                animatingSaveValue = null
            )
        )
    }


    @Test
    fun `saved mat space is reserved once game starts`() {
        assertTrue(shouldReserveSequenceSavedMatSpace(isStarted = true))
        assertFalse(shouldReserveSequenceSavedMatSpace(isStarted = false))
    }

    @Test
    fun `saved mat is hidden while cards are being shown`() {
        assertFalse(
            shouldShowSequenceSavedMat(
                isStarted = true,
                hasReward = false,
                hasPendingReward = true
            )
        )
    }

    @Test
    fun `saved mat is visible during active rounds`() {
        assertTrue(
            shouldShowSequenceSavedMat(
                isStarted = true,
                hasReward = false,
                hasPendingReward = false
            )
        )
    }

    @Test
    fun `latest saved value visibility follows explicit transition state`() {
        assertFalse(
            shouldHideLatestSavedValue(
                isLatestSavedValueHidden = false,
                previousSavedCount = 1,
                currentSavedCount = 2
            )
        )
        assertTrue(
            shouldHideLatestSavedValue(
                isLatestSavedValueHidden = true,
                previousSavedCount = 2,
                currentSavedCount = 2
            )
        )
    }

}
