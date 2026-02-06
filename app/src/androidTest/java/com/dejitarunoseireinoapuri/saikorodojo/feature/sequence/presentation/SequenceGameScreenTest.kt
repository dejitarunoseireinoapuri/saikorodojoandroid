package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SequenceGameScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun decisionButtonsStayBetweenRoundStatusAndMat() {
        val uiState = SequenceGameUiState(
            isStarted = true,
            isAwaitingDecision = true,
            currentRoll = 1,
            totalRolls = 5,
            diceValue = 4
        )

        composeRule.setContent {
            SaikoroDojoTheme {
                SequenceGameScreen(
                    uiState = uiState,
                    onStartClick = {},
                    onSaveClick = {},
                    onDiscardClick = {},
                    onContinueClick = {},
                    onExitToMenu = {}
                )
            }
        }

        val roundBounds = composeRule.onNodeWithTag(SEQUENCE_ROUND_STATUS_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val decisionBounds = composeRule.onNodeWithTag(SEQUENCE_DECISION_ROW_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
        val matBounds = composeRule.onNodeWithTag(SEQUENCE_SAVED_MAT_TAG)
            .fetchSemanticsNode()
            .boundsInRoot

        assertTrue(
            "Decision buttons should render below the round status.",
            decisionBounds.top > roundBounds.bottom
        )
        assertTrue(
            "Decision buttons should render above the saved mat.",
            decisionBounds.bottom < matBounds.top
        )
    }
}
