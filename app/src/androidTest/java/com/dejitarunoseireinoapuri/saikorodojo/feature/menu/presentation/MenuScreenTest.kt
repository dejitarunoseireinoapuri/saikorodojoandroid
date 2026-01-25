package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun menuTextUsesSourceCodeProFontFamily() {
        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                MenuScreen(
                    isDarkTheme = false,
                    onToggleTheme = {},
                    onPlayClick = {},
                    onRulesClick = {}
                )
            }
        }

        val expectedFont = MenuFontFamilyName
        val fontMatcher = SemanticsMatcher.expectValue(MenuFontFamilyKey, expectedFont)
        val context = composeRule.activity

        composeRule.onNodeWithText(context.getString(R.string.game_title)).assert(fontMatcher)
        composeRule.onNodeWithText(context.getString(R.string.play)).assert(fontMatcher)
        composeRule.onNodeWithText(context.getString(R.string.rules)).assert(fontMatcher)
    }
}
