package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HigherLowerGameScreenLogicTest {
    @Test
    fun `totals are hidden while rolling or transitioning`() {
        assertFalse(shouldShowHigherLowerTotals(isRolling = true, isTransitioning = false))
        assertFalse(shouldShowHigherLowerTotals(isRolling = false, isTransitioning = true))
        assertFalse(shouldShowHigherLowerTotals(isRolling = true, isTransitioning = true))
        assertTrue(shouldShowHigherLowerTotals(isRolling = false, isTransitioning = false))
    }

    @Test
    fun `choice row stays visible when a selection exists`() {
        assertTrue(shouldShowHigherLowerChoiceRow(isChoiceVisible = true, selectedChoice = null))
        assertTrue(
            shouldShowHigherLowerChoiceRow(
                isChoiceVisible = false,
                selectedChoice = HigherLowerChoice.HIGHER
            )
        )
        assertFalse(shouldShowHigherLowerChoiceRow(isChoiceVisible = false, selectedChoice = null))
    }

    @Test
    fun `bottom mat highlights success or failure`() {
        assertEquals(
            HigherLowerMatColors(VictoryMatBackground, VictoryMatBackground),
            higherLowerBottomMatColors(
                isSuccessHighlighting = true,
                isComplete = false,
                hasLoss = false
            )
        )
        assertEquals(
            HigherLowerMatColors(FailureMatBackground, FailureMatBackground),
            higherLowerBottomMatColors(
                isSuccessHighlighting = false,
                isComplete = true,
                hasLoss = true
            )
        )
        assertEquals(
            HigherLowerMatColors(SequenceSaveMatBackground, SequenceSaveMatBorder),
            higherLowerBottomMatColors(
                isSuccessHighlighting = false,
                isComplete = false,
                hasLoss = false
            )
        )
    }
}
