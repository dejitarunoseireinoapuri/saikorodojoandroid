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
    fun `new failure die stays hidden until failure animation starts`() {
        val hidden = shouldHidePendingFailureDie(
            failureDieValue = 6,
            previousFailureDieValue = null,
            animatingSaveValue = null,
            isAnimatingToFailure = false
        )

        assertTrue(hidden)
    }

    @Test
    fun `failure die is visible once failure animation is active`() {
        val hidden = shouldHidePendingFailureDie(
            failureDieValue = 6,
            previousFailureDieValue = null,
            animatingSaveValue = 6,
            isAnimatingToFailure = true
        )

        assertFalse(hidden)
    }

    @Test
    fun `already known failure die is never treated as pending`() {
        val hidden = shouldHidePendingFailureDie(
            failureDieValue = 6,
            previousFailureDieValue = 6,
            animatingSaveValue = null,
            isAnimatingToFailure = false
        )

        assertFalse(hidden)
    }

    @Test
    fun `latest saved die is visible when no animation is running`() {
        val visible = shouldShowSequenceSavedDie(
            isVisible = true,
            isLatest = true,
            animatingSaveValue = null,
            isAnimatingToFailure = false,
            value = 7
        )

        assertTrue(visible)
    }

    @Test
    fun `latest saved die remains hidden before animation effect starts`() {
        val latestDie = sequenceSavedDiceUiState(
            savedValues = listOf(3, 7),
            isLatestSavedValueHidden = true
        ).last()

        val visible = shouldShowSequenceSavedDie(
            isVisible = latestDie.isVisible,
            isLatest = latestDie.isLatest,
            animatingSaveValue = null,
            isAnimatingToFailure = false,
            value = latestDie.value
        )

        assertFalse(visible)
    }

    @Test
    fun `latest saved slot stays hidden when save is pending before animation starts`() {
        val hidden = shouldHideLatestSavedSlotUntilAnimationEnds(
            savedValuesSize = 2,
            lastAnimatedSavedCount = 1,
            animatingSaveValue = null,
            isAnimatingToFailure = false
        )

        assertTrue(hidden)
    }

    @Test
    fun `latest saved slot stays hidden while save animation is running`() {
        val hidden = shouldHideLatestSavedSlotUntilAnimationEnds(
            savedValuesSize = 2,
            lastAnimatedSavedCount = 2,
            animatingSaveValue = 7,
            isAnimatingToFailure = false
        )

        assertTrue(hidden)
    }

    @Test
    fun `latest saved slot is visible when no save animation is pending`() {
        val hidden = shouldHideLatestSavedSlotUntilAnimationEnds(
            savedValuesSize = 2,
            lastAnimatedSavedCount = 2,
            animatingSaveValue = null,
            isAnimatingToFailure = false
        )

        assertFalse(hidden)
    }
}
