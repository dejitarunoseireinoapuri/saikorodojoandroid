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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.positionInRoot
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameMessageType
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameButtonColors
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameMessageColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
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
internal const val SEQUENCE_ROUND_STATUS_TAG = "sequence_round_status"
internal const val SEQUENCE_DECISION_ROW_TAG = "sequence_decision_row"

internal enum class SequenceDecisionAction {
    Discard,
    Save
}

internal fun sequenceDecisionActionOrder(): List<SequenceDecisionAction> = listOf(
    SequenceDecisionAction.Discard,
    SequenceDecisionAction.Save
)

internal fun sequenceDiceNumberYOffset(): Dp = 0.dp
internal const val SEQUENCE_SAVED_MAT_TAG = "sequence_saved_mat"
internal const val SEQUENCE_REWARD_STACK_TAG = "sequence_reward_stack"
private const val SEQUENCE_SAVE_ANIMATION_MS = 320
private val SEQUENCE_ACTIVE_CONTENT_SHIFT = 72.dp

@Composable
fun SequenceGameRoute(
    modifier: Modifier = Modifier,
    viewModel: SequenceGameViewModel = viewModel(),
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

    SequenceGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(SequenceGameUiEvent.StartGame) },
        onSaveClick = { viewModel.onEvent(SequenceGameUiEvent.SaveRoll) },
        onDiscardClick = { viewModel.onEvent(SequenceGameUiEvent.DiscardRoll) },
        onContinueClick = onContinueClick,
        onExitToMenu = {
            viewModel.saveSession()
            onNavigateToMenu()
        }
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
    onContinueClick: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var wasRolling by remember { mutableStateOf(false) }
    var hadRewardCards by remember { mutableStateOf(false) }
    var previousHasFailure by remember { mutableStateOf(false) }
    var previousFailureDieValue by remember { mutableStateOf<Int?>(null) }
    var animatingSaveValue by remember { mutableStateOf<Int?>(null) }
    var animatingToFailureDie by remember { mutableStateOf(false) }
    var diceCenterInRoot by remember { mutableStateOf<Offset?>(null) }
    var savedDieCenterInRoot by remember { mutableStateOf<Offset?>(null) }
    var failureDieCenterInRoot by remember { mutableStateOf<Offset?>(null) }
    var animatedDieSize by remember { mutableStateOf(0.dp) }
    val animatedTextOffsetPx = with(LocalDensity.current) { sequenceDiceNumberYOffset().toPx() }
    val saveAnimationProgress = remember { Animatable(0f) }
    LaunchedEffect(uiState.isRolling) {
        if (uiState.isRolling && !wasRolling) {
            soundPlayer.play(SoundEffect.DICE_ROLL)
        }
        wasRolling = uiState.isRolling
    }
    var previousSavedCount by remember { mutableStateOf(0) }
    LaunchedEffect(uiState.savedValues.size) {
        if (shouldPlaySequenceSuccess(previousSavedCount, uiState.savedValues.size)) {
            soundPlayer.play(SoundEffect.SUCCESS)
        }
        previousSavedCount = uiState.savedValues.size
    }
    LaunchedEffect(uiState.pendingSavedValue) {
        val pendingValue = uiState.pendingSavedValue ?: return@LaunchedEffect
        try {
            saveAnimationProgress.snapTo(0f)
            animatingSaveValue = pendingValue
            animatingToFailureDie = false
            soundPlayer.play(SoundEffect.MOVE_DICE)
            saveAnimationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = SEQUENCE_SAVE_ANIMATION_MS,
                    easing = LinearOutSlowInEasing
                )
            )
        } finally {
            animatingSaveValue = null
            animatingToFailureDie = false
        }
    }
    LaunchedEffect(uiState.failureReason) {
        val hasFailure = uiState.failureReason != null
        if (shouldPlaySequenceLoss(previousHasFailure, hasFailure)) {
            soundPlayer.play(SoundEffect.LOSS)
        }
        previousHasFailure = hasFailure
    }
    LaunchedEffect(uiState.failureDieValue) {
        val failureValue = uiState.failureDieValue
        if (failureValue != null && failureValue != previousFailureDieValue) {
            try {
                saveAnimationProgress.snapTo(0f)
                animatingSaveValue = failureValue
                animatingToFailureDie = true
                soundPlayer.play(SoundEffect.MOVE_DICE)
                saveAnimationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = SEQUENCE_SAVE_ANIMATION_MS,
                        easing = LinearOutSlowInEasing
                    )
                )
            } finally {
                animatingSaveValue = null
                animatingToFailureDie = false
            }
        }
        previousFailureDieValue = failureValue
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
        val hasReward = uiState.rewardCards.isNotEmpty()
        val hasPendingReward = uiState.isComplete && uiState.pendingRewardCards.isNotEmpty()
        val hasLoss = uiState.isComplete && !hasReward && !hasPendingReward && uiState.isStarted

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
                text = stringResource(R.string.rules_minigame_sequence_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        val activeContentShiftPx = with(LocalDensity.current) { SEQUENCE_ACTIVE_CONTENT_SHIFT.toPx() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 24.dp)
                .graphicsLayer {
                    if (uiState.isStarted) {
                        translationY = -activeContentShiftPx
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            val titleColor = MaterialTheme.colorScheme.onBackground
            val showRules = !uiState.isStarted
            val rulesModifier = if (showRules) {
                Modifier
            } else {
                Modifier.alpha(0f).clearAndSetSemantics { }
            }
            if (uiState.isStarted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                ) {
                    Text(
                        text = stringResource(R.string.rules_minigame_sequence_body),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = titleColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .alpha(0f)
                            .clearAndSetSemantics { }
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    )
                    when {
                        hasPendingReward -> Text(
                            text = stringResource(R.string.minigame_win_message),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                            color = minigameMessageColor(
                                MinigameMessageType.Win,
                                titleColor = titleColor
                            ),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.rules_minigame_sequence_body),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = titleColor,
                    textAlign = TextAlign.Start,
                    modifier = rulesModifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
            if (uiState.isStarted && !hasReward) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRoll,
                        uiState.totalRolls
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = titleColor,
                    modifier = Modifier.testTag(SEQUENCE_ROUND_STATUS_TAG)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            if (uiState.isStarted) {
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isAwaitingDecision) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(SEQUENCE_DECISION_ROW_TAG),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            sequenceDecisionActionOrder().forEach { action ->
                                when (action) {
                                    SequenceDecisionAction.Discard -> SequenceChoiceButton(
                                        label = stringResource(R.string.sequence_discard),
                                        testTag = SEQUENCE_DISCARD_BUTTON_TAG,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onDiscardClick()
                                        }
                                    )

                                    SequenceDecisionAction.Save -> SequenceChoiceButton(
                                        label = stringResource(R.string.sequence_save),
                                        testTag = SEQUENCE_SAVE_BUTTON_TAG,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            onSaveClick()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                val showMats = shouldShowSequenceMats(hasRewardCards = uiState.rewardCards.isNotEmpty())
                if (showMats) {
                    Box(
                        modifier = Modifier.height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SequenceDiceFace(
                            value = uiState.diceValue,
                            size = 140.dp,
                            showDie = shouldShowSequenceTopDie(
                                isComplete = uiState.isComplete,
                                animatingSaveValue = animatingSaveValue,
                                keepTopDieOnFailure = uiState.keepTopDieOnFailure
                            ),
                            backgroundColor = SequenceSaveMatBackground,
                            borderColor = SequenceSaveMatBorder,
                            modifier = Modifier
                                .testTag(SEQUENCE_DICE_TAG)
                                .onGloballyPositioned { coordinates ->
                                    val position = coordinates.positionInRoot()
                                    diceCenterInRoot = Offset(
                                        x = position.x + coordinates.size.width / 2f,
                                        y = position.y + coordinates.size.height / 2f
                                    )
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
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
                            if (animatedDieSize != dieSize) {
                                animatedDieSize = dieSize
                            }
                            Row(
                                modifier = Modifier.padding(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(spacing),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isLatestSavedValueHidden = shouldHideLatestSavedValue(
                                    isLatestSavedValueHidden = uiState.isLatestSavedValueHidden,
                                    previousSavedCount = previousSavedCount,
                                    currentSavedCount = uiState.savedValues.size
                                )
                                sequenceSavedDiceUiState(
                                    savedValues = uiState.savedValues,
                                    isLatestSavedValueHidden = isLatestSavedValueHidden,
                                    hasPendingSavedValue = uiState.pendingSavedValue != null
                                ).forEach { savedDie ->
                                    if (savedDie.value != null) {
                                        SequenceSavedDie(
                                            value = savedDie.value,
                                            size = dieSize,
                                            isVisible = shouldShowSequenceSavedDie(
                                                isVisible = savedDie.isVisible,
                                                isLatest = savedDie.isLatest,
                                                animatingSaveValue = animatingSaveValue,
                                                isAnimatingToFailure = animatingToFailureDie,
                                                value = savedDie.value
                                            ),
                                            modifier = if (savedDie.isLatest) {
                                                Modifier.onGloballyPositioned { coordinates ->
                                                    val position = coordinates.positionInRoot()
                                                    savedDieCenterInRoot = Offset(
                                                        x = position.x + coordinates.size.width / 2f,
                                                        y = position.y + coordinates.size.height / 2f
                                                    )
                                                }
                                            } else {
                                                Modifier
                                            }
                                        )
                                    } else {
                                        Spacer(
                                            modifier = Modifier
                                                .size(dieSize)
                                                .onGloballyPositioned { coordinates ->
                                                    val position = coordinates.positionInRoot()
                                                    savedDieCenterInRoot = Offset(
                                                        x = position.x + coordinates.size.width / 2f,
                                                        y = position.y + coordinates.size.height / 2f
                                                    )
                                                }
                                        )
                                    }
                                }
                                uiState.failureDieValue?.let { value ->
                                    SequenceSavedDie(
                                        value = value,
                                        size = dieSize,
                                        isVisible = shouldShowSequenceSavedDie(
                                            isVisible = true,
                                            isLatest = false,
                                            animatingSaveValue = animatingSaveValue,
                                            isAnimatingToFailure = animatingToFailureDie,
                                            value = value
                                        ),
                                        modifier = Modifier.onGloballyPositioned { coordinates ->
                                            val position = coordinates.positionInRoot()
                                            failureDieCenterInRoot = Offset(
                                                x = position.x + coordinates.size.width / 2f,
                                                y = position.y + coordinates.size.height / 2f
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!uiState.isStarted) {
            Button(
                onClick = {
                    onStartClick()
                },
                shape = RoundedCornerShape(20.dp),
                colors = minigameButtonColors(),
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

        if (shouldShowSequenceRewardOverlay(hasRewardCards = uiState.rewardCards.isNotEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .testTag(SEQUENCE_REWARD_STACK_TAG)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.minigame_win_message),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.Win,
                            titleColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_cards_message),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.WinCards,
                            titleColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RewardCardStack(cards = uiState.rewardCards)
                }
            }
        }

        if (shouldShowSequenceLossOverlay(hasLoss = hasLoss)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.minigame_lose_message),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                    color = minigameMessageColor(
                        MinigameMessageType.Lose,
                        titleColor = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (shouldShowSequenceContinueButton(hasReward = hasReward, hasLoss = hasLoss)) {
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    }

    val currentAnimationValue = animatingSaveValue
    if (currentAnimationValue != null) {
        val start = diceCenterInRoot
        val end = if (animatingToFailureDie) failureDieCenterInRoot else savedDieCenterInRoot
        val dieSize = animatedDieSize
        if (start != null && end != null && dieSize > 0.dp) {
            val sizePx = with(LocalDensity.current) { dieSize.toPx() }
            val progress = saveAnimationProgress.value
            val offset = Offset(
                x = start.x + (end.x - start.x) * progress,
                y = start.y + (end.y - start.y) * progress
            )
            Box(
                modifier = Modifier
                    .size(dieSize)
                    .graphicsLayer {
                        translationX = offset.x - sizePx / 2f
                        translationY = offset.y - sizePx / 2f
                    }
                    .zIndex(5f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ten_sides),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = currentAnimationValue.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.graphicsLayer { translationY = animatedTextOffsetPx }
                )
            }
        }
    }
}


internal data class SequenceSavedDieUi(
    val value: Int?,
    val isVisible: Boolean,
    val isLatest: Boolean
)

internal fun shouldHideLatestSavedValue(
    isLatestSavedValueHidden: Boolean,
    previousSavedCount: Int,
    currentSavedCount: Int
): Boolean {
    return isLatestSavedValueHidden
}

internal fun sequenceSavedDiceUiState(
    savedValues: List<Int>,
    isLatestSavedValueHidden: Boolean,
    hasPendingSavedValue: Boolean
): List<SequenceSavedDieUi> {
    if (savedValues.isEmpty() && !hasPendingSavedValue) {
        return emptyList()
    }
    val hiddenIndex = if (isLatestSavedValueHidden) savedValues.lastIndex else -1
    val savedDice = savedValues.mapIndexed { index, value ->
        SequenceSavedDieUi(
            value = value,
            isVisible = index != hiddenIndex,
            isLatest = index == savedValues.lastIndex && !hasPendingSavedValue
        )
    }
    if (!hasPendingSavedValue) {
        return savedDice
    }
    return savedDice + SequenceSavedDieUi(
        value = null,
        isVisible = false,
        isLatest = true
    )
}

internal fun shouldShowSequenceRewardOverlay(hasRewardCards: Boolean): Boolean {
    return hasRewardCards
}

internal fun shouldShowSequenceLossOverlay(hasLoss: Boolean): Boolean {
    return hasLoss
}

internal fun shouldShowSequenceMats(hasRewardCards: Boolean): Boolean {
    return !hasRewardCards
}

internal fun shouldShowSequenceContinueButton(
    hasReward: Boolean,
    hasLoss: Boolean
): Boolean {
    return hasReward || hasLoss
}

internal fun shouldShowSequenceTopDie(
    isComplete: Boolean,
    animatingSaveValue: Int?,
    keepTopDieOnFailure: Boolean
): Boolean {
    if (isComplete && !keepTopDieOnFailure) {
        return false
    }
    return animatingSaveValue == null
}

internal fun shouldPlaySequenceSuccess(previousSavedCount: Int, currentSavedCount: Int): Boolean {
    return currentSavedCount > previousSavedCount
}

internal fun shouldPlaySequenceLoss(previousHasFailure: Boolean, currentHasFailure: Boolean): Boolean {
    return !previousHasFailure && currentHasFailure
}

internal fun shouldShowSequenceSavedDie(
    isVisible: Boolean,
    isLatest: Boolean,
    animatingSaveValue: Int?,
    isAnimatingToFailure: Boolean,
    value: Int
): Boolean {
    if (!isVisible) {
        return false
    }
    if (animatingSaveValue == null) {
        return true
    }
    return if (isAnimatingToFailure) {
        value != animatingSaveValue
    } else {
        !isLatest
    }
}

@Composable
private fun SequenceChoiceButton(
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = minigameButtonColors(),
        modifier = modifier
            .height(64.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
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
    showDie: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val textOffsetPx = with(LocalDensity.current) { sequenceDiceNumberYOffset().toPx() }
    Box(
        modifier = modifier
            .size(size)
            .background(backgroundColor, RoundedCornerShape(18.dp))
            .border(2.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (showDie && value != null) {
            Image(
                painter = painterResource(id = R.drawable.ten_sides),
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
    size: Dp,
    isVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    val diceRes = R.drawable.ten_sides
    val textOffsetPx = with(LocalDensity.current) { sequenceDiceNumberYOffset().toPx() }
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (isVisible) 1f else 0f)
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
