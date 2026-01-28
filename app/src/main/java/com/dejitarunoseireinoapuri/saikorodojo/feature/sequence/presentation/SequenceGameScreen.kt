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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem

internal const val SEQUENCE_DICE_TAG = "sequence_dice"
internal const val SEQUENCE_SAVE_BUTTON_TAG = "sequence_save_button"
internal const val SEQUENCE_DISCARD_BUTTON_TAG = "sequence_discard_button"
internal const val SEQUENCE_CONTINUE_BUTTON_TAG = "sequence_continue_button"

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
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1F2335),
            Color(0xFF2E2A6B),
            Color(0xFF2B5B8A)
        )
    )
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(backgroundBrush)
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
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val hasReward = uiState.rewardCard != null
            val hasPendingReward = uiState.isComplete && uiState.pendingRewardCard != null
            val hasLoss = uiState.isComplete && !hasReward && uiState.isStarted
            if (hasReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.sequence_congrats),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = Color(0xFFFFF176)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sequence_reward_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White
                )
            } else if (hasLoss) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.odd_even_try_again),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = Color(0xFFFFF176),
                    textAlign = TextAlign.Center
                )
            } else if (hasPendingReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.sequence_congrats),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = Color(0xFFFFF176)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.sequence_reward_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White
                )
            } else {
                Text(
                    text = stringResource(R.string.sequence_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
            if (uiState.isStarted) {
                Spacer(modifier = Modifier.height(16.dp))
                val roundColor = if (uiState.isComplete &&
                    uiState.failureReason == SequenceFailureReason.ROUNDS
                ) {
                    Color(0xFFFF1744)
                } else {
                    Color.White
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!uiState.isStarted) {
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1744),
                    contentColor = Color.White
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
                Spacer(modifier = Modifier.height(56.dp))
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
                if (uiState.rewardCard == null) {
                    SequenceMat(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        backgroundColor = Color(0xFF00695C),
                        borderColor = Color(0xFF80CBC4),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val horizontalPadding = 20.dp
                            val spacing = 12.dp
                            val availableWidth = maxWidth - horizontalPadding * 2 - spacing * 2
                            val dieSize = (availableWidth / 3f).coerceAtMost(96.dp)
                            Row(
                                modifier = Modifier.padding(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.savedValues.forEach { value ->
                                    SequenceSavedDie(
                                        value = value,
                                        size = dieSize,
                                        isFailure = false
                                    )
                                }
                                uiState.failureDieValue?.let { value ->
                                    SequenceSavedDie(
                                        value = value,
                                        size = dieSize,
                                        isFailure = uiState.failureReason == SequenceFailureReason.ORDER
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        uiState.rewardCard?.let { reward ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                CardItem(
                    card = reward,
                    onApplyClick = {},
                    showActionButton = false,
                    showCount = false
                )
            }
        }

        if (uiState.isComplete && uiState.isStarted) {
            Button(
                onClick = onContinueClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26C6DA),
                    contentColor = Color.White
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
            containerColor = Color(0xFFFF1744),
            contentColor = Color.White
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
    val frameBrush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFF59D), Color(0xFFFF6F00))
    )
    Box(
        modifier = modifier
            .size(size)
            .background(frameBrush, RoundedCornerShape(18.dp))
            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(18.dp))
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
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SequenceSavedDie(
    value: Int,
    size: Dp,
    isFailure: Boolean
) {
    val diceRes = if (isFailure) {
        R.drawable.eigth_sides_red
    } else {
        R.drawable.eigth_sides
    }
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceRes),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
            color = Color.White,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
