package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
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
}
