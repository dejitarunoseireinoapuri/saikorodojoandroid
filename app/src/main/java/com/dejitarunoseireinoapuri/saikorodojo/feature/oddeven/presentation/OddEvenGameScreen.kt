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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameMessageType
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameButtonPrimaryColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameButtonColors
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameMessageColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val ODD_EVEN_DICE_TAG = "odd_even_dice"
internal const val ODD_EVEN_CHOICE_ROW_TAG = "odd_even_choice_row"
internal const val ODD_EVEN_CONTINUE_BUTTON_TAG = "odd_even_continue_button"
internal const val ODD_EVEN_REWARD_STACK_TAG = "odd_even_reward_stack"
internal const val ODD_EVEN_STATUS_COLUMN_TAG = "odd_even_status_column"
internal const val ODD_EVEN_ROUND_STATUS_TAG = "odd_even_round_status"
internal const val ODD_EVEN_HITS_STATUS_TAG = "odd_even_hits_status"
internal val ODD_EVEN_DICE_SIZE = 150.dp
internal val ODD_EVEN_OVERLAY_OFFSET_Y = 190.dp

@Composable
fun OddEvenGameRoute(
    modifier: Modifier = Modifier,
    viewModel: OddEvenGameViewModel = viewModel(),
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

    OddEvenGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(OddEvenGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(choice))
        },
        onContinueClick = onContinueClick,
        onExitToMenu = {
            viewModel.saveSession()
            onNavigateToMenu()
        }
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
    onContinueClick: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var wasRolling by remember { mutableStateOf(false) }
    var hadRewardCards by remember { mutableStateOf(false) }
    var previousCorrectCount by remember { mutableStateOf(0) }
    var previousWrongCount by remember { mutableStateOf(0) }
    var previousHasLoss by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isRolling) {
        if (uiState.isRolling && !wasRolling) {
            soundPlayer.play(SoundEffect.DICE_ROLL)
        }
        wasRolling = uiState.isRolling
    }
    LaunchedEffect(uiState.correctCount) {
        if (shouldPlayOddEvenSuccess(previousCorrectCount, uiState.correctCount)) {
            soundPlayer.play(SoundEffect.SUCCESS)
        }
        previousCorrectCount = uiState.correctCount
    }
    LaunchedEffect(uiState.wrongCount, uiState.isComplete, uiState.rewardCards) {
        val hasLoss = uiState.isComplete && uiState.rewardCards.isEmpty() && uiState.isStarted
        if (shouldPlayOddEvenLoss(previousWrongCount, uiState.wrongCount, previousHasLoss, hasLoss)) {
            soundPlayer.play(SoundEffect.LOSS)
        }
        previousWrongCount = uiState.wrongCount
        previousHasLoss = hasLoss
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
                text = stringResource(R.string.rules_minigame_odd_even_title),
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
                .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 24.dp)
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
            val hasLoss = uiState.isComplete && !hasReward && uiState.isStarted
            val showStartButton = !uiState.isStarted && !uiState.isComplete && !hasReward
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
            } else if (hasLoss) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.minigame_lose_message),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = minigameMessageColor(
                        MinigameMessageType.Lose,
                        titleColor = titleColor
                    )
                )
            } else {
                Text(
                    text = stringResource(R.string.rules_minigame_odd_even_body),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = titleColor,
                    textAlign = TextAlign.Start,
                    modifier = rulesModifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
                if (!uiState.isStarted && showStartButton) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            onStartClick()
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = minigameButtonColors(),
                        modifier = Modifier.height(64.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.odd_even_start),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (shouldShowOddEvenDice(uiState)) {
            val hasReward = uiState.rewardCards.isNotEmpty()
            val hasLoss = uiState.isComplete && !hasReward && uiState.isStarted
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OddEvenDiceFace(
                    value = uiState.diceValue,
                    size = ODD_EVEN_DICE_SIZE,
                    isSuccess = uiState.showFireworks,
                    isFailure = uiState.showFailure || hasLoss,
                    modifier = Modifier.testTag(ODD_EVEN_DICE_TAG)
                )
            }
        }

        if (uiState.isStarted && uiState.rewardCards.isEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -ODD_EVEN_OVERLAY_OFFSET_Y)
                    .testTag(ODD_EVEN_STATUS_COLUMN_TAG),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRound,
                        uiState.totalRounds
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(ODD_EVEN_ROUND_STATUS_TAG)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_hits_status,
                        uiState.correctCount,
                        uiState.targetCorrect
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.testTag(ODD_EVEN_HITS_STATUS_TAG)
                )
                if (!uiState.isComplete) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.testTag(ODD_EVEN_CHOICE_ROW_TAG),
                        horizontalArrangement = Arrangement.spacedBy(36.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.ODD,
                            label = stringResource(R.string.odd_even_even),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = {
                                onChoiceSelect(OddEvenChoice.EVEN)
                            }
                        )
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.EVEN,
                            label = stringResource(R.string.odd_even_odd),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = {
                                onChoiceSelect(OddEvenChoice.ODD)
                            }
                        )
                    }
                }
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .testTag(ODD_EVEN_REWARD_STACK_TAG)
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
        colors = minigameButtonColors(),
        modifier = Modifier.height(56.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
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

internal fun shouldPlayOddEvenSuccess(previousCorrect: Int, currentCorrect: Int): Boolean {
    return currentCorrect > previousCorrect
}

internal fun shouldPlayOddEvenLoss(
    previousWrong: Int,
    currentWrong: Int,
    previousHasLoss: Boolean,
    currentHasLoss: Boolean
): Boolean {
    return currentWrong > previousWrong || (currentHasLoss && !previousHasLoss)
}

internal fun shouldShowOddEvenDice(uiState: OddEvenGameUiState): Boolean {
    return uiState.isStarted && uiState.rewardCards.isEmpty()
}
