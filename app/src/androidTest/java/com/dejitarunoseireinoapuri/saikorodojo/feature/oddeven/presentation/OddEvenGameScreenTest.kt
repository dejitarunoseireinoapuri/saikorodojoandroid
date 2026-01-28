package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.fetchSemanticsNode
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OddEvenGameScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun diceUsesLargerSize() {
        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                OddEvenGameScreen(
                    applySystemBarsPadding = false,
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        currentRound = 1,
                        diceValue = 4
                    ),
                    onStartClick = {},
                    onChoiceSelect = {}
                )
            }
        }

        composeRule.onNodeWithTag(ODD_EVEN_DICE_TAG)
            .assertWidthIsEqualTo(ODD_EVEN_DICE_SIZE)
            .assertHeightIsEqualTo(ODD_EVEN_DICE_SIZE)
    }

    @Test
    fun diceKeepsMinimumDistanceFromChoiceButtons() {
        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                OddEvenGameScreen(
                    applySystemBarsPadding = false,
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        currentRound = 1,
                        diceValue = 2
                    ),
                    onStartClick = {},
                    onChoiceSelect = {}
                )
            }
        }

        val rowBounds = composeRule.onNodeWithTag(ODD_EVEN_CHOICE_ROW_TAG)
            .fetchSemanticsNode().boundsInRoot
        val diceBounds = composeRule.onNodeWithTag(ODD_EVEN_DICE_TAG)
            .fetchSemanticsNode().boundsInRoot
        val minGapPx = with(composeRule.density) { ODD_EVEN_CHOICE_DICE_MIN_GAP.toPx() }

        assertTrue(diceBounds.top - rowBounds.bottom >= minGapPx)
    }
}
