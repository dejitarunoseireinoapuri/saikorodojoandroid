package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SequenceGameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun activeRollShowsCenteredDiceValueAndSavedValues() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = SequenceGameUiState(
                        isStarted = true,
                        isRolling = false,
                        isAwaitingDecision = true,
                        currentRoll = 2,
                        savedValues = listOf(2, 5),
                        diceValue = 6
                    ),
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(SEQUENCE_DICE_VALUE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag("${SEQUENCE_SAVED_DIE_VALUE_TAG_PREFIX}_2").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${SEQUENCE_SAVED_DIE_VALUE_TAG_PREFIX}_5").assertIsDisplayed()
    }

    @Test
    fun diceMatUsesMainGameMatColor() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = SequenceGameUiState(
                        isStarted = true,
                        isComplete = false,
                        diceValue = null
                    ),
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(SEQUENCE_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(SequenceSaveMatBackground, centerColor)
    }
}
