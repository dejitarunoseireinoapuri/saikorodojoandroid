package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice

internal const val ODD_EVEN_DICE_TAG = "odd_even_dice"
internal const val ODD_EVEN_CHOICE_ROW_TAG = "odd_even_choice_row"
internal const val ODD_EVEN_CONTINUE_BUTTON_TAG = "odd_even_continue_button"
internal val ODD_EVEN_DICE_SIZE = 150.dp

@Composable
fun OddEvenGameRoute(
    modifier: Modifier = Modifier,
    viewModel: OddEvenGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OddEvenGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(OddEvenGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(choice))
        },
        onContinueClick = onContinueClick
    )
}

@Composable
fun OddEvenGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: OddEvenGameUiState,
    onStartClick: () -> Unit,
    onChoiceSelect: (OddEvenChoice) -> Unit,
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
                text = stringResource(R.string.odd_even_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasLoss = uiState.isComplete && !hasReward && uiState.isStarted
            if (hasReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.odd_even_congrats),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = Color(0xFFFFF176)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.odd_even_reward_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White
                )
            } else if (hasLoss) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.odd_even_try_again),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = Color(0xFFFFF176)
                )
            } else {
                Text(
                    text = stringResource(R.string.odd_even_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                if (uiState.isStarted) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = stringResource(
                            R.string.odd_even_round_status,
                            uiState.currentRound,
                            uiState.totalRounds
                        ),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.odd_even_hits_status,
                            uiState.correctCount,
                            uiState.targetCorrect
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.testTag(ODD_EVEN_CHOICE_ROW_TAG),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.ODD,
                            label = stringResource(R.string.odd_even_even),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = { onChoiceSelect(OddEvenChoice.EVEN) }
                        )
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.EVEN,
                            label = stringResource(R.string.odd_even_odd),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = { onChoiceSelect(OddEvenChoice.ODD) }
                        )
                    }
                }
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
                    text = stringResource(R.string.odd_even_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        if (uiState.isStarted && uiState.rewardCards.isEmpty() && !uiState.isComplete) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 120.dp)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OddEvenDiceFace(
                    value = uiState.diceValue,
                    size = ODD_EVEN_DICE_SIZE,
                    modifier = Modifier.testTag(ODD_EVEN_DICE_TAG)
                )
                val resultTextRes = when {
                    uiState.showFireworks -> R.string.odd_even_correct
                    uiState.showFailure -> R.string.odd_even_wrong
                    else -> null
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (resultTextRes != null) {
                        Text(
                            text = stringResource(resultTextRes),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            modifier = Modifier.alpha(0f)
                        )
                    }
                }
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    containerColor = Color(0xFF26C6DA),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .zIndex(4f)
                    .testTag(ODD_EVEN_CONTINUE_BUTTON_TAG)
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
private fun OddEvenChoiceButton(
    visible: Boolean,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    if (!visible) return
    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF1744),
            contentColor = Color.White,
            disabledContentColor = Color(0xFFFFF8E1)
        ),
        modifier = Modifier.height(56.dp)
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
private fun OddEvenDiceFace(
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
            .graphicsLayer {
                shadowElevation = 12.dp.toPx()
                ambientShadowColor = Color(0xFF4E2A00)
                spotShadowColor = Color(0xFF4E2A00)
            }
            .background(frameBrush, RoundedCornerShape(18.dp))
            .border(2.dp, Color(0xFFFFD54F), RoundedCornerShape(18.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (value != null) {
            Image(
                painter = painterResource(id = R.drawable.six_sides),
                contentDescription = stringResource(R.string.cd_dice_face, value),
                modifier = Modifier.fillMaxSize()
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.offset(y = 0.dp)
            )
        }
    }
}
