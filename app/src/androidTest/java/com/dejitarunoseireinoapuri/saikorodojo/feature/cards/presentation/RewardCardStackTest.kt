package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
    fun clickingBackgroundResetsExpandedCard() {
        val cards = defaultCardUiModels().take(2)
        val firstDescription = composeTestRule.activity.getString(cards.first().descriptionRes)
        val secondDescription = composeTestRule.activity.getString(cards.last().descriptionRes)

        composeTestRule.setContent {
            SaikoroDojoTheme {
                RewardCardStack(cards = cards)
            }
        }

        composeTestRule.onNodeWithText(secondDescription).assertIsDisplayed()

        composeTestRule.onNodeWithTag(RewardCardStackTestTags.card(0)).performClick()

        composeTestRule.onNodeWithText(firstDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(secondDescription).assertDoesNotExist()

        composeTestRule.onNodeWithTag(RewardCardStackTestTags.Background).performClick()

        composeTestRule.onNodeWithText(secondDescription).assertIsDisplayed()
        composeTestRule.onNodeWithText(firstDescription).assertDoesNotExist()
    }
}
