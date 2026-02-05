package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Style
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType

@Composable
fun GameRoute(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
    onNavigateToMinigame: (MinigameType) -> Unit,
    onNavigateToMenu: (Boolean) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(GameUiEvent.StartRoll)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is GameUiEffect.NavigateToMinigame -> onNavigateToMinigame(effect.minigame)
                is GameUiEffect.NavigateToMenu -> onNavigateToMenu(effect.resetProgress)
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
        onConfirmSurrender = { viewModel.onEvent(GameUiEvent.ConfirmSurrender) },
        onConfirmExit = { viewModel.onEvent(GameUiEvent.ConfirmExit) },
        onOpenRandomMinigame = { viewModel.onEvent(GameUiEvent.OpenRandomMinigame) }
    )
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
    onConfirmSurrender: () -> Unit,
    onConfirmExit: () -> Unit,
    onOpenRandomMinigame: () -> Unit
) {
    var containerModifier = modifier
        .fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    BoxWithConstraints(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        var showSurrenderDialog by remember { mutableStateOf(false) }
        var showExitDialog by remember { mutableStateOf(false) }
        val constraintsWidth = maxWidth
        val constraintsHeight = maxHeight
        val shouldHideCards = uiState.isAwaitingRerollSingle ||
            uiState.isAwaitingRerollSelected ||
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
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .widthIn(max = 320.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.level_title, uiState.levelNumber),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                uiState.objectiveLines.forEach { line ->
                    Text(
                        text = stringResource(line.textRes, *line.formatArgs.toTypedArray()),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (line.isMet) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp, start = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                IconButton(onClick = { showExitDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = stringResource(R.string.cd_exit_home),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenRandomMinigame) {
                    Icon(
                        imageVector = Icons.Outlined.Style,
                        contentDescription = stringResource(R.string.cd_random_minigame),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = { showSurrenderDialog = true }) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = stringResource(R.string.cd_surrender),
                        tint = Color.White
                    )
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
                onRollSingleDie = onRollSingleDie
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .clipToBounds()
                    .zIndex(1f)
                    .offset(y = stackOffset)
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
            if (showSurrenderDialog) {
                AlertDialog(
                    onDismissRequest = { showSurrenderDialog = false },
                    title = { Text(text = stringResource(R.string.surrender_title)) },
                    text = { Text(text = stringResource(R.string.surrender_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showSurrenderDialog = false
                            onConfirmSurrender()
                        }) {
                            Text(text = stringResource(R.string.surrender_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSurrenderDialog = false }) {
                            Text(text = stringResource(R.string.dialog_cancel))
                        }
                    }
                )
            }
            if (showExitDialog) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text(text = stringResource(R.string.exit_title)) },
                    text = { Text(text = stringResource(R.string.exit_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            onConfirmExit()
                        }) {
                            Text(text = stringResource(R.string.exit_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text(text = stringResource(R.string.dialog_cancel))
                        }
                    }
                )
            }
        }
    }
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
    if (cards.isEmpty()) return
    val cardSize = DpSize(width = 208.dp, height = 278.dp)
    val peekHeight = 120.dp
    val centerX = (maxWidth - cardSize.width) / 2f
    val centerY = (maxHeight - cardSize.height) / 2f
    val bottomY = maxHeight - peekHeight
    val stackSpacing = 40.dp
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
                        onCardDismiss()
                    } else {
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
                onApplyClick = { onCardApply(index) }
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
        CardId.RETRY -> R.string.card_retry_title
    }
}
