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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val ODD_EVEN_DICE_TAG = "odd_even_dice"
internal const val ODD_EVEN_CHOICE_ROW_TAG = "odd_even_choice_row"
internal const val ODD_EVEN_CONTINUE_BUTTON_TAG = "odd_even_continue_button"
internal const val ODD_EVEN_REWARD_STACK_TAG = "odd_even_reward_stack"
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
                text = stringResource(R.string.odd_even_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasLoss = uiState.isComplete && !hasReward && uiState.isStarted
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
                    color = FailureMatBackground
                )
            } else {
                Text(
                    text = stringResource(R.string.odd_even_subtitle),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = rulesModifier
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
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.odd_even_hits_status,
                            uiState.correctCount,
                            uiState.targetCorrect
                        ),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.testTag(ODD_EVEN_CHOICE_ROW_TAG),
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
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
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
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
                    isSuccess = uiState.showFireworks,
                    isFailure = uiState.showFailure,
                    modifier = Modifier.testTag(ODD_EVEN_DICE_TAG)
                )
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
                    .testTag(ODD_EVEN_REWARD_STACK_TAG)
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
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            disabledContentColor = MaterialTheme.colorScheme.onTertiary
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
    isSuccess: Boolean,
    isFailure: Boolean,
    modifier: Modifier = Modifier
) {
    val matBackground = when {
        isSuccess -> VictoryMatBackground
        isFailure -> FailureMatBackground
        else -> SequenceSaveMatBackground
    }
    val matBorder = when {
        isSuccess -> VictoryMatBackground
        isFailure -> FailureMatBackground
        else -> SequenceSaveMatBorder
    }
    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                shadowElevation = 12.dp.toPx()
                ambientShadowColor = matBorder
                spotShadowColor = matBorder
            }
            .background(matBackground, RoundedCornerShape(18.dp))
            .border(2.dp, matBorder, RoundedCornerShape(18.dp))
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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.offset(y = 0.dp)
            )
        }
    }
}
