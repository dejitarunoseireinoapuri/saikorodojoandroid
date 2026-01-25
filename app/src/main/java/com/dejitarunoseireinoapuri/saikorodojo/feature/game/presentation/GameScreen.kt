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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R

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
                availableHeight = 100.dp,
                diceCount = diceCount,
                spacing = 4.dp
            )
            val positions = calculateDicePositions(
                diceCount = diceCount,
                availableWidth = maxWidth - 16.dp,
                availableHeight = 100.dp,
                diceSize = diceSize,
                spacing = 4.dp
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(100.dp)
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
    spacing: Dp
): Dp {
    if (diceCount <= 0) return 0.dp
    val totalSpacing = spacing * (diceCount - 1)
    val widthBasedSize = (availableWidth - totalSpacing) / diceCount
    return minOf(widthBasedSize, availableHeight)
}

private data class DicePosition(val x: Dp, val y: Dp)

private fun calculateDicePositions(
    diceCount: Int,
    availableWidth: Dp,
    availableHeight: Dp,
    diceSize: Dp,
    spacing: Dp
): List<DicePosition> {
    if (diceCount <= 0) return emptyList()
    val totalWidth = diceSize * diceCount + spacing * (diceCount - 1)
    val startX = ((availableWidth - totalWidth) / 2).coerceAtLeast(0.dp)
    val verticalSpread = (availableHeight - diceSize).coerceAtLeast(0.dp)
    val yOffsets = listOf(0.dp, verticalSpread / 2, verticalSpread)
    return List(diceCount) { index ->
        DicePosition(
            x = startX + (diceSize + spacing) * index,
            y = yOffsets[index % yOffsets.size]
        )
    }
}
