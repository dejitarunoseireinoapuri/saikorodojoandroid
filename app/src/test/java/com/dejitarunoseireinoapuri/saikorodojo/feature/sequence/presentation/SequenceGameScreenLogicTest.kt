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
            pendingMoveTarget = null,
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
            pendingMoveTarget = null,
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
            pendingMoveTarget = null,
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
            pendingMoveTarget = SequenceMoveTarget.SAVED,
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
            pendingMoveTarget = SequenceMoveTarget.FAILURE,
            value = 2
        )

        assertFalse(hidden)
    }

    @Test
    fun `center die hides while animation is pending or running`() {
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                pendingMoveTarget = SequenceMoveTarget.SAVED
            )
        )
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                pendingMoveTarget = SequenceMoveTarget.FAILURE
            )
        )
        assertTrue(
            shouldHideSequenceCenterDie(
                animatingSaveValue = 4,
                pendingMoveTarget = null
            )
        )
        assertFalse(
            shouldHideSequenceCenterDie(
                animatingSaveValue = null,
                pendingMoveTarget = null
            )
        )
    }

    @Test
    fun `save animation waits for latest anchor index`() {
        val shouldWait = canStartSequenceMoveAnimation(
            diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterIndex = 0,
            expectedSavedIndex = 1,
            failureDieCenterInRoot = null,
            isAnimatingToFailureDie = false
        )

        assertFalse(shouldWait)

        val shouldStart = canStartSequenceMoveAnimation(
            diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterIndex = 1,
            expectedSavedIndex = 1,
            failureDieCenterInRoot = null,
            isAnimatingToFailureDie = false
        )

        assertTrue(shouldStart)
    }

    @Test
    fun `failure animation waits for failure anchor`() {
        val shouldWait = canStartSequenceMoveAnimation(
            diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterInRoot = null,
            savedDieCenterIndex = -1,
            expectedSavedIndex = 0,
            failureDieCenterInRoot = null,
            isAnimatingToFailureDie = true
        )

        assertFalse(shouldWait)

        val shouldStart = canStartSequenceMoveAnimation(
            diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            savedDieCenterInRoot = null,
            savedDieCenterIndex = -1,
            expectedSavedIndex = 0,
            failureDieCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
            isAnimatingToFailureDie = true
        )

        assertTrue(shouldStart)
    }

}
