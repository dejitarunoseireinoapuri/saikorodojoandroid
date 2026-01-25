package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
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
        uiState = uiState
    )
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    uiState: GameUiState
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints {
            val diceCount = uiState.diceValues.size
            val diceSize = calculateDiceSize(
                availableWidth = maxWidth - 16.dp,
                availableHeight = 300.dp,
                diceCount = diceCount,
                spacing = 4.dp,
                columns = diceCount.coerceAtMost(2)
            ) * 0.67f
            val positions = remember(uiState.layoutSeed, maxWidth, diceCount) {
                calculateRandomDicePositions(
                    seed = uiState.layoutSeed,
                    diceCount = diceCount,
                    availableWidth = maxWidth - 16.dp,
                    availableHeight = 300.dp,
                    diceSize = diceSize,
                    minSpacing = 2.dp
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                uiState.diceValues.forEachIndexed { index, value ->
                    val position = positions.getOrNull(index) ?: DicePosition(0.dp, 0.dp)
                    Box(modifier = Modifier.offset(x = position.x, y = position.y)) {
                        DiceFace(number = value, size = diceSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun DiceFace(number: Int, size: Dp) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.six_sides),
            contentDescription = stringResource(R.string.cd_dice_face, number)
        )
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun calculateDiceSize(
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

private data class DicePosition(val x: Dp, val y: Dp)

private fun calculateRandomDicePositions(
    seed: Long,
    diceCount: Int,
    availableWidth: Dp,
    availableHeight: Dp,
    diceSize: Dp,
    minSpacing: Dp
): List<DicePosition> {
    if (diceCount <= 0) return emptyList()
    val random = Random(seed)
    val maxX = (availableWidth - diceSize).coerceAtLeast(0.dp)
    val maxY = (availableHeight - diceSize).coerceAtLeast(0.dp)
    val positions = mutableListOf<DicePosition>()
    repeat(diceCount) { index ->
        var placed = false
        repeat(60) {
            val candidate = DicePosition(
                x = (maxX.value * random.nextFloat()).dp,
                y = (maxY.value * random.nextFloat()).dp
            )
            if (positions.none { overlaps(candidate, it, diceSize, minSpacing) }) {
                positions.add(candidate)
                placed = true
                return@repeat
            }
        }
        if (!placed) {
            val fallback = fallbackGridPosition(
                index = index,
                diceSize = diceSize,
                minSpacing = minSpacing,
                availableWidth = availableWidth,
                availableHeight = availableHeight
            )
            positions.add(fallback)
        }
    }
    return positions
}

private fun fallbackGridPosition(
    index: Int,
    diceSize: Dp,
    minSpacing: Dp,
    availableWidth: Dp,
    availableHeight: Dp
): DicePosition {
    val cellSize = diceSize + minSpacing
    val columns = ((availableWidth + minSpacing) / cellSize).toInt().coerceAtLeast(1)
    val rows = ((availableHeight + minSpacing) / cellSize).toInt().coerceAtLeast(1)
    val safeIndex = index % (columns * rows)
    val row = safeIndex / columns
    val column = safeIndex % columns
    val x = (cellSize * column).coerceAtMost(availableWidth - diceSize)
    val y = (cellSize * row).coerceAtMost(availableHeight - diceSize)
    return DicePosition(x, y)
}

private fun overlaps(
    first: DicePosition,
    second: DicePosition,
    diceSize: Dp,
    minSpacing: Dp
): Boolean {
    val sizeWithSpacing = diceSize + minSpacing
    val firstRight = first.x + sizeWithSpacing
    val firstBottom = first.y + sizeWithSpacing
    val secondRight = second.x + sizeWithSpacing
    val secondBottom = second.y + sizeWithSpacing
    val overlapsHorizontally = first.x < secondRight && firstRight > second.x
    val overlapsVertically = first.y < secondBottom && firstBottom > second.y
    return overlapsHorizontally && overlapsVertically
}
