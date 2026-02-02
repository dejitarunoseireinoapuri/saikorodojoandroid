package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
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

    @Test
    fun savedMatUsesFailureColorOnLoss() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = SequenceGameUiState(
                        isStarted = true,
                        isComplete = true,
                        failureReason = SequenceFailureReason.ORDER
                    ),
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(SEQUENCE_SAVED_MAT_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(FailureMatBackground, centerColor)
    }

    @Test
    fun savedMatUsesVictoryColorOnSuccess() {
        val pendingReward = CardUiModel(
            id = com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId.FLIP_FACE,
            titleRes = 0,
            descriptionRes = 0,
            iconRes = 0
        )
        composeTestRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = SequenceGameUiState(
                        isStarted = true,
                        isComplete = true,
                        pendingRewardCards = listOf(pendingReward)
                    ),
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(SEQUENCE_SAVED_MAT_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(VictoryMatBackground, centerColor)
    }

    @Test
    fun rulesTextIsHiddenAfterStart() {
        val subtitle = composeTestRule.activity.getString(R.string.sequence_subtitle)
        composeTestRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = SequenceGameUiState(
                        isStarted = true
                    ),
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
    }
}
