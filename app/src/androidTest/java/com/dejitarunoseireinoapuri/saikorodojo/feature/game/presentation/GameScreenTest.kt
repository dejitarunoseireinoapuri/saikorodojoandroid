package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
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
    fun gameTitleIsNotDisplayed() {
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
        val titleText = composeRule.activity.getString(R.string.game_title)

        composeRule.setContent {
            SaikoroDojoTheme {
                GameScreen(
                    applySystemBarsPadding = false,
                    uiState = uiState,
                    onDiceClick = {},
                    onCardSelect = {},
                    onCardDismiss = {},
                    onCardApply = {},
                    onAdjustSelectedDie = {},
                    onSetSelectedDieValue = {},
                    onRollSelectedDice = {},
                    onRollSingleDie = {},
                    onFlipSelectedDie = {},
                    onConfirmSurrender = {},
                    onConfirmExit = {},
                    onOpenRandomMinigame = {},
                    onConfirmMinigamesAd = {},
                    onDismissMinigamesAdPrompt = {}
                )
            }
        }

        composeRule.onAllNodesWithText(titleText).assertCountEquals(0)
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
            SaikoroDojoTheme {
                GameScreen(
                    applySystemBarsPadding = false,
                    uiState = uiState,
                    onDiceClick = {},
                    onCardSelect = {},
                    onCardDismiss = {},
                    onCardApply = {},
                    onAdjustSelectedDie = {},
                    onSetSelectedDieValue = {},
                    onRollSelectedDice = {},
                    onRollSingleDie = {},
                    onFlipSelectedDie = {},
                    onConfirmSurrender = {},
                    onConfirmExit = {},
                    onOpenRandomMinigame = {},
                    onConfirmMinigamesAd = {},
                    onDismissMinigamesAdPrompt = {}
                )
            }
        }

        composeRule.onAllNodesWithText(sumText).assertCountEquals(0)
    }
}
