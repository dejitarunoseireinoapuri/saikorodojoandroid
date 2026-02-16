package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

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
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        composeTestRule.onAllNodesWithTag(ODD_EVEN_DICE_TAG).assertCountEquals(0)
        composeTestRule.onNodeWithTag(ODD_EVEN_CONTINUE_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun lossStateShowsDiceAndContinueButton() {
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
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        composeTestRule.onAllNodesWithTag(ODD_EVEN_DICE_TAG).assertCountEquals(1)
        composeTestRule.onNodeWithTag(ODD_EVEN_CONTINUE_BUTTON_TAG).assertIsDisplayed()
    }



    @Test
    fun diceStaysCenteredInPlayingAndLossStates() {
        var uiState by mutableStateOf(
            OddEvenGameUiState(
                isStarted = true,
                isComplete = false,
                currentRound = 1,
                totalRounds = 3
            )
        )

        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = uiState,
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val rootCenterYInPlaying = composeTestRule.onRoot().fetchSemanticsNode().boundsInRoot.center.y
        val diceCenterYInPlaying = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y

        composeTestRule.runOnIdle {
            uiState = uiState.copy(
                isComplete = true,
                diceValue = 4,
                rewardCards = emptyList()
            )
        }

        val rootCenterYInLoss = composeTestRule.onRoot().fetchSemanticsNode().boundsInRoot.center.y
        val diceCenterYInLoss = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
            .center
            .y

        val tolerance = with(composeTestRule.density) { 2.dp.toPx() }
        assertTrue(abs(diceCenterYInPlaying - rootCenterYInPlaying) <= tolerance)
        assertTrue(abs(diceCenterYInLoss - rootCenterYInLoss) <= tolerance)
    }

    @Test
    fun choicesAndStatusRenderAboveDiceInRequestedOrder() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        currentRound = 1,
                        totalRounds = 3,
                        correctCount = 1,
                        targetCorrect = 3
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val roundBounds = composeTestRule.onNodeWithTag(ODD_EVEN_ROUND_STATUS_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val hitsBounds = composeTestRule.onNodeWithTag(ODD_EVEN_HITS_STATUS_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val choiceBounds = composeTestRule.onNodeWithTag(ODD_EVEN_CHOICE_ROW_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val diceBounds = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(roundBounds.bottom < hitsBounds.top)
        assertTrue(hitsBounds.bottom < choiceBounds.top)
        assertTrue(choiceBounds.bottom < diceBounds.top)
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
                    onContinueClick = {},
                    onExitToMenu = {}
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
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(VictoryMatBackground, centerColor)
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
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(ODD_EVEN_DICE_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(FailureMatBackground, centerColor)
    }

    @Test
    fun rulesTextIsHiddenAfterStart() {
        val subtitle = composeTestRule.activity.getString(R.string.rules_minigame_odd_even_body)
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        composeTestRule.onAllNodesWithText(subtitle).assertCountEquals(0)
    }

    @Test
    fun rewardStackIsOffsetDownward() {
        val rewardCard = defaultCardUiModels().first()
        composeTestRule.setContent {
            SaikoroDojoTheme {
                OddEvenGameScreen(
                    uiState = OddEvenGameUiState(
                        isStarted = true,
                        isComplete = true,
                        rewardCards = listOf(rewardCard)
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val node = composeTestRule.onNodeWithTag(ODD_EVEN_REWARD_STACK_TAG)
            .fetchSemanticsNode()
        val expectedOffset = with(composeTestRule.density) { 32.dp.toPx() }
        assertTrue(node.boundsInRoot.top >= expectedOffset)
    }
}
