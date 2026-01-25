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
                availableWidth = maxWidth - 40.dp,
                availableHeight = 300.dp,
                diceCount = diceCount,
                spacing = 4.dp,
                columns = diceCount.coerceAtMost(2)
            ) * 0.89f
            val positions = remember(maxWidth, diceCount) {
                calculateGridPositions(
                    diceCount = diceCount,
                    columns = diceCount.coerceAtMost(2),
                    diceSize = diceSize,
                    minSpacing = 4.dp,
                    availableWidth = maxWidth - 40.dp,
                    availableHeight = 300.dp
                )
            }
            val diceFaces = remember(uiState.layoutSeed, diceCount) {
                selectDiceFaceDrawables(
                    seed = uiState.layoutSeed,
                    diceCount = diceCount,
                    faces = DiceFaceDrawables
                )
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .height(300.dp)
                    .fillMaxWidth()
            ) {
                uiState.diceValues.forEachIndexed { index, value ->
                    val position = positions.getOrNull(index) ?: DicePosition(0.dp, 0.dp)
                    val faceDrawable = diceFaces.getOrElse(index) { DiceFaceDrawables.first() }
                    Box(modifier = Modifier.offset(x = position.x, y = position.y)) {
                        DiceFace(number = value, size = diceSize, faceDrawable = faceDrawable)
                    }
                }
            }
        }
    }
}

private val DiceFaceDrawables = listOf(
    R.drawable.six_sides,
    R.drawable.eigth_sides,
    R.drawable.ten_sides
)

@Composable
private fun DiceFace(number: Int, size: Dp, faceDrawable: Int) {
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = faceDrawable),
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

internal data class DicePosition(val x: Dp, val y: Dp)

internal fun calculateGridPositions(
    diceCount: Int,
    columns: Int,
    diceSize: Dp,
    minSpacing: Dp,
    availableWidth: Dp,
    availableHeight: Dp
): List<DicePosition> {
    if (diceCount <= 0) return emptyList()
    val safeColumns = columns.coerceAtLeast(1)
    val cellSize = diceSize + minSpacing
    return List(diceCount) { index ->
        val row = index / safeColumns
        val column = index % safeColumns
        val x = (cellSize * column).coerceAtMost(availableWidth - diceSize)
        val y = (cellSize * row).coerceAtMost(availableHeight - diceSize)
        DicePosition(x, y)
    }
}

internal fun selectDiceFaceDrawables(
    seed: Long,
    diceCount: Int,
    faces: List<Int>
): List<Int> {
    if (diceCount <= 0 || faces.isEmpty()) return emptyList()
    val random = Random(seed)
    return List(diceCount) { faces[random.nextInt(faces.size)] }
}

internal fun diceNumberYOffset(faceDrawable: Int): Dp {
    return if (faceDrawable == R.drawable.eigth_sides) {
        6.dp
    } else {
        0.dp
    }
}
