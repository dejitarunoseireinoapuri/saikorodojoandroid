package com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val SEQUENCE_DICE_TAG = "sequence_dice"
internal const val SEQUENCE_DICE_VALUE_TAG = "sequence_dice_value"
internal const val SEQUENCE_SAVE_BUTTON_TAG = "sequence_save_button"
internal const val SEQUENCE_DISCARD_BUTTON_TAG = "sequence_discard_button"
internal const val SEQUENCE_CONTINUE_BUTTON_TAG = "sequence_continue_button"
internal const val SEQUENCE_SAVED_DIE_TAG_PREFIX = "sequence_saved_die"
internal const val SEQUENCE_SAVED_DIE_VALUE_TAG_PREFIX = "sequence_saved_die_value"
internal const val SEQUENCE_SAVED_MAT_TAG = "sequence_saved_mat"
internal const val SEQUENCE_REWARD_STACK_TAG = "sequence_reward_stack"

@Composable
fun SequenceGameRoute(
    modifier: Modifier = Modifier,
    viewModel: SequenceGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SequenceGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(SequenceGameUiEvent.StartGame) },
        onSaveClick = { viewModel.onEvent(SequenceGameUiEvent.SaveRoll) },
        onDiscardClick = { viewModel.onEvent(SequenceGameUiEvent.DiscardRoll) },
        onContinueClick = onContinueClick
    )
}

@Composable
fun SequenceGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: SequenceGameUiState,
    onStartClick: () -> Unit,
    onSaveClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    Box(
        modifier = containerModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.sequence_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasPendingReward = uiState.isComplete && uiState.pendingRewardCards.isNotEmpty()
            val hasLoss = uiState.isComplete && !hasReward && !hasPendingReward && uiState.isStarted
            val showRules = !uiState.isStarted
            val rulesModifier = if (showRules) {
                Modifier
            } else {
                Modifier.alpha(0f).clearAndSetSemantics { }
            }
            if (hasReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.minigame_win_message),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = VictoryMatBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.minigame_win_cards_message),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = VictoryMatBackground
                )
            } else if (hasLoss) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.minigame_lose_message),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = FailureMatBackground,
                    textAlign = TextAlign.Center
                )
            } else if (hasPendingReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.minigame_win_message),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = VictoryMatBackground
                )
            } else {
                Text(
                    text = stringResource(R.string.sequence_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = rulesModifier
                )
            }
            if (uiState.isStarted && !hasReward) {
                Spacer(modifier = Modifier.height(16.dp))
                val roundColor = if (uiState.isComplete &&
                    uiState.failureReason == SequenceFailureReason.ROUNDS
                ) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onBackground
                }
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRoll,
                        uiState.totalRolls
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = roundColor
                )
                Spacer(modifier = Modifier.height(40.dp))
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
            ) {
                Text(
                    text = stringResource(R.string.sequence_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        if (uiState.isStarted) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(72.dp))
                Box(
                    modifier = Modifier.height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isAwaitingDecision) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SequenceChoiceButton(
                                label = stringResource(R.string.sequence_save),
                                testTag = SEQUENCE_SAVE_BUTTON_TAG,
                                onClick = onSaveClick
                            )
                            SequenceChoiceButton(
                                label = stringResource(R.string.sequence_discard),
                                testTag = SEQUENCE_DISCARD_BUTTON_TAG,
                                onClick = onDiscardClick
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!uiState.isComplete) {
                        SequenceDiceFace(
                            value = uiState.diceValue,
                            size = 140.dp,
                            modifier = Modifier.testTag(SEQUENCE_DICE_TAG)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (uiState.rewardCards.isEmpty()) {
                    val matBackground = when {
                        uiState.failureReason != null -> FailureMatBackground
                        uiState.pendingRewardCards.isNotEmpty() -> VictoryMatBackground
                        else -> SequenceSaveMatBackground
                    }
                    val matBorder = when {
                        uiState.failureReason != null -> FailureMatBackground
                        uiState.pendingRewardCards.isNotEmpty() -> VictoryMatBackground
                        else -> SequenceSaveMatBorder
                    }
                    SequenceMat(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag(SEQUENCE_SAVED_MAT_TAG),
                        backgroundColor = matBackground,
                        borderColor = matBorder,
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val horizontalPadding = 16.dp
                            val spacing = 10.dp
                            val availableWidth = maxWidth - horizontalPadding * 2 - spacing * 2
                            val dieSize = (availableWidth / 3f).coerceAtMost(104.dp)
                            Row(
                                modifier = Modifier.padding(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.savedValues.forEach { value ->
                                    SequenceSavedDie(
                                        value = value,
                                        size = dieSize
                                    )
                                }
                                uiState.failureDieValue?.let { value ->
                                    SequenceSavedDie(
                                        value = value,
                                        size = dieSize
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
                    .testTag(SEQUENCE_REWARD_STACK_TAG)
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
                    .testTag(SEQUENCE_CONTINUE_BUTTON_TAG)
            ) {
                Text(
                    text = stringResource(R.string.odd_even_continue),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun SequenceChoiceButton(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        ),
        modifier = Modifier
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
private fun SequenceMat(
    modifier: Modifier,
    backgroundColor: Color,
    borderColor: Color,
    contentAlignment: Alignment = Alignment.Center,
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
private fun SequenceDiceFace(
    value: Int?,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val textOffsetPx = with(LocalDensity.current) { 6.dp.toPx() }
    Box(
        modifier = modifier
            .size(size)
            .background(SequenceSaveMatBackground, RoundedCornerShape(18.dp))
            .border(2.dp, SequenceSaveMatBorder, RoundedCornerShape(18.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value != null) {
            Image(
                painter = painterResource(id = R.drawable.eigth_sides),
                contentDescription = stringResource(R.string.cd_dice_face, value),
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .graphicsLayer { translationY = textOffsetPx }
                    .testTag(SEQUENCE_DICE_VALUE_TAG)
            )
        }
    }
}

@Composable
private fun SequenceSavedDie(
    value: Int,
    size: Dp
) {
    val diceRes = R.drawable.eigth_sides
    val textOffsetPx = with(LocalDensity.current) { 6.dp.toPx() }
    Box(
        modifier = Modifier
            .size(size)
            .testTag("${SEQUENCE_SAVED_DIE_TAG_PREFIX}_$value"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceRes),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .graphicsLayer { translationY = textOffsetPx }
                .testTag("${SEQUENCE_SAVED_DIE_VALUE_TAG_PREFIX}_$value")
        )
    }
}
