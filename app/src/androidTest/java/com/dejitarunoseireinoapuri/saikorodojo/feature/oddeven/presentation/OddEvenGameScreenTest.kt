package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.OddEvenFailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.OddEvenSuccessMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OddEvenGameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rewardStateHidesDiceAndShowsContinueButton() {
        val rewardCard = defaultCardUiModels().first()
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = true,
                        diceValue = 6,
                        rewardCards = listOf(rewardCard)
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(ODD_EVEN_CONTINUE_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun lossStateKeepsDiceVisibleAndShowsContinueButton() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = true,
                        diceValue = 4,
                        rewardCards = emptyList()
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ODD_EVEN_CONTINUE_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun diceMatUsesMainGameMatColor() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = false,
                        diceValue = null
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(SequenceSaveMatBackground, centerColor)
    }

    @Test
    fun successStateUsesGreenMatColor() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = false,
                        diceValue = null,
                        showFireworks = true
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(OddEvenSuccessMatBackground, centerColor)
    }

    @Test
    fun failureStateUsesRedMatColor() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = false,
                        diceValue = null,
                        showFailure = true
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(OddEvenFailureMatBackground, centerColor)
    }
}
