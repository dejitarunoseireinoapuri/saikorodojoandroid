package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import kotlin.random.Random

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
        onDiceClick = { index -> viewModel.onEvent(GameUiEvent.ToggleDiceSelection(index)) },
        onCardSelect = { index -> viewModel.onEvent(GameUiEvent.SelectCard(index)) },
        onCardDismiss = { viewModel.onEvent(GameUiEvent.DismissSelectedCard) },
        onCardApply = { index -> viewModel.onEvent(GameUiEvent.ApplyCard(index)) }
    )
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    uiState: GameUiState,
    onDiceClick: (Int) -> Unit,
    onCardSelect: (Int) -> Unit,
    onCardDismiss: () -> Unit,
    onCardApply: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        val constraintsWidth = maxWidth
        val constraintsHeight = maxHeight
        Text(
            text = stringResource(R.string.selected_dice_sum, uiState.selectedDiceSum),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        val diceCount = uiState.diceValues.size
        val diceSize = calculateDiceSize(
            availableWidth = maxWidth - 40.dp,
            availableHeight = 300.dp,
            diceCount = diceCount,
            spacing = 4.dp,
            columns = diceCount.coerceAtMost(2)
        ) * 0.89f
        val positions = remember(uiState.layoutSeed, maxWidth, diceCount) {
            calculateRandomDicePositions(
                seed = uiState.layoutSeed,
                diceCount = diceCount,
                availableWidth = maxWidth - 40.dp,
                availableHeight = 300.dp,
                diceSize = diceSize,
                minSpacing = 4.dp
            )
        }
        val diceFaces = remember(uiState.diceTypes, diceCount) {
            List(diceCount) { index ->
                diceTypeDrawable(uiState.diceTypes.getOrElse(index) { DiceType.D6 })
            }
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .height(300.dp)
                .fillMaxWidth()
                .zIndex(0f)
        ) {
            uiState.diceValues.forEachIndexed { index, value ->
                val position = positions.getOrNull(index) ?: DicePosition(0.dp, 0.dp)
                val faceDrawable = diceFaces.getOrElse(index) { diceTypeDrawable(DiceType.D6) }
                val isSelected = uiState.selectedDice.contains(index)
                Box(
                    modifier = Modifier
                        .offset(x = position.x, y = position.y)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDiceClick(index) }
                ) {
                    DiceFace(
                        number = value,
                        size = diceSize,
                        faceDrawable = faceDrawable,
                        isSelected = isSelected
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) {
            GameCardStack(
                cards = uiState.cardUiModels,
                selectedCardIndex = uiState.selectedCardIndex,
                maxWidth = constraintsWidth,
                maxHeight = constraintsHeight,
                onCardSelect = onCardSelect,
                onCardDismiss = onCardDismiss,
                onCardApply = onCardApply
            )
        }
    }
}

@Composable
private fun GameCardStack(
    cards: List<CardUiModel>,
    selectedCardIndex: Int?,
    maxWidth: Dp,
    maxHeight: Dp,
    onCardSelect: (Int) -> Unit,
    onCardDismiss: () -> Unit,
    onCardApply: (Int) -> Unit
) {
    if (cards.isEmpty()) return
    val cardSize = DpSize(width = 180.dp, height = 240.dp)
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
                    indication = null
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

@Composable
private fun DiceFace(number: Int, size: Dp, faceDrawable: Int, isSelected: Boolean) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceFaceDrawable(faceDrawable, isSelected)),
            contentDescription = stringResource(R.string.cd_dice_face, number)
        )
        Text(
            text = number.toString(),
            modifier = Modifier.offset(y = diceNumberYOffset(faceDrawable)),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

internal fun calculateDiceSize(
    availableWidth: Dp,
    availableHeight: Dp,
    diceCount: Int,
    spacing: Dp,
    columns: Int
): Dp {
    if (diceCount <= 0) return 0.dp
    val safeColumns = columns.coerceAtLeast(1)
    val rows = ((diceCount + safeColumns - 1) / safeColumns).coerceAtLeast(1)
    val widthBasedSize = (availableWidth - spacing * (safeColumns - 1)) / safeColumns
    val heightBasedSize = (availableHeight - spacing * (rows - 1)) / rows
    return minOf(widthBasedSize, heightBasedSize)
}

internal data class DicePosition(val x: Dp, val y: Dp)

internal fun calculateRandomDicePositions(
    seed: Long,
    diceCount: Int,
    availableWidth: Dp,
    availableHeight: Dp,
    diceSize: Dp,
    minSpacing: Dp
): List<DicePosition> {
    if (diceCount <= 0) return emptyList()
    val cellSize = diceSize + minSpacing
    val columns = ((availableWidth + minSpacing) / cellSize).toInt().coerceAtLeast(1)
    val rows = ((availableHeight + minSpacing) / cellSize).toInt().coerceAtLeast(1)
    val totalCells = columns * rows
    val random = Random(seed)
    val indices = List(totalCells) { it }.shuffled(random)
    val jitterXLimit = (minSpacing / 2f).coerceAtLeast(0.dp)
    val jitterYLimit = (minSpacing / 2f).coerceAtLeast(0.dp)
    return List(minOf(diceCount, totalCells)) { index ->
        val cellIndex = indices[index]
        val row = cellIndex / columns
        val column = cellIndex % columns
        val baseX = (cellSize * column).coerceAtMost(availableWidth - diceSize)
        val baseY = (cellSize * row).coerceAtMost(availableHeight - diceSize)
        val jitterX = ((random.nextFloat() - 0.5f) * 2f * jitterXLimit.value).dp
        val jitterY = ((random.nextFloat() - 0.5f) * 2f * jitterYLimit.value).dp
        DicePosition(
            x = (baseX + jitterX).coerceIn(0.dp, availableWidth - diceSize),
            y = (baseY + jitterY).coerceIn(0.dp, availableHeight - diceSize)
        )
    }
}

internal fun diceNumberYOffset(faceDrawable: Int): Dp {
    return if (faceDrawable == R.drawable.eigth_sides) {
        6.dp
    } else {
        0.dp
    }
}

internal fun diceTypeDrawable(diceType: DiceType): Int {
    return when (diceType) {
        DiceType.D6 -> R.drawable.six_sides
        DiceType.D8 -> R.drawable.eigth_sides
        DiceType.D10 -> R.drawable.ten_sides
    }
}

internal fun diceFaceDrawable(faceDrawable: Int, isSelected: Boolean): Int {
    if (!isSelected) return faceDrawable
    return when (faceDrawable) {
        R.drawable.six_sides -> R.drawable.six_sides_selected
        R.drawable.eigth_sides -> R.drawable.eigth_sides_selected
        R.drawable.ten_sides -> R.drawable.ten_sides_selected
        else -> faceDrawable
    }
}
