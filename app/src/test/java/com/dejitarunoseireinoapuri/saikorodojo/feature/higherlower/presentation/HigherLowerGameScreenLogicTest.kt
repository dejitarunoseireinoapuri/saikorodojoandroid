package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.compose.ui.unit.dp
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
    fun `current total shows only when current dice are visible`() {
        assertFalse(
            shouldShowHigherLowerCurrentTotal(
                isRolling = true,
                isTransitioning = false,
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = false,
                hasCurrentDice = true
            )
        )
        assertFalse(
            shouldShowHigherLowerCurrentTotal(
                isRolling = false,
                isTransitioning = true,
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = false,
                hasCurrentDice = true
            )
        )
        assertFalse(
            shouldShowHigherLowerCurrentTotal(
                isRolling = false,
                isTransitioning = false,
                isCurrentDiceHidden = true,
                isCurrentDiceAnchoredUp = false,
                hasCurrentDice = true
            )
        )
        assertFalse(
            shouldShowHigherLowerCurrentTotal(
                isRolling = false,
                isTransitioning = false,
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = true,
                hasCurrentDice = true
            )
        )
        assertFalse(
            shouldShowHigherLowerCurrentTotal(
                isRolling = false,
                isTransitioning = false,
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = false,
                hasCurrentDice = false
            )
        )
        assertTrue(
            shouldShowHigherLowerCurrentTotal(
                isRolling = false,
                isTransitioning = false,
                isCurrentDiceHidden = false,
                isCurrentDiceAnchoredUp = false,
                hasCurrentDice = true
            )
        )
    }

    @Test
    fun `choice row stays visible when a selection exists`() {
        assertEquals(
            HigherLowerChoiceButtonsMode.Both,
            higherLowerChoiceButtonsMode(isChoiceVisible = true, selectedChoice = null)
        )
        assertEquals(
            HigherLowerChoiceButtonsMode.SelectedOnly,
            higherLowerChoiceButtonsMode(
                isChoiceVisible = false,
                selectedChoice = HigherLowerChoice.HIGHER
            )
        )
        assertEquals(
            HigherLowerChoiceButtonsMode.Hidden,
            higherLowerChoiceButtonsMode(isChoiceVisible = false, selectedChoice = null)
        )
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

    @Test
    fun `mats keep fixed spacing from continue button area even when hidden`() {
        assertEquals(88.dp, higherLowerMatsBottomPadding())
    }
}
