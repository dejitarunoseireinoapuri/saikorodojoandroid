package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardCardStackTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun tappingOutsideExpandedCardReturnsItToStack() {
        val cards = defaultCardUiModels().take(3).map { it.copy(count = 1) }
        val firstCardTag = "${REWARD_CARD_TAG_PREFIX}_0"
        val lastCardTag = "${REWARD_CARD_TAG_PREFIX}_2"

        composeTestRule.setContent {
            SaikoroDojoTheme {
                RewardCardStack(cards = cards)
            }
        }

        composeTestRule.onNodeWithTag(lastCardTag)
            .assert(SemanticsMatcher.expectValue(RewardCardExpandedKey, true))

        composeTestRule.onNodeWithTag(firstCardTag).performClick()

        composeTestRule.onNodeWithTag(firstCardTag)
            .assert(SemanticsMatcher.expectValue(RewardCardExpandedKey, true))

        composeTestRule.onNodeWithTag(REWARD_CARD_STACK_TAG).performClick()

        composeTestRule.onNodeWithTag(firstCardTag)
            .assert(SemanticsMatcher.expectValue(RewardCardExpandedKey, false))
        composeTestRule.onNodeWithTag(lastCardTag)
            .assert(SemanticsMatcher.expectValue(RewardCardExpandedKey, true))
    }
}
