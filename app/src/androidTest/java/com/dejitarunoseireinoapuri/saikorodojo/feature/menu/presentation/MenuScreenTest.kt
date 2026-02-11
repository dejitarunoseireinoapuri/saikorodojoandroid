package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.OxaniunFontFamily
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.unit.dp

@RunWith(AndroidJUnit4::class)
class MenuScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun themeUsesOxaniunFontFamily() {
        var typographyFontFamily = OxaniunFontFamily

        composeRule.setContent {
            SaikoroDojoTheme {
                typographyFontFamily = requireNotNull(MaterialTheme.typography.bodyLarge.fontFamily)
            }
        }

        composeRule.runOnIdle {
            assertNotNull(typographyFontFamily)
            assertEquals(OxaniunFontFamily, typographyFontFamily)
        }
    }

    @Test
    fun topAppBarRespectsProvidedContentPadding() {
        val topPadding = 32.dp

        composeRule.setContent {
            SaikoroDojoTheme {
                MenuScreen(
                    contentPadding = PaddingValues(top = topPadding),
                    applySystemBarsPadding = false,
                    showContinueDialog = false,
                    isSoundEnabled = true,
                    onPlayClick = {},
                    onRulesClick = {},
                    onContinueGame = {},
                    onStartNewGame = {},
                    onDismissDialog = {},
                    onSoundToggleClick = {},
                    onSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithTag(MENU_TOP_APP_BAR_TAG)
            .assertTopPositionInRootIsEqualTo(topPadding)
    }

    @Test
    fun menuButtonsUseThickerHeight() {
        val expectedHeight = 64.dp

        composeRule.setContent {
            SaikoroDojoTheme {
                MenuScreen(
                    applySystemBarsPadding = false,
                    showContinueDialog = false,
                    isSoundEnabled = true,
                    onPlayClick = {},
                    onRulesClick = {},
                    onContinueGame = {},
                    onStartNewGame = {},
                    onDismissDialog = {},
                    onSoundToggleClick = {},
                    onSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithTag(MENU_PLAY_BUTTON_TAG)
            .assertHeightIsEqualTo(expectedHeight)
        composeRule.onNodeWithTag(MENU_RULES_BUTTON_TAG)
            .assertHeightIsEqualTo(expectedHeight)
    }

    @Test
    fun menuDoesNotShowTopTitleText() {
        composeRule.setContent {
            SaikoroDojoTheme {
                MenuScreen(
                    applySystemBarsPadding = false,
                    showContinueDialog = false,
                    isSoundEnabled = true,
                    onPlayClick = {},
                    onRulesClick = {},
                    onContinueGame = {},
                    onStartNewGame = {},
                    onDismissDialog = {},
                    onSoundToggleClick = {},
                    onSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.game_title))
            .assertDoesNotExist()
    }

    @Test
    fun menuShowsSaikoroDojoDieImage() {
        composeRule.setContent {
            SaikoroDojoTheme {
                MenuScreen(
                    applySystemBarsPadding = false,
                    showContinueDialog = false,
                    isSoundEnabled = true,
                    onPlayClick = {},
                    onRulesClick = {},
                    onContinueGame = {},
                    onStartNewGame = {},
                    onDismissDialog = {},
                    onSoundToggleClick = {},
                    onSettingsClick = {}
                )
            }
        }

        composeRule.onNodeWithTag(MENU_DIE_IMAGE_TAG)
            .assertIsDisplayed()
    }
}
