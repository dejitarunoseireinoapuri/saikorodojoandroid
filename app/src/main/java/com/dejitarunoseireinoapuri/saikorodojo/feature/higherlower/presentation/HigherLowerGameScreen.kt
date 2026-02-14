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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameMessageType
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameButtonPrimaryColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameButtonColors
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameMessageColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val HIGHER_LOWER_BUTTON_ROW_TAG = "higher_lower_button_row"
internal const val HIGHER_LOWER_MAT_ROW_TAG = "higher_lower_mat_row"
internal const val HIGHER_LOWER_CONTINUE_BUTTON_TAG = "higher_lower_continue_button"
internal const val HIGHER_LOWER_REWARD_STACK_TAG = "higher_lower_reward_stack"
private const val HIGHER_LOWER_TRANSITION_MS = 750
private val HigherLowerButtonReserveHeight = 140.dp
private val HigherLowerChoiceButtonHeight = 56.dp
private val HigherLowerChoiceButtonMinWidth = 140.dp
private val HigherLowerContinueButtonHeight = 56.dp
private val HigherLowerContinueButtonBottomPadding = 24.dp
private val HigherLowerContinueButtonSpacing = 8.dp

@Composable
fun HigherLowerGameRoute(
    modifier: Modifier = Modifier,
    viewModel: HigherLowerGameViewModel = viewModel(),
    onContinueClick: () -> Unit,
    onNavigateToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.saveSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    HigherLowerGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(HigherLowerGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(choice))
        },
        onContinueClick = onContinueClick,
        onExitToMenu = {
            viewModel.saveSession()
            onNavigateToMenu()
        }
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
    onContinueClick: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var wasRolling by remember { mutableStateOf(false) }
    var wasTransitioning by remember { mutableStateOf(false) }
    var hadRewardCards by remember { mutableStateOf(false) }
    var previousCorrectStreak by remember { mutableStateOf(0) }
    var previousHasLoss by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isRolling) {
        if (uiState.isRolling && !wasRolling) {
            soundPlayer.play(SoundEffect.DICE_ROLL)
        }
        wasRolling = uiState.isRolling
    }
    LaunchedEffect(uiState.isTransitioning) {
        if (uiState.isTransitioning && !wasTransitioning) {
            soundPlayer.play(SoundEffect.MOVE_DICE)
        }
        wasTransitioning = uiState.isTransitioning
    }
    LaunchedEffect(uiState.correctStreak) {
        if (shouldPlayHigherLowerSuccess(previousCorrectStreak, uiState.correctStreak)) {
            soundPlayer.play(SoundEffect.SUCCESS)
        }
        previousCorrectStreak = uiState.correctStreak
    }
    LaunchedEffect(uiState.hasLoss) {
        if (shouldPlayHigherLowerLoss(previousHasLoss, uiState.hasLoss)) {
            soundPlayer.play(SoundEffect.LOSS)
        }
        previousHasLoss = uiState.hasLoss
    }
    LaunchedEffect(uiState.rewardCards) {
        val hasRewards = uiState.rewardCards.isNotEmpty()
        if (hasRewards && !hadRewardCards) {
            soundPlayer.play(SoundEffect.CARD_DRAW)
        }
        hadRewardCards = hasRewards
    }
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    val startedGameBottomPadding = higherLowerStartedGameBottomPadding(uiState.isStarted)
    val startedGameVerticalOffsetPx = with(LocalDensity.current) {
        higherLowerStartedGameVerticalOffset(uiState.isStarted).toPx()
    }
    Box(
        modifier = containerModifier
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    soundPlayer.play(SoundEffect.QUESTION)
                    showExitDialog = true
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.cd_exit_home),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.rules_minigame_higher_lower_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 24.dp, top = 64.dp, bottom = startedGameBottomPadding)
                .then(
                    if (!uiState.isStarted) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            val titleColor = MaterialTheme.colorScheme.onBackground
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasLoss = uiState.hasLoss && uiState.isComplete
            val showStartButton = !uiState.isStarted && !uiState.isComplete && !hasReward
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
                        text = stringResource(R.string.rules_minigame_higher_lower_body),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = titleColor,
                        textAlign = TextAlign.Start,
                        modifier = rulesModifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                    )
                }
            }
            if (uiState.isStarted && !uiState.isComplete) {
                Column(
                    modifier = Modifier.graphicsLayer {
                        translationY = -startedGameVerticalOffsetPx
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                    when (higherLowerChoiceButtonsMode(uiState.isChoiceVisible, uiState.selectedChoice)) {
                        HigherLowerChoiceButtonsMode.Hidden -> Unit
                        HigherLowerChoiceButtonsMode.SelectedOnly -> {
                            val selectedChoice = uiState.selectedChoice
                            if (selectedChoice != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag(HIGHER_LOWER_BUTTON_ROW_TAG)
                                ) {
                                    HigherLowerChoiceButton(
                                        label = stringResource(
                                            if (selectedChoice == HigherLowerChoice.LOWER) {
                                                R.string.higher_lower_lower
                                            } else {
                                                R.string.higher_lower_higher
                                            }
                                        ),
                                        isEnabled = false,
                                        onClick = {},
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        HigherLowerChoiceButtonsMode.Both -> {
                            Row(
                                modifier = Modifier.testTag(HIGHER_LOWER_BUTTON_ROW_TAG),
                                horizontalArrangement = Arrangement.spacedBy(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HigherLowerChoiceButton(
                                    label = stringResource(R.string.higher_lower_lower),
                                    isEnabled = uiState.selectedChoice == null,
                                    onClick = {
                                        onChoiceSelect(HigherLowerChoice.LOWER)
                                    }
                                )
                                HigherLowerChoiceButton(
                                    label = stringResource(R.string.higher_lower_higher),
                                    isEnabled = uiState.selectedChoice == null,
                                    onClick = {
                                        onChoiceSelect(HigherLowerChoice.HIGHER)
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (showStartButton) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onStartClick()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = minigameButtonColors(),
                    modifier = Modifier.height(64.dp)
                ) {
                    Text(
                        text = stringResource(R.string.higher_lower_start),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        val showMats = uiState.isStarted && uiState.rewardCards.isEmpty()
        if (showMats) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 12.dp + startedGameBottomPadding
                    ),
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
                    val baseTransitionProgress by animateFloatAsState(
                        targetValue = if (uiState.isTransitioning) 1f else 0f,
                        animationSpec = if (uiState.isTransitioning) {
                            tween(durationMillis = HIGHER_LOWER_TRANSITION_MS)
                        } else {
                            tween(durationMillis = 0)
                        },
                        label = "higherLowerTransition"
                    )
                    val currentTransitionProgress by animateFloatAsState(
                        targetValue = if (uiState.isTransitioning || uiState.isCurrentDiceAnchoredUp) {
                            1f
                        } else {
                            0f
                        },
                        animationSpec = if (uiState.isTransitioning) {
                            tween(durationMillis = HIGHER_LOWER_TRANSITION_MS)
                        } else {
                            tween(durationMillis = 0)
                        },
                        label = "higherLowerCurrentTransition"
                    )
                    val baseSum = uiState.baseDiceValues.takeIf { it.isNotEmpty() }?.sum()
                    val currentSum = uiState.currentDiceValues.takeIf { it.isNotEmpty() }?.sum()
                    val showTotals = shouldShowHigherLowerTotals(
                        isRolling = uiState.isRolling,
                        isTransitioning = uiState.isTransitioning
                    )
                    val showCurrentTotal = shouldShowHigherLowerCurrentTotal(
                        isRolling = uiState.isRolling,
                        isTransitioning = uiState.isTransitioning,
                        isCurrentDiceHidden = uiState.isCurrentDiceHidden,
                        isCurrentDiceAnchoredUp = uiState.isCurrentDiceAnchoredUp,
                        hasCurrentDice = uiState.currentDiceValues.isNotEmpty()
                    )
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
                                isVisible = showTotals && baseSum != null
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
                                    modifier = Modifier
                                        .alpha(if (uiState.isCurrentDiceAnchoredUp) 0f else 1f)
                                        .graphicsLayer {
                                            translationX = shiftX * baseTransitionProgress
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
                                            translationY = -shiftY * currentTransitionProgress
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
                    .padding(top = 112.dp, bottom = startedGameBottomPadding)
                    .testTag(HIGHER_LOWER_REWARD_STACK_TAG)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                RewardCardStack(cards = uiState.rewardCards)
            }
        }

        if (uiState.isComplete && uiState.isStarted) {
            Button(
                onClick = {
                    soundPlayer.play(SoundEffect.USE)
                    onContinueClick()
                },
                shape = RoundedCornerShape(20.dp),
                colors = minigameButtonColors(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = HigherLowerContinueButtonBottomPadding)
                    .height(HigherLowerContinueButtonHeight)
                    .zIndex(4f)
                    .testTag(HIGHER_LOWER_CONTINUE_BUTTON_TAG)
            ) {
                Text(
                    text = stringResource(R.string.odd_even_continue),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = MaterialTheme.colorScheme.background,
                title = {
                    Text(
                        text = stringResource(R.string.exit_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.exit_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            showExitDialog = false
                            onExitToMenu()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.exit_confirm),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            showExitDialog = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_cancel),
                            style = MaterialTheme.typography.titleMedium,
                            color = MinigameButtonPrimaryColor
                        )
                    }
                }
            )
        }
    }
}

internal fun higherLowerStartedGameBottomPadding(isStarted: Boolean): Dp {
    if (!isStarted) {
        return 24.dp
    }
    return 24.dp +
        HigherLowerContinueButtonHeight +
        HigherLowerContinueButtonBottomPadding +
        HigherLowerContinueButtonSpacing
}

internal fun higherLowerStartedGameVerticalOffset(isStarted: Boolean): Dp {
    if (!isStarted) {
        return 0.dp
    }
    return higherLowerStartedGameBottomPadding(isStarted = true) - 24.dp
}

@Composable
private fun HigherLowerChoiceButton(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        shape = RoundedCornerShape(20.dp),
        colors = minigameButtonColors(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        modifier = modifier
            .height(HigherLowerChoiceButtonHeight)
            .defaultMinSize(minWidth = HigherLowerChoiceButtonMinWidth)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
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

internal fun shouldShowHigherLowerCurrentTotal(
    isRolling: Boolean,
    isTransitioning: Boolean,
    isCurrentDiceHidden: Boolean,
    isCurrentDiceAnchoredUp: Boolean,
    hasCurrentDice: Boolean
): Boolean {
    return shouldShowHigherLowerTotals(
        isRolling = isRolling,
        isTransitioning = isTransitioning
    ) && !isCurrentDiceHidden && !isCurrentDiceAnchoredUp && hasCurrentDice
}

internal enum class HigherLowerChoiceButtonsMode {
    Hidden,
    SelectedOnly,
    Both
}

internal fun higherLowerChoiceButtonsMode(
    isChoiceVisible: Boolean,
    selectedChoice: HigherLowerChoice?
): HigherLowerChoiceButtonsMode {
    return when {
        isChoiceVisible -> HigherLowerChoiceButtonsMode.Both
        selectedChoice != null -> HigherLowerChoiceButtonsMode.SelectedOnly
        else -> HigherLowerChoiceButtonsMode.Hidden
    }
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

internal fun shouldPlayHigherLowerSuccess(previousStreak: Int, currentStreak: Int): Boolean {
    return currentStreak > previousStreak
}

internal fun shouldPlayHigherLowerLoss(previousHasLoss: Boolean, currentHasLoss: Boolean): Boolean {
    return !previousHasLoss && currentHasLoss
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
