package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.defaultMinSize
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameMessageType
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameMessageColor
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val HIGHER_LOWER_BUTTON_ROW_TAG = "higher_lower_button_row"
internal const val HIGHER_LOWER_MAT_ROW_TAG = "higher_lower_mat_row"
internal const val HIGHER_LOWER_CONTINUE_BUTTON_TAG = "higher_lower_continue_button"
internal const val HIGHER_LOWER_REWARD_STACK_TAG = "higher_lower_reward_stack"
private const val HIGHER_LOWER_TRANSITION_MS = 900
private val HigherLowerButtonReserveHeight = 140.dp
private val HigherLowerChoiceButtonHeight = 64.dp
private val HigherLowerChoiceButtonMinWidth = 160.dp

@Composable
fun HigherLowerGameRoute(
    modifier: Modifier = Modifier,
    viewModel: HigherLowerGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HigherLowerGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(HigherLowerGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(choice))
        },
        onContinueClick = onContinueClick
    )
}

@Composable
fun HigherLowerGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: HigherLowerGameUiState,
    onStartClick: () -> Unit,
    onChoiceSelect: (HigherLowerChoice) -> Unit,
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
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.higher_lower_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val titleColor = MaterialTheme.colorScheme.onBackground
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasLoss = uiState.hasLoss && uiState.isComplete
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
                        color = minigameMessageColor(
                            MinigameMessageType.Win,
                            titleColor = titleColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_cards_message),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.WinCards,
                            titleColor = titleColor
                        )
                    )
                }
                hasLoss -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.minigame_lose_message),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.Lose,
                            titleColor = titleColor
                        ),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.higher_lower_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = titleColor,
                        textAlign = TextAlign.Center,
                        modifier = rulesModifier
                    )
                }
            }
            if (uiState.isStarted && !uiState.isComplete) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRound,
                        uiState.totalRounds
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (shouldShowHigherLowerChoiceRow(uiState.isChoiceVisible, uiState.selectedChoice)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(HIGHER_LOWER_BUTTON_ROW_TAG)
                    ) {
                        HigherLowerChoiceButton(
                            label = stringResource(R.string.higher_lower_lower),
                            isEnabled = uiState.selectedChoice == null,
                            isVisible = uiState.selectedChoice != HigherLowerChoice.HIGHER,
                            onClick = { onChoiceSelect(HigherLowerChoice.LOWER) },
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        HigherLowerChoiceButton(
                            label = stringResource(R.string.higher_lower_higher),
                            isEnabled = uiState.selectedChoice == null,
                            isVisible = uiState.selectedChoice != HigherLowerChoice.LOWER,
                            onClick = { onChoiceSelect(HigherLowerChoice.HIGHER) },
                            modifier = Modifier.align(Alignment.CenterEnd)
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
                    text = stringResource(R.string.higher_lower_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        val showMats = uiState.isStarted && uiState.rewardCards.isEmpty()
        if (showMats) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(140.dp + HigherLowerButtonReserveHeight))
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag(HIGHER_LOWER_MAT_ROW_TAG)
                ) {
                    val spacing = 16.dp
                    val shiftX = with(LocalDensity.current) {
                        maxWidth.toPx() + 24.dp.toPx()
                    }
                    val shiftY = with(LocalDensity.current) {
                        ((maxHeight - spacing) / 2f + spacing).toPx()
                    }
                    val transitionProgress by animateFloatAsState(
                        targetValue = if (uiState.isTransitioning) 1f else 0f,
                        animationSpec = if (uiState.isTransitioning) {
                            tween(durationMillis = HIGHER_LOWER_TRANSITION_MS)
                        } else {
                            tween(durationMillis = 0)
                        },
                        label = "higherLowerTransition"
                    )
                    val baseSum = uiState.baseDiceValues.takeIf { it.isNotEmpty() }?.sum()
                    val currentSum = uiState.currentDiceValues.takeIf { it.isNotEmpty() }?.sum()
                    val showTotals = shouldShowHigherLowerTotals(
                        isRolling = uiState.isRolling,
                        isTransitioning = uiState.isTransitioning
                    )
                    val showBaseTotal = showTotals
                    val showCurrentTotal = showTotals
                    val lowerMatColors = higherLowerBottomMatColors(
                        isSuccessHighlighting = uiState.isSuccessHighlighting,
                        isComplete = uiState.isComplete,
                        hasLoss = uiState.hasLoss
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(spacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HigherLowerSumLabel(
                                sum = baseSum,
                                isVisible = showBaseTotal && baseSum != null
                            )
                            HigherLowerMat(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                backgroundColor = SequenceSaveMatBackground,
                                borderColor = SequenceSaveMatBorder
                            ) {
                                HigherLowerDiceRow(
                                    values = uiState.baseDiceValues,
                                    diceRes = R.drawable.ten_sides,
                                    modifier = Modifier.graphicsLayer {
                                        translationX = shiftX * transitionProgress
                                    }
                                )
                            }
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HigherLowerSumLabel(
                                sum = currentSum,
                                isVisible = showCurrentTotal && currentSum != null
                            )
                            HigherLowerMat(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                backgroundColor = lowerMatColors.background,
                                borderColor = lowerMatColors.border
                            ) {
                                HigherLowerDiceRow(
                                    values = if (uiState.isCurrentDiceHidden) {
                                        emptyList()
                                    } else {
                                        uiState.currentDiceValues
                                    },
                                    diceRes = R.drawable.ten_sides,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationY = -shiftY * transitionProgress
                                        }
                                        .zIndex(2f)
                                )
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
                    .testTag(HIGHER_LOWER_REWARD_STACK_TAG)
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
                    .testTag(HIGHER_LOWER_CONTINUE_BUTTON_TAG)
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
private fun HigherLowerChoiceButton(
    label: String,
    isEnabled: Boolean,
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            disabledContentColor = MaterialTheme.colorScheme.onTertiary
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
        modifier = modifier
            .height(HigherLowerChoiceButtonHeight)
            .defaultMinSize(minWidth = HigherLowerChoiceButtonMinWidth)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

internal fun shouldShowHigherLowerTotals(
    isRolling: Boolean,
    isTransitioning: Boolean
): Boolean {
    return !isRolling && !isTransitioning
}

internal fun shouldShowHigherLowerChoiceRow(
    isChoiceVisible: Boolean,
    selectedChoice: HigherLowerChoice?
): Boolean {
    return isChoiceVisible || selectedChoice != null
}

internal data class HigherLowerMatColors(
    val background: Color,
    val border: Color
)

internal fun higherLowerBottomMatColors(
    isSuccessHighlighting: Boolean,
    isComplete: Boolean,
    hasLoss: Boolean
): HigherLowerMatColors {
    return when {
        isComplete && hasLoss -> HigherLowerMatColors(FailureMatBackground, FailureMatBackground)
        isSuccessHighlighting -> HigherLowerMatColors(VictoryMatBackground, VictoryMatBackground)
        else -> HigherLowerMatColors(SequenceSaveMatBackground, SequenceSaveMatBorder)
    }
}

@Composable
private fun HigherLowerMat(
    modifier: Modifier,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun HigherLowerDiceRow(
    values: List<Int>,
    diceRes: Int,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val spacing = 8.dp
        val horizontalPadding = 8.dp
        val availableWidth = maxWidth - horizontalPadding * 2 - spacing
        val diceSize = (availableWidth / 2f)
            .coerceAtMost(96.dp)
            .coerceAtLeast(48.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            values.take(2).forEach { value ->
                HigherLowerDieFace(
                    value = value,
                    size = diceSize,
                    diceRes = diceRes
                )
            }
        }
    }
}

@Composable
private fun HigherLowerSumLabel(
    sum: Int?,
    isVisible: Boolean
) {
    val text = if (isVisible && sum != null) {
        stringResource(R.string.higher_lower_total, sum)
    } else {
        ""
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.height(24.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun HigherLowerDieFace(
    value: Int,
    size: Dp,
    diceRes: Int
) {
    val fontSize = (size.value * 0.32f).coerceIn(14f, 22f).sp
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
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
