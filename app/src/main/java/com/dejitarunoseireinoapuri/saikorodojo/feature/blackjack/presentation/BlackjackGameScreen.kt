package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val BLACKJACK_HIT_BUTTON_TAG = "blackjack_hit_button"
internal const val BLACKJACK_STAND_BUTTON_TAG = "blackjack_stand_button"
internal const val BLACKJACK_START_BUTTON_TAG = "blackjack_start_button"
internal const val BLACKJACK_DEALER_MAT_TAG = "blackjack_dealer_mat"
internal const val BLACKJACK_PLAYER_MAT_TAG = "blackjack_player_mat"
internal const val BLACKJACK_REWARD_STACK_TAG = "blackjack_reward_stack"

@Composable
fun BlackjackGameRoute(
    modifier: Modifier = Modifier,
    viewModel: BlackjackGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BlackjackGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(BlackjackGameUiEvent.StartGame) },
        onHitClick = { viewModel.onEvent(BlackjackGameUiEvent.Hit) },
        onStandClick = { viewModel.onEvent(BlackjackGameUiEvent.Stand) },
        onContinueClick = onContinueClick
    )
}

@Composable
fun BlackjackGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: BlackjackGameUiState,
    onStartClick: () -> Unit,
    onHitClick: () -> Unit,
    onStandClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    Box(modifier = containerModifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.blackjack_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val showEndState = uiState.isComplete
            val resultTextRes = when (uiState.result) {
                BlackjackOutcome.PLAYER_WIN -> R.string.minigame_win_message
                BlackjackOutcome.PLAYER_LOSE -> R.string.minigame_lose_message
                null -> null
            }
            val resultTextColor = blackjackResultTextColor(
                result = uiState.result,
                defaultColor = MaterialTheme.colorScheme.primary
            )
            val hasReward = uiState.rewardCards.isNotEmpty()
            val showRules = !uiState.isStarted
            val rulesModifier = if (showRules) {
                Modifier
            } else {
                Modifier.alpha(0f).clearAndSetSemantics { }
            }
            when {
                hasReward -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_message),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = VictoryMatBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.blackjack_dealer_score,
                            uiState.dealerTotal
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.blackjack_player_score,
                            uiState.playerTotal
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_cards_message),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                resultTextRes != null && showEndState -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(resultTextRes),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = resultTextColor,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.blackjack_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = rulesModifier
                    )
                }
            }
        }

        if (!uiState.isStarted) {
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(64.dp)
                    .testTag(BLACKJACK_START_BUTTON_TAG)
            ) {
                Text(
                    text = stringResource(R.string.blackjack_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        if (uiState.isStarted && uiState.rewardCards.isEmpty() && (!uiState.isComplete || uiState.result == BlackjackOutcome.PLAYER_LOSE)) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))
                ScoreLabel(
                    text = stringResource(R.string.blackjack_dealer_score, uiState.dealerTotal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BlackjackMat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag(BLACKJACK_DEALER_MAT_TAG),
                    contentAlignment = Alignment.Center,
                    backgroundColor = SequenceSaveMatBackground,
                    borderColor = SequenceSaveMatBorder
                ) {
                    DiceRow(
                        values = uiState.dealerDice
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (uiState.isAwaitingDecision) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BlackjackActionButton(
                            label = stringResource(R.string.blackjack_stand),
                            testTag = BLACKJACK_STAND_BUTTON_TAG,
                            onClick = onStandClick,
                            modifier = Modifier.weight(1f)
                        )
                        BlackjackActionButton(
                            label = stringResource(R.string.blackjack_hit),
                            testTag = BLACKJACK_HIT_BUTTON_TAG,
                            onClick = onHitClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(56.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                ScoreLabel(
                    text = stringResource(R.string.blackjack_player_score, uiState.playerTotal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BlackjackMat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag(BLACKJACK_PLAYER_MAT_TAG),
                    contentAlignment = Alignment.Center,
                    backgroundColor = when (uiState.result) {
                        BlackjackOutcome.PLAYER_WIN -> VictoryMatBackground
                        BlackjackOutcome.PLAYER_LOSE -> FailureMatBackground
                        null -> SequenceSaveMatBackground
                    },
                    borderColor = when (uiState.result) {
                        BlackjackOutcome.PLAYER_WIN -> VictoryMatBackground
                        BlackjackOutcome.PLAYER_LOSE -> FailureMatBackground
                        null -> SequenceSaveMatBorder
                    }
                ) {
                    DiceRow(
                        values = uiState.playerDice
                    )
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .testTag(BLACKJACK_REWARD_STACK_TAG)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                RewardCardStack(cards = uiState.rewardCards)
            }
        }

        if (uiState.isComplete && uiState.isStarted) {
            Button(
                onClick = onContinueClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .zIndex(4f)
            ) {
                Text(
                    text = stringResource(R.string.blackjack_continue),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

internal fun blackjackResultTextColor(
    result: BlackjackOutcome?,
    defaultColor: Color
): Color {
    return when (result) {
        BlackjackOutcome.PLAYER_LOSE -> FailureMatBackground
        BlackjackOutcome.PLAYER_WIN -> VictoryMatBackground
        null -> defaultColor
    }
}

@Composable
private fun ScoreLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BlackjackActionButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = modifier
            .height(56.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun BlackjackMat(
    modifier: Modifier,
    contentAlignment: Alignment,
    backgroundColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(8.dp),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
private fun DiceRow(
    values: List<Int>
) {
    if (values.isEmpty()) return
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val maxPerRow = 5
        val rows = values.chunked(maxPerRow)
        val rowCount = rows.size.coerceAtLeast(1)
        val spacing = 10.dp
        val rowSpacing = 8.dp
        val horizontalPadding = 12.dp
        val verticalPadding = 8.dp
        val availableWidth = maxWidth - horizontalPadding * 2
        val availableHeight = maxHeight - verticalPadding * 2 - rowSpacing * (rowCount - 1)
        val columns = rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val widthBasedSize =
            (availableWidth - spacing * (columns - 1)).coerceAtLeast(0.dp) / columns
        val heightBasedSize =
            (availableHeight / rowCount.coerceAtLeast(1)).coerceAtLeast(0.dp)
        val diceSize = minOf(widthBasedSize, heightBasedSize)
            .coerceAtMost(96.dp)
            .coerceAtLeast(32.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowValues ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowValues.forEach { value ->
                        BlackjackDieFace(
                            value = value,
                            size = diceSize.coerceAtMost(96.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlackjackDieFace(
    value: Int,
    size: Dp
) {
    val fontSize = (size.value * 0.32f).coerceIn(14f, 22f).sp
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ten_sides),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
