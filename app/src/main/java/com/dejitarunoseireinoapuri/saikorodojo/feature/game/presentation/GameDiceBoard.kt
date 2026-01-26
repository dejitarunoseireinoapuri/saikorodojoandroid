package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import kotlin.random.Random

@Composable
internal fun DiceBoard(
    modifier: Modifier = Modifier,
    maxWidth: Dp,
    uiState: GameUiState,
    onDiceClick: (Int) -> Unit
) {
    val diceCount = uiState.diceValues.size
    val boardHeight = 300.dp
    val horizontalMargin = 20.dp
    val contentPadding = 16.dp
    val boardWidth = maxWidth - horizontalMargin * 2
    val contentSize = calculateBoardContentSize(
        containerWidth = boardWidth,
        containerHeight = boardHeight,
        horizontalPadding = contentPadding,
        verticalPadding = contentPadding
    )
    val diceSize = calculateDiceSize(
        availableWidth = contentSize.width,
        availableHeight = contentSize.height,
        diceCount = diceCount,
        spacing = 4.dp,
        columns = diceCount.coerceAtMost(2)
    ) * 0.89f
    val positions = remember(uiState.layoutSeed, maxWidth, diceCount) {
        calculateRandomDicePositions(
            seed = uiState.layoutSeed,
            diceCount = diceCount,
            availableWidth = contentSize.width,
            availableHeight = contentSize.height,
            diceSize = diceSize,
            minSpacing = 4.dp
        )
    }
    val diceFaces = remember(uiState.diceTypes, diceCount) {
        List(diceCount) { index ->
            diceTypeDrawable(uiState.diceTypes.getOrElse(index) { DiceType.D6 })
        }
    }
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (uiState.isAwaitingRerollSingle) {
            Text(
                text = stringResource(R.string.select_die_to_reroll),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -(150.dp + 8.dp)),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = horizontalMargin, end = horizontalMargin)
                .height(boardHeight)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(contentPadding),
            contentAlignment = Alignment.TopStart
        ) {
            uiState.diceValues.forEachIndexed { index, value ->
                val position = positions.getOrNull(index) ?: DicePosition(0.dp, 0.dp)
                val faceDrawable = diceFaces.getOrElse(index) { diceTypeDrawable(DiceType.D6) }
                val isSelected = uiState.selectedDice.contains(index)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
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
    }
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
            color = Color.White
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

internal fun calculateBoardContentSize(
    containerWidth: Dp,
    containerHeight: Dp,
    horizontalPadding: Dp,
    verticalPadding: Dp
): DpSize {
    val width = (containerWidth - horizontalPadding * 2).coerceAtLeast(0.dp)
    val height = (containerHeight - verticalPadding * 2).coerceAtLeast(0.dp)
    return DpSize(width, height)
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
