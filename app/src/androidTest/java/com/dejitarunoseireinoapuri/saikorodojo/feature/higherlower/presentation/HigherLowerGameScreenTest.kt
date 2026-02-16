package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HigherLowerGameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rulesTextIsHiddenAfterStart() {
        val subtitle = composeTestRule.activity.getString(R.string.rules_minigame_higher_lower_body)
        composeTestRule.setContent {
            SaikoroDojoTheme {
                HigherLowerGameScreen(
                    uiState = HigherLowerGameUiState(
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
        val rewardCard = CardUiModel(
            id = com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId.FLIP_FACE,
            titleRes = R.string.card_flip_face_title,
            descriptionRes = R.string.card_flip_face_description,
            iconRes = R.drawable.ic_card_flip
        )
        composeTestRule.setContent {
            SaikoroDojoTheme {
                HigherLowerGameScreen(
                    uiState = HigherLowerGameUiState(
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

        val node = composeTestRule.onNodeWithTag(HIGHER_LOWER_REWARD_STACK_TAG)
            .fetchSemanticsNode()
        val expectedOffset = with(composeTestRule.density) { 32.dp.toPx() }
        assertTrue(node.boundsInRoot.top >= expectedOffset)
    }
}
