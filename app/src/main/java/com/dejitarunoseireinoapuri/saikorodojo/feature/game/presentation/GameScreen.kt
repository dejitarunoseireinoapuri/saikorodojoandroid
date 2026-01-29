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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.alpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.ui.ads.BannerAd
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoThemeColors

@Composable
fun GameRoute(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onEvent(GameUiEvent.StartRoll)
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
        }
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
    onSetSelectedDieValue: (Int) -> Unit
) {
    var containerModifier = modifier
        .fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    val gradientColors = SaikoroDojoThemeColors.gradientColors
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            gradientColors.menuGameTop,
            gradientColors.menuGameMiddle,
            gradientColors.menuGameBottom
        )
    )
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(backgroundBrush)
    Column(modifier = containerModifier) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val constraintsWidth = maxWidth
            val constraintsHeight = maxHeight
            val shouldHideCards = uiState.isAwaitingRerollSingle ||
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
            DiceBoard(
                modifier = Modifier.zIndex(0f),
                maxWidth = maxWidth,
                uiState = uiState,
                onDiceClick = onDiceClick,
                onAdjustSelectedDie = onAdjustSelectedDie,
                onSetSelectedDieValue = onSetSelectedDieValue
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
        }
        BannerAd(modifier = Modifier.fillMaxWidth())
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
                .offset(x = animatedX, y = animatedY)
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
