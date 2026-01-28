package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
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
                        rewardCard = rewardCard
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
                        rewardCard = null
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
}
