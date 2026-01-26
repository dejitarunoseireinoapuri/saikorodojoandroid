package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun gameTitleRespectsProvidedContentPadding() {
        val topPadding = 32.dp
        val uiState = GameUiState(
            diceValues = listOf(1, 2, 3, 4, 5),
            diceCount = 5,
            diceType = DiceType.D6,
            diceTypes = listOf(
                DiceType.D6,
                DiceType.D6,
                DiceType.D6,
                DiceType.D6,
                DiceType.D6
            ),
            selectedDiceSum = 0
        )
        val expectedText = composeRule.activity.getString(R.string.game_title)

        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                GameScreen(
                    contentPadding = PaddingValues(top = topPadding),
                    applySystemBarsPadding = false,
                    uiState = uiState,
                    onDiceClick = {},
                    onCardSelect = {},
                    onCardDismiss = {},
                    onCardApply = {}
                )
            }
        }

        composeRule.onNodeWithText(expectedText)
            .assertTopPositionInRootIsEqualTo(topPadding + 20.dp)
    }

    @Test
    fun selectedDiceSumIsNotDisplayed() {
        val uiState = GameUiState(
            diceValues = listOf(1, 2, 3, 4, 5),
            diceCount = 5,
            diceType = DiceType.D6,
            diceTypes = listOf(
                DiceType.D6,
                DiceType.D6,
                DiceType.D6,
                DiceType.D6,
                DiceType.D6
            ),
            selectedDiceSum = 12
        )
        val sumText = composeRule.activity.getString(R.string.selected_dice_sum, 12)

        composeRule.setContent {
            SaikoroDojoTheme(darkTheme = false, dynamicColor = false) {
                GameScreen(
                    applySystemBarsPadding = false,
                    uiState = uiState,
                    onDiceClick = {},
                    onCardSelect = {},
                    onCardDismiss = {},
                    onCardApply = {}
                )
            }
        }

        composeRule.onNodeWithText(sumText)
            .assertDoesNotExist()
    }
}
