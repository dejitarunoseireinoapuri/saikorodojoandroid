package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Rect
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
    fun `saved dice remain visible while overlay handles pending movement`() {
        val shown = sequenceSavedDiceUiState(savedValues = listOf(2, 5, 8))

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
    fun `move anchor readiness requires source target and size`() {
        assertFalse(
            isSequenceMoveAnchorReady(
                diceCenterInRoot = null,
                targetCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                animatedDieSize = 10.dp
            )
        )
        assertFalse(
            isSequenceMoveAnchorReady(
                diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                targetCenterInRoot = null,
                animatedDieSize = 10.dp
            )
        )
        assertFalse(
            isSequenceMoveAnchorReady(
                diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                targetCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                animatedDieSize = 0.dp
            )
        )
        assertTrue(
            isSequenceMoveAnchorReady(
                diceCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                targetCenterInRoot = androidx.compose.ui.geometry.Offset.Zero,
                animatedDieSize = 10.dp
            )
        )
    }

    @Test
    fun `fallback target center resolves to next saved slot`() {
        val center = resolvePendingSequenceTargetCenter(
            boundsInRoot = Rect(left = 100f, top = 200f, right = 460f, bottom = 340f),
            dieSize = 80.dp,
            savedValuesCount = 1,
            horizontalPaddingPx = 16f,
            spacingPx = 10f,
            maxDieSizePx = 104f
        )

        assertEquals(280f, center?.x)
        assertEquals(270f, center?.y)
    }

}
