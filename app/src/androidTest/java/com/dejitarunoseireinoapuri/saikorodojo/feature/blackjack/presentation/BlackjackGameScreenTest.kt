package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BlackjackGameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun playerLossUsesFailureMatBackground() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        playerDice = listOf(2, 3),
                        dealerDice = listOf(6),
                        result = BlackjackOutcome.PLAYER_LOSE
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(BLACKJACK_PLAYER_MAT_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(FailureMatBackground, centerColor)
    }

    @Test
    fun playerWinUsesVictoryMatBackground() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        playerDice = listOf(4, 5),
                        dealerDice = listOf(6),
                        result = BlackjackOutcome.PLAYER_WIN
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(BLACKJACK_PLAYER_MAT_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(VictoryMatBackground, centerColor)
    }

    @Test
    fun rulesTextIsHiddenAfterStart() {
        val subtitle = composeTestRule.activity.getString(R.string.blackjack_subtitle)
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        composeTestRule.onAllNodesWithText(subtitle).assertCountEquals(0)
    }


    @Test
    fun playerWinKeepsDealerMatDefaultBackground() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        playerDice = listOf(4, 5),
                        dealerDice = listOf(6),
                        result = BlackjackOutcome.PLAYER_WIN
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val image = composeTestRule.onNodeWithTag(BLACKJACK_DEALER_MAT_TAG).captureToImage()
        val pixelMap = image.toPixelMap()
        val centerColor = pixelMap[pixelMap.width / 2, pixelMap.height / 2]
        assertEquals(SequenceSaveMatBackground, centerColor)
    }


    @Test
    fun completedLossKeepsBothMatsVisible() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        isComplete = true,
                        playerDice = listOf(4, 5),
                        dealerDice = listOf(6, 6),
                        result = BlackjackOutcome.PLAYER_LOSE
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        composeTestRule.onAllNodesWithTag(BLACKJACK_DEALER_MAT_TAG).assertCountEquals(1)
        composeTestRule.onAllNodesWithTag(BLACKJACK_PLAYER_MAT_TAG).assertCountEquals(1)
    }

    @Test
    fun awaitingDecisionShowsStandOnLeftAndHitOnRightWithSameSize() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        isAwaitingDecision = true,
                        playerDice = listOf(4, 5),
                        dealerDice = listOf(6)
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val standNode = composeTestRule.onNodeWithTag(BLACKJACK_STAND_BUTTON_TAG).fetchSemanticsNode()
        val hitNode = composeTestRule.onNodeWithTag(BLACKJACK_HIT_BUTTON_TAG).fetchSemanticsNode()

        assertTrue(standNode.boundsInRoot.left < hitNode.boundsInRoot.left)
        assertEquals(standNode.boundsInRoot.width, hitNode.boundsInRoot.width)
        assertEquals(standNode.boundsInRoot.height, hitNode.boundsInRoot.height)
    }

    @Test
    fun rewardStackIsOffsetDownward() {
        val rewardCard = CardUiModel(
            id = com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId.FLIP_FACE,
            titleRes = 0,
            descriptionRes = 0,
            iconRes = 0
        )
        composeTestRule.setContent {
            SaikoroDojoTheme {
                BlackjackGameScreen(
                    uiState = BlackjackGameUiState(
                        isStarted = true,
                        isComplete = true,
                        rewardCards = listOf(rewardCard)
                    ),
                    onStartClick = {},
                    onHitClick = {},
                    onStandClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val node = composeTestRule.onNodeWithTag(BLACKJACK_REWARD_STACK_TAG)
            .fetchSemanticsNode()
        val expectedOffset = with(composeTestRule.density) { 32.dp.toPx() }
        assertTrue(node.boundsInRoot.top >= expectedOffset)
    }
}
