package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test

class HigherLowerGameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rulesTextIsHiddenAfterStart() {
        val subtitle = composeTestRule.activity.getString(R.string.higher_lower_subtitle)
        composeTestRule.setContent {
            SaikoroDojoTheme {
                HigherLowerGameScreen(
                    uiState = HigherLowerGameUiState(
                        isStarted = true
                    ),
                    onStartClick = {},
                    onChoiceSelect = {},
                    onContinueClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(subtitle).assertDoesNotExist()
    }
}
