package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
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
    onDiceClick: (Int) -> Unit,
    onAdjustSelectedDie: (Int) -> Unit,
    onSetSelectedDieValue: (Int) -> Unit
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
        val promptOffset = -(boardHeight / 2 + 32.dp)
        when {
            uiState.isAwaitingRerollSingle -> {
                Text(
                    text = stringResource(R.string.select_die_to_reroll),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            uiState.isAwaitingAdjustPlusMinus -> {
                Text(
                    text = stringResource(R.string.select_die_to_modify),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState.selectedAdjustmentDieIndex != null) {
                    val selectedIndex = uiState.selectedAdjustmentDieIndex
                    val selectedValue = uiState.diceValues.getOrNull(selectedIndex) ?: 1
                    val selectedType = uiState.diceTypes.getOrElse(selectedIndex) { DiceType.D6 }
                    val availability = adjustActionAvailability(
                        value = selectedValue,
                        diceType = selectedType
                    )
                    val adjustOffset = boardHeight / 2 + diceSize + 24.dp
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = adjustOffset)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (availability.canDecrease) {
                            DiceOption(
                                value = (selectedValue - 1).coerceAtLeast(1),
                                faceDrawable = diceTypeOptionDrawable(selectedType),
                                size = diceSize,
                                numberTextScale = 1f,
                                onClick = { onAdjustSelectedDie(-1) }
                            )
                        }
                        if (availability.canIncrease) {
                            DiceOption(
                                value = (selectedValue + 1).coerceAtMost(selectedType.sides),
                                faceDrawable = diceTypeOptionDrawable(selectedType),
                                size = diceSize,
                                numberTextScale = 1f,
                                onClick = { onAdjustSelectedDie(1) }
                            )
                        }
                    }
                }
            }
            uiState.isAwaitingSetValue -> {
                Text(
                    text = stringResource(R.string.select_die_to_modify),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState.selectedSetValueDieIndex != null) {
                    val selectedIndex = uiState.selectedSetValueDieIndex
                    val selectedType = uiState.diceTypes.getOrElse(selectedIndex) { DiceType.D6 }
                    val optionCount = selectedType.sides
                    val optionsPerRow = (optionCount / 2).coerceAtLeast(1)
                    val optionSpacing = 6.dp
                    val availableWidth = (maxWidth - horizontalMargin * 2).coerceAtLeast(0.dp)
                    val rowSize = calculateRowDiceSize(availableWidth, optionsPerRow, optionSpacing)
                    val optionSize = minOf(rowSize, diceSize)
                    val textScale = if (diceSize.value == 0f) 1f else {
                        (optionSize.value / diceSize.value).coerceAtMost(1f)
                    }
                    val setValueOffset = boardHeight / 2 + optionSize + 24.dp
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = setValueOffset)
                            .fillMaxWidth()
                            .padding(horizontal = horizontalMargin),
                        verticalArrangement = Arrangement.spacedBy(optionSpacing),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(2) { rowIndex ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(
                                    optionSpacing,
                                    Alignment.CenterHorizontally
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val startValue = rowIndex * optionsPerRow + 1
                                val endValue = minOf(startValue + optionsPerRow - 1, optionCount)
                                for (value in startValue..endValue) {
                                    DiceOption(
                                        value = value,
                                        faceDrawable = diceTypeOptionDrawable(selectedType),
                                        size = optionSize,
                                        numberTextScale = textScale,
                                        onClick = { onSetSelectedDieValue(value) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
                val isAdjustmentSelected = uiState.selectedAdjustmentDieIndex == index
                val isSetValueSelected = uiState.selectedSetValueDieIndex == index
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
                        isSelected = isSelected,
                        isAdjustmentSelected = isAdjustmentSelected || isSetValueSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun DiceFace(
    number: Int,
    size: Dp,
    faceDrawable: Int,
    isSelected: Boolean,
    isAdjustmentSelected: Boolean,
    numberTextScale: Float = 1f
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (isAdjustmentSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceFaceDrawable(faceDrawable, isSelected)),
            contentDescription = stringResource(R.string.cd_dice_face, number)
        )
        Text(
            text = number.toString(),
            modifier = Modifier.offset(y = diceNumberYOffset(faceDrawable)),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = MaterialTheme.typography.displaySmall.fontSize * numberTextScale
            ),
            color = Color.White
        )
    }
}

@Composable
private fun DiceOption(
    value: Int,
    faceDrawable: Int,
    size: Dp,
    numberTextScale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() }
    ) {
        DiceFace(
            number = value,
            size = size,
            faceDrawable = faceDrawable,
            isSelected = false,
            isAdjustmentSelected = false,
            numberTextScale = numberTextScale
        )
    }
}

internal data class AdjustActionAvailability(
    val canIncrease: Boolean,
    val canDecrease: Boolean
)

internal fun adjustActionAvailability(value: Int, diceType: DiceType): AdjustActionAvailability {
    return AdjustActionAvailability(
        canIncrease = value < diceType.sides,
        canDecrease = value > 1
    )
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

internal fun calculateRowDiceSize(
    availableWidth: Dp,
    diceCount: Int,
    spacing: Dp
): Dp {
    if (diceCount <= 0) return 0.dp
    val totalSpacing = spacing * (diceCount - 1).coerceAtLeast(0)
    return ((availableWidth - totalSpacing) / diceCount).coerceAtLeast(0.dp)
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
    val gridWidth = (diceSize * columns) + (minSpacing * (columns - 1).coerceAtLeast(0))
    val gridHeight = (diceSize * rows) + (minSpacing * (rows - 1).coerceAtLeast(0))
    val horizontalInset = ((availableWidth - gridWidth) / 2f).coerceAtLeast(0.dp)
    val verticalInset = ((availableHeight - gridHeight) / 2f).coerceAtLeast(0.dp)
    val random = Random(seed)
    val indices = List(totalCells) { it }.shuffled(random)
    val jitterXLimit = (minSpacing / 2f).coerceAtLeast(0.dp)
    val jitterYLimit = (minSpacing / 2f).coerceAtLeast(0.dp)
    return List(minOf(diceCount, totalCells)) { index ->
        val cellIndex = indices[index]
        val row = cellIndex / columns
        val column = cellIndex % columns
        val baseX = (horizontalInset + cellSize * column).coerceAtMost(availableWidth - diceSize)
        val baseY = (verticalInset + cellSize * row).coerceAtMost(availableHeight - diceSize)
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

internal fun diceTypeOptionDrawable(diceType: DiceType): Int {
    return when (diceType) {
        DiceType.D6 -> R.drawable.six_sides_green
        DiceType.D8 -> R.drawable.eigth_sides_green
        DiceType.D10 -> R.drawable.ten_sides_green
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
