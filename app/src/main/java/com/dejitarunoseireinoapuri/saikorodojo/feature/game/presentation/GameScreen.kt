package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.ui.platform.LocalContext
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

@Composable
fun GameRoute(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
    onNavigateToMinigame: (MinigameType) -> Unit,
    onNavigateToMenu: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val rewardedAdState = remember { mutableStateOf<RewardedAd?>(null) }
    val pendingRewardedAd = remember { mutableStateOf(false) }
    val onAdCompleted by rememberUpdatedState { viewModel.onEvent(GameUiEvent.MinigamesAdCompleted) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.saveSession()
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(GameUiEvent.RefreshInventory)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (viewModel.shouldStartRollOnLaunch()) {
            viewModel.onEvent(GameUiEvent.StartRoll)
        }
    }

    LaunchedEffect(Unit) {
        loadRewardedAd(context, rewardedAdState) { rewardAd ->
            if (pendingRewardedAd.value) {
                pendingRewardedAd.value = false
                showRewardedAd(
                    activity = context.findActivity(),
                    rewardedAd = rewardAd,
                    onEarnedReward = onAdCompleted,
                    onDismissed = { loadRewardedAd(context, rewardedAdState) },
                    onFailedToShow = { loadRewardedAd(context, rewardedAdState) }
                )
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GameUiEffect.NavigateToMinigame -> onNavigateToMinigame(effect.minigame)
                is GameUiEffect.NavigateToMenu -> onNavigateToMenu(effect.resetProgress)
                GameUiEffect.ShowMinigamesRewardedAd -> {
                    val activity = context.findActivity()
                    val shown = showRewardedAd(
                        activity = activity,
                        rewardedAd = rewardedAdState.value,
                        onEarnedReward = onAdCompleted,
                        onDismissed = { loadRewardedAd(context, rewardedAdState) },
                        onFailedToShow = { loadRewardedAd(context, rewardedAdState) }
                    )
                    if (!shown) {
                        pendingRewardedAd.value = true
                        loadRewardedAd(context, rewardedAdState) { rewardAd ->
                            if (pendingRewardedAd.value) {
                                pendingRewardedAd.value = false
                                showRewardedAd(
                                    activity = context.findActivity(),
                                    rewardedAd = rewardAd,
                                    onEarnedReward = onAdCompleted,
                                    onDismissed = { loadRewardedAd(context, rewardedAdState) },
                                    onFailedToShow = { loadRewardedAd(context, rewardedAdState) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    GameScreen(
        modifier = modifier,
        uiState = uiState,
        onDiceClick = { index -> viewModel.onEvent(GameUiEvent.DiceClicked(index)) },
        onCardSelect = { index -> viewModel.onEvent(GameUiEvent.SelectCard(index)) },
        onCardDismiss = { viewModel.onEvent(GameUiEvent.DismissSelectedCard) },
        onCardApply = { index -> viewModel.onEvent(GameUiEvent.ApplyCard(index)) },
        onAdjustSelectedDie = { delta -> viewModel.onEvent(GameUiEvent.AdjustSelectedDie(delta)) },
        onSetSelectedDieValue = { value ->
            viewModel.onEvent(GameUiEvent.SetSelectedDieValue(value))
        },
        onRollSelectedDice = { viewModel.onEvent(GameUiEvent.RollSelectedDice) },
        onRollSingleDie = { viewModel.onEvent(GameUiEvent.RollSingleDie) },
        onFlipSelectedDie = { viewModel.onEvent(GameUiEvent.FlipSelectedDie) },
        onConfirmSurrender = { viewModel.onEvent(GameUiEvent.ConfirmSurrender) },
        onConfirmExit = { viewModel.onEvent(GameUiEvent.ConfirmExit) },
        onOpenRandomMinigame = { viewModel.onEvent(GameUiEvent.OpenRandomMinigame) },
        onConfirmMinigamesAd = { viewModel.onEvent(GameUiEvent.ConfirmMinigamesAd) },
        onDismissMinigamesAdPrompt = { viewModel.onEvent(GameUiEvent.DismissMinigamesAdPrompt) }
    )
}

private const val MINIGAMES_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

private fun loadRewardedAd(
    context: Context,
    rewardedAdState: androidx.compose.runtime.MutableState<RewardedAd?>,
    onLoaded: (RewardedAd) -> Unit = {}
) {
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
        context,
        MINIGAMES_REWARDED_AD_UNIT_ID,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAdState.value = ad
                onLoaded(ad)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewardedAdState.value = null
            }
        }
    )
}

private fun showRewardedAd(
    activity: Activity?,
    rewardedAd: RewardedAd?,
    onEarnedReward: () -> Unit,
    onDismissed: () -> Unit,
    onFailedToShow: () -> Unit
): Boolean {
    if (rewardedAd == null || activity == null) return false
    rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            onDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
            onFailedToShow()
        }
    }
    rewardedAd.show(activity) { onEarnedReward() }
    return true
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: GameUiState,
    onDiceClick: (Int) -> Unit,
    onCardSelect: (Int) -> Unit,
    onCardDismiss: () -> Unit,
    onCardApply: (Int) -> Unit,
    onAdjustSelectedDie: (Int) -> Unit,
    onSetSelectedDieValue: (Int) -> Unit,
    onRollSelectedDice: () -> Unit,
    onRollSingleDie: () -> Unit,
    onFlipSelectedDie: () -> Unit,
    onConfirmSurrender: () -> Unit,
    onConfirmExit: () -> Unit,
    onOpenRandomMinigame: () -> Unit,
    onConfirmMinigamesAd: () -> Unit,
    onDismissMinigamesAdPrompt: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var wasRolling by remember { mutableStateOf(false) }
    var wasLevelComplete by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isRolling) {
        if (uiState.isRolling && !wasRolling) {
            soundPlayer.play(SoundEffect.DICE_ROLL)
        }
        wasRolling = uiState.isRolling
    }
    LaunchedEffect(uiState.isLevelComplete) {
        if (uiState.isLevelComplete && !wasLevelComplete) {
            soundPlayer.play(SoundEffect.SUCCESS)
        }
        wasLevelComplete = uiState.isLevelComplete
    }
    val containerModifier = modifier
        .fillMaxSize()
        .let { baseModifier ->
            if (applySystemBarsPadding) {
                baseModifier.systemBarsPadding()
            } else {
                baseModifier
            }
        }
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    BoxWithConstraints(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        var showSurrenderDialog by remember { mutableStateOf(false) }
        var showExitDialog by remember { mutableStateOf(false) }
        var expandedObjectiveIndex by remember { mutableStateOf<Int?>(null) }
        BackHandler(enabled = !showExitDialog && !showSurrenderDialog) {
            showExitDialog = true
        }
        val constraintsWidth = maxWidth
        val constraintsHeight = maxHeight
        val shouldHideCards = uiState.isAwaitingRerollSingle ||
            uiState.isAwaitingRerollSelected ||
            uiState.isAwaitingFlipFace ||
            uiState.isAwaitingAdjustPlusMinus ||
            uiState.isAwaitingSetValue
        val stackOffset by animateDpAsState(
            targetValue = if (shouldHideCards) maxHeight else 0.dp,
            animationSpec = tween(durationMillis = 220),
            label = "cardStackOffset"
        )
        val stackAlpha by animateFloatAsState(
            targetValue = if (shouldHideCards) 0f else 1f,
            animationSpec = tween(durationMillis = 180),
            label = "cardStackAlpha"
        )
        if (uiState.showLevelCompleteMessage) {
            Text(
                text = stringResource(R.string.level_complete_message),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 280.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp, start = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.level_title, uiState.levelNumber),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.QUESTION)
                            showExitDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = stringResource(R.string.cd_exit_home),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    MinigamesAvailableBadge(
                        minigamesAvailable = uiState.minigamesAvailable,
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            onOpenRandomMinigame()
                        }
                    )
                    IconButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.QUESTION)
                            showSurrenderDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = stringResource(R.string.cd_surrender),
                            tint = Color.White
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 76.dp)
                    .widthIn(max = 360.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.objectiveLines.forEachIndexed { index, line ->
                    val objectiveText = when (val text = line.text) {
                        is ObjectiveLineText.StringRes -> {
                            stringResource(text.resId, *text.formatArgs.toTypedArray())
                        }
                        is ObjectiveLineText.PluralRes -> {
                            pluralStringResource(
                                text.resId,
                                text.quantity,
                                *text.formatArgs.toTypedArray()
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = objectiveText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (line.isMet) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        if (line.explainText != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(1.dp, Color.White, CircleShape)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        expandedObjectiveIndex = if (expandedObjectiveIndex == index) {
                                            null
                                        } else {
                                            index
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "i",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
            DiceBoard(
                modifier = Modifier.zIndex(0f),
                maxWidth = maxWidth,
                uiState = uiState,
                onDiceClick = onDiceClick,
                onAdjustSelectedDie = onAdjustSelectedDie,
                onSetSelectedDieValue = onSetSelectedDieValue,
                onRollSelectedDice = onRollSelectedDice,
                onRollSingleDie = onRollSingleDie,
                onFlipSelectedDie = onFlipSelectedDie
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .clipToBounds()
                    .zIndex(1f)
                    .offset { IntOffset(0, stackOffset.roundToPx()) }
                    .alpha(stackAlpha)
            ) {
                GameCardStack(
                    cards = uiState.cardUiModels,
                    selectedCardIndex = uiState.selectedCardIndex,
                    lastAppliedCardId = uiState.lastAppliedCardId,
                    maxWidth = constraintsWidth,
                    maxHeight = constraintsHeight,
                    isInteractionEnabled = !shouldHideCards,
                    onCardSelect = onCardSelect,
                    onCardDismiss = onCardDismiss,
                    onCardApply = onCardApply
                )
            }
            val expandedObjectiveLine = expandedObjectiveIndex?.let { uiState.objectiveLines.getOrNull(it) }
            AnimatedVisibility(
                visible = expandedObjectiveLine?.explainText != null,
                enter = fadeIn(tween(durationMillis = 120)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(durationMillis = 120)
                ),
                exit = fadeOut(tween(durationMillis = 120)) + scaleOut(
                    targetScale = 0.96f,
                    animationSpec = tween(durationMillis = 120)
                )
            ) {
                val explainText = expandedObjectiveLine?.explainText ?: return@AnimatedVisibility
                val explainTextValue = when (explainText) {
                    is ObjectiveLineText.StringRes -> {
                        stringResource(explainText.resId, *explainText.formatArgs.toTypedArray())
                    }
                    is ObjectiveLineText.PluralRes -> {
                        pluralStringResource(
                            explainText.resId,
                            explainText.quantity,
                            *explainText.formatArgs.toTypedArray()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(2f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { expandedObjectiveIndex = null },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 136.dp, start = 24.dp, end = 24.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .widthIn(max = 320.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = explainTextValue,
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (showSurrenderDialog) {
                GameAlertDialog(
                    title = stringResource(R.string.surrender_title),
                    message = stringResource(R.string.surrender_message),
                    confirmLabel = stringResource(R.string.surrender_confirm),
                    dismissLabel = stringResource(R.string.dialog_cancel),
                    confirmSoundEffect = SoundEffect.LOSS,
                    onConfirm = {
                        showSurrenderDialog = false
                        onConfirmSurrender()
                    },
                    onDismiss = { showSurrenderDialog = false }
                )
            }
            if (showExitDialog) {
                GameAlertDialog(
                    title = stringResource(R.string.exit_title),
                    message = stringResource(R.string.exit_message),
                    confirmLabel = stringResource(R.string.exit_confirm),
                    dismissLabel = stringResource(R.string.dialog_cancel),
                    onConfirm = {
                        showExitDialog = false
                        onConfirmExit()
                    },
                    onDismiss = { showExitDialog = false }
                )
            }
            if (uiState.showMinigamesAdPrompt) {
                GameAlertDialog(
                    title = stringResource(R.string.minigames_ad_title),
                    message = stringResource(R.string.minigames_ad_message),
                    confirmLabel = stringResource(R.string.minigames_ad_confirm),
                    dismissLabel = stringResource(R.string.dialog_cancel),
                    onConfirm = onConfirmMinigamesAd,
                    onDismiss = onDismissMinigamesAdPrompt
                )
            }
        }
    }
}

@Composable
private fun MinigamesAvailableBadge(
    minigamesAvailable: Int,
    onClick: () -> Unit
) {
    val badgeContentDescription = stringResource(R.string.cd_minigames_available)
    Box(
        modifier = Modifier
            .padding(end = 4.dp)
            .height(36.dp)
            .defaultMinSize(minWidth = 36.dp)
            .animateContentSize()
            .semantics { contentDescription = badgeContentDescription }
            .border(
                width = 1.5.dp,
                color = Color.White,
                shape = RoundedCornerShape(percent = 50)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = minigamesAvailable.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
    }
}

@Composable
private fun GameAlertDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    confirmSoundEffect: SoundEffect = SoundEffect.USE,
    dismissSoundEffect: SoundEffect = SoundEffect.USE,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    soundPlayer.play(confirmSoundEffect)
                    onConfirm()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    soundPlayer.play(dismissSoundEffect)
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(
                    text = dismissLabel,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    )
}

@Composable
private fun GameCardStack(
    cards: List<CardUiModel>,
    selectedCardIndex: Int?,
    lastAppliedCardId: CardId?,
    maxWidth: Dp,
    maxHeight: Dp,
    isInteractionEnabled: Boolean,
    onCardSelect: (Int) -> Unit,
    onCardDismiss: () -> Unit,
    onCardApply: (Int) -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    if (cards.isEmpty()) return
    val cardSize = DpSize(width = 220.dp, height = 300.dp)
    val peekHeight = 136.dp
    val centerX = (maxWidth - cardSize.width) / 2f
    val centerY = (maxHeight - cardSize.height) / 2f
    val bottomY = maxHeight - peekHeight
    val stackSpacing = 44.dp
    val rightPadding = 8.dp
    val maxCardTypes = remember { defaultCardUiModels().size }
    val startX = calculateCardStackStartX(
        cardsCount = cards.size,
        maxCardTypes = maxCardTypes,
        maxWidth = maxWidth,
        cardSize = cardSize,
        stackSpacing = stackSpacing,
        rightPadding = rightPadding
    )
    val stackRise = 8.dp

    cards.forEachIndexed { index, card ->
        val baseX = startX + stackSpacing * index.toFloat()
        val baseY = bottomY - stackRise * index.toFloat()
        val isSelected = selectedCardIndex == index
        val isSecondaryExpanded = selectedCardIndex != null && index == selectedCardIndex - 1
        val isRightmostExpanded = index == cards.lastIndex && !isSelected
        val isRepeatLast = card.id == CardId.REPEAT_LAST
        val repeatDescription = if (isRepeatLast) {
            val lastCardTitleRes = lastAppliedCardId?.let { cardTitleResForId(it) }
            val lastCardName = lastCardTitleRes?.let { stringResource(it) }
                ?: stringResource(R.string.card_repeat_last_none)
            buildAnnotatedString {
                append(stringResource(card.descriptionRes))
                append(" ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(lastCardName)
                }
            }
        } else {
            null
        }
        val targetX = if (isSelected) centerX else baseX
        val targetY = if (isSelected) centerY else baseY
        val animatedX by animateDpAsState(
            targetValue = targetX,
            animationSpec = tween(durationMillis = 220),
            label = "cardX"
        )
        val animatedY by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(durationMillis = 220),
            label = "cardY"
        )
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedX.roundToPx(), animatedY.roundToPx()) }
                .zIndex(if (isSelected) 2f else 1f + index * 0.01f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = isInteractionEnabled
                ) {
                    if (isSelected) {
                        if (shouldPlayCardDrawSound(isInteractionEnabled)) {
                            soundPlayer.play(SoundEffect.CARD_DRAW)
                        }
                        onCardDismiss()
                    } else {
                        if (shouldPlayCardDrawSound(isInteractionEnabled)) {
                            soundPlayer.play(SoundEffect.CARD_DRAW)
                        }
                        onCardSelect(index)
                    }
                }
                ) {
            CardItem(
                card = card,
                cardSize = cardSize,
                showDescription = isSelected || isSecondaryExpanded || isRightmostExpanded,
                showActionButton = isSelected || isSecondaryExpanded || isRightmostExpanded,
                showTitle = isSelected || isSecondaryExpanded || isRightmostExpanded,
                showCount = true,
                iconAlignment = if (isSelected || isSecondaryExpanded || isRightmostExpanded) {
                    Alignment.CenterHorizontally
                } else {
                    Alignment.Start
                },
                isEnabled = isInteractionEnabled,
                description = repeatDescription,
                descriptionTextAlign = if (isRepeatLast) TextAlign.Center else TextAlign.Start,
                onApplyClick = {
                    soundPlayer.play(SoundEffect.USE)
                    onCardApply(index)
                }
            )
        }
    }
}

internal fun calculateCardStackStartX(
    cardsCount: Int,
    maxCardTypes: Int,
    maxWidth: Dp,
    cardSize: DpSize,
    stackSpacing: Dp,
    rightPadding: Dp
): Dp {
    if (cardsCount <= 0) return 0.dp
    val stackedCards = (cardsCount - 1).coerceAtLeast(0)
    val stackWidth = cardSize.width + stackSpacing * stackedCards.toFloat()
    val centeredStartX = ((maxWidth - stackWidth) / 2f).coerceAtLeast(0.dp)
    val rightEdgeX = maxWidth - cardSize.width + rightPadding
    val rightAlignedStartX =
        (rightEdgeX - stackSpacing * stackedCards.toFloat()).coerceAtLeast(0.dp)
    return if (cardsCount >= maxCardTypes) rightAlignedStartX else centeredStartX
}

private fun cardTitleResForId(cardId: CardId): Int? {
    return when (cardId) {
        CardId.ADJUST_PLUS_MINUS_ONE -> R.string.card_adjust_plus_minus_one_title
        CardId.FLIP_FACE -> R.string.card_flip_face_title
        CardId.REROLL_SINGLE -> R.string.card_reroll_single_title
        CardId.REROLL_ALL -> R.string.card_reroll_all_title
        CardId.SET_VALUE -> R.string.card_set_value_title
        CardId.REPEAT_LAST -> R.string.card_repeat_last_title
        CardId.MINIGAMES -> R.string.card_minigames_title
    }
}

internal fun shouldPlayCardDrawSound(isInteractionEnabled: Boolean): Boolean {
    return isInteractionEnabled
}
