package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test

class MenuScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuScreenShowsTitleAndButtons() {
        composeTestRule.setContent {
            SaikoroDojoTheme {
                MenuScreen(
                    isDarkTheme = false,
                    onToggleTheme = {},
                    onPlayClick = {},
                    onRulesClick = {}
                )
            }
        }

        val title = composeTestRule.activity.getString(R.string.game_title)
        val play = composeTestRule.activity.getString(R.string.play)
        val rules = composeTestRule.activity.getString(R.string.rules)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(play).assertIsDisplayed()
        composeTestRule.onNodeWithText(rules).assertIsDisplayed()
    }
}
