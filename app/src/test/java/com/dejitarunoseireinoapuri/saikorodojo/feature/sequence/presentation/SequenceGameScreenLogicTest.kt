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

    @Test
    fun `saved die stays hidden while save animation is active`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = true,
            isFailureDie = false,
            animatingSaveValue = 5,
            isAnimatingToFailure = false,
            hasPendingSaveAnimation = false,
            hasPendingFailureAnimation = false,
            value = 5
        )

        assertFalse(hidden)
    }

    @Test
    fun `failure die stays hidden while failure animation is active`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = false,
            isFailureDie = true,
            animatingSaveValue = 3,
            isAnimatingToFailure = true,
            hasPendingSaveAnimation = false,
            hasPendingFailureAnimation = false,
            value = 3
        )

        assertFalse(hidden)
    }

    @Test
    fun `non latest dice remain visible during save animation`() {
        val visible = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = false,
            isFailureDie = false,
            animatingSaveValue = 7,
            isAnimatingToFailure = false,
            hasPendingSaveAnimation = false,
            hasPendingFailureAnimation = false,
            value = 4
        )

        assertTrue(visible)
    }
    @Test
    fun `latest saved die stays hidden before save animation starts`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = true,
            isFailureDie = false,
            animatingSaveValue = null,
            isAnimatingToFailure = false,
            hasPendingSaveAnimation = true,
            hasPendingFailureAnimation = false,
            value = 8
        )

        assertFalse(hidden)
    }

    @Test
    fun `failure die stays hidden before failure animation starts`() {
        val hidden = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = false,
            isFailureDie = true,
            animatingSaveValue = null,
            isAnimatingToFailure = false,
            hasPendingSaveAnimation = false,
            hasPendingFailureAnimation = true,
            value = 2
        )

        assertFalse(hidden)
    }

    @Test
    fun `center die hides while animation is pending or running`() {
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                hasPendingSaveAnimation = true,
                hasPendingFailureAnimation = false
            )
        )
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                hasPendingSaveAnimation = false,
                hasPendingFailureAnimation = true
            )
        )
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = 4,
                hasPendingSaveAnimation = false,
                hasPendingFailureAnimation = false
            )
        )
        assertFalse(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                hasPendingSaveAnimation = false,
                hasPendingFailureAnimation = false
            )
        )
    }

}
