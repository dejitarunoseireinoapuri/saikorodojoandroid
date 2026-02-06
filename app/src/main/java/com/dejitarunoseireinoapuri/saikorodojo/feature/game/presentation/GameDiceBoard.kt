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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceOptionNumberColor
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import kotlin.random.Random

@Composable
internal fun DiceBoard(
    modifier: Modifier = Modifier,
    maxWidth: Dp,
    uiState: GameUiState,
    onDiceClick: (Int) -> Unit,
    onAdjustSelectedDie: (Int) -> Unit,
    onSetSelectedDieValue: (Int) -> Unit,
    onRollSelectedDice: () -> Unit,
    onRollSingleDie: () -> Unit,
    onFlipSelectedDie: () -> Unit
) {
    val diceCount = uiState.diceValues.size
    val boardHeight = 276.dp
    val horizontalMargin = 20.dp
    val contentPadding = 16.dp
    val boardWidth = maxWidth - horizontalMargin * 2
    val contentSize = calculateBoardContentSize(
        containerWidth = boardWidth,
        containerHeight = boardHeight,
        horizontalPadding = contentPadding,
        verticalPadding = contentPadding
    )
    val diceSpacing = 4.dp
    val gridSpec = remember(diceCount, contentSize) {
        calculateDiceGridSpec(
            availableWidth = contentSize.width,
            availableHeight = contentSize.height,
            diceCount = diceCount,
            spacing = diceSpacing
        )
    }
    val diceSize = gridSpec.diceSize
    val positions = remember(diceCount, contentSize, gridSpec) {
        calculatePackedDicePositions(
            diceCount = diceCount,
            availableWidth = contentSize.width,
            availableHeight = contentSize.height,
            diceSize = diceSize,
            spacing = diceSpacing,
            columns = gridSpec.columns,
            rows = gridSpec.rows,
            seed = uiState.layoutSeed
        )
    }
    val diceFaces = remember(uiState.diceTypes, diceCount) {
        List(diceCount) { index ->
            diceTypeDrawable(uiState.diceTypes.getOrElse(index) { DiceType.D6 })
        }
    }
    val boardYOffset = 24.dp
    val diceTextScale = calculateDiceTextScale(diceSize)
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (uiState.selectedDice.isNotEmpty()) {
            val sumOffset = -(boardHeight / 2 + 42.dp)
            val selectionText = if (uiState.shouldShowSelectedSum) {
                stringResource(R.string.selected_dice_sum, uiState.selectedDiceSum)
            } else {
                stringResource(
                    R.string.selected_dice_count,
                    uiState.selectedDice.size,
                    uiState.diceValues.size
                )
            }
            Text(
                text = selectionText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = sumOffset),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        val promptOffset = -(boardHeight / 2 + 10.dp)
        when {
            uiState.isAwaitingRerollSingle -> {
                Text(
                    text = stringResource(R.string.select_die_to_reroll),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            uiState.isAwaitingRerollSelected -> {
                Text(
                    text = stringResource(R.string.select_dice_to_reroll),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            uiState.isAwaitingFlipFace -> {
                Text(
                    text = stringResource(R.string.select_die_to_flip),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            uiState.isAwaitingAdjustPlusMinus -> {
                Text(
                    text = stringResource(R.string.select_die_to_modify),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = promptOffset),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
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
                                numberTextColor = diceOptionNumberColor(),
                                onClick = { onAdjustSelectedDie(-1) }
                            )
                        }
                        if (availability.canIncrease) {
                            DiceOption(
                                value = (selectedValue + 1).coerceAtMost(selectedType.sides),
                                faceDrawable = diceTypeOptionDrawable(selectedType),
                                size = diceSize,
                                numberTextScale = 1f,
                                numberTextColor = diceOptionNumberColor(),
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
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uiState.selectedSetValueDieIndex != null) {
                    val selectedIndex = uiState.selectedSetValueDieIndex
                    val selectedType = uiState.diceTypes.getOrElse(selectedIndex) { DiceType.D6 }
                    val currentValue = uiState.diceValues.getOrNull(selectedIndex) ?: 1
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
                                val values = setValueRowValues(
                                    optionCount = optionCount,
                                    optionsPerRow = optionsPerRow,
                                    rowIndex = rowIndex,
                                    currentValue = currentValue
                                )
                                values.forEach { value ->
                                    if (value == null) {
                                        Box(modifier = Modifier.size(optionSize))
                                    } else {
                                        DiceOption(
                                            value = value,
                                            faceDrawable = diceTypeOptionDrawable(selectedType),
                                            size = optionSize,
                                            numberTextScale = textScale,
                                            numberTextColor = diceOptionNumberColor(),
                                            onClick = { onSetSelectedDieValue(value) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (uiState.isAwaitingRerollSelected || uiState.isAwaitingRerollSingle) {
            val buttonOffset = boardHeight / 2 + 48.dp
            val isEnabled = when {
                uiState.isAwaitingRerollSelected -> uiState.selectedRerollDice.isNotEmpty()
                else -> uiState.selectedRerollSingleDieIndex != null
            }
            val onClick = if (uiState.isAwaitingRerollSelected) onRollSelectedDice else onRollSingleDie
            Button(
                onClick = onClick,
                enabled = isEnabled && !uiState.isRolling,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = buttonOffset)
                    .padding(top = 8.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.roll_selected_dice),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        if (uiState.isAwaitingFlipFace) {
            val buttonOffset = boardHeight / 2 + 48.dp
            Button(
                onClick = onFlipSelectedDie,
                enabled = uiState.selectedFlipDieIndex != null && !uiState.isRolling,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = buttonOffset)
                    .padding(top = 8.dp)
                    .height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.flip_selected_die),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = horizontalMargin, end = horizontalMargin)
                .offset(y = boardYOffset)
                .height(boardHeight)
                .fillMaxWidth()
                .background(
                    color = SequenceSaveMatBackground,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(
                    width = 1.dp,
                    color = SequenceSaveMatBorder,
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
                val isRerollSingleSelected = uiState.selectedRerollSingleDieIndex == index
                val isFlipSelected = uiState.selectedFlipDieIndex == index
                val showSelectionBorder = shouldShowDiceSelectionBorder(
                    isAwaitingRerollSelected = uiState.isAwaitingRerollSelected,
                    isAwaitingRerollSingle = uiState.isAwaitingRerollSingle,
                    isAwaitingFlipFace = uiState.isAwaitingFlipFace,
                    isAwaitingAdjustPlusMinus = uiState.isAwaitingAdjustPlusMinus,
                    isAwaitingSetValue = uiState.isAwaitingSetValue,
                    isRerollSelected = uiState.selectedRerollDice.contains(index),
                    isAdjustmentSelected = isAdjustmentSelected,
                    isSetValueSelected = isSetValueSelected,
                    isRerollSingleSelected = isRerollSingleSelected,
                    isFlipSelected = isFlipSelected
                )
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
                        showSelectionBorder = showSelectionBorder,
                        numberTextScale = diceTextScale
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
    showSelectionBorder: Boolean,
    showSelectedFace: Boolean = true,
    numberTextScale: Float = 1f,
    numberTextColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showSelectionBorder) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceFaceDrawable(faceDrawable, isSelected && showSelectedFace)),
            contentDescription = stringResource(R.string.cd_dice_face, number)
        )
        Text(
            text = number.toString(),
            modifier = Modifier.offset(y = diceNumberYOffset(faceDrawable)),
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = MaterialTheme.typography.displaySmall.fontSize * numberTextScale
            ),
            color = numberTextColor
        )
    }
}

@Composable
private fun DiceOption(
    value: Int,
    faceDrawable: Int,
    size: Dp,
    numberTextScale: Float,
    numberTextColor: Color,
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
            showSelectionBorder = false,
            numberTextScale = numberTextScale,
            numberTextColor = numberTextColor
        )
    }
}

internal fun diceOptionNumberColor(): Color = DiceOptionNumberColor

internal fun shouldShowDiceSelectionBorder(
    isAwaitingRerollSelected: Boolean,
    isAwaitingRerollSingle: Boolean,
    isAwaitingFlipFace: Boolean,
    isAwaitingAdjustPlusMinus: Boolean,
    isAwaitingSetValue: Boolean,
    isRerollSelected: Boolean,
    isAdjustmentSelected: Boolean,
    isSetValueSelected: Boolean,
    isRerollSingleSelected: Boolean,
    isFlipSelected: Boolean
): Boolean {
    return when {
        isAwaitingRerollSelected -> isRerollSelected
        isAwaitingRerollSingle -> isRerollSingleSelected
        isAwaitingFlipFace -> isFlipSelected
        isAwaitingAdjustPlusMinus -> isAdjustmentSelected
        isAwaitingSetValue -> isSetValueSelected
        else -> false
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

internal data class DiceGridSpec(
    val columns: Int,
    val rows: Int,
    val diceSize: Dp
)

internal fun calculateDiceGridSpec(
    availableWidth: Dp,
    availableHeight: Dp,
    diceCount: Int,
    spacing: Dp
): DiceGridSpec {
    if (diceCount <= 0) {
        return DiceGridSpec(columns = 0, rows = 0, diceSize = 0.dp)
    }
    var bestColumns = 1
    var bestRows = diceCount
    var bestSize = 0.dp
    var bestDiff = Int.MAX_VALUE
    var bestCapacity = Int.MAX_VALUE
    for (columns in 1..diceCount) {
        val rows = ((diceCount + columns - 1) / columns).coerceAtLeast(1)
        if (diceCount >= 4 && (rows < 2 || columns < 2)) continue
        val widthBasedSize = (availableWidth - spacing * (columns - 1)) / columns
        val heightBasedSize = (availableHeight - spacing * (rows - 1)) / rows
        val size = minOf(widthBasedSize, heightBasedSize)
        if (size <= 0.dp) continue
        val diff = kotlin.math.abs(columns - rows)
        val capacity = columns * rows
        if (diff < bestDiff ||
            (diff == bestDiff && size > bestSize) ||
            (diff == bestDiff && size == bestSize && capacity < bestCapacity)
        ) {
            bestDiff = diff
            bestSize = size
            bestColumns = columns
            bestRows = rows
            bestCapacity = capacity
        }
    }
    return DiceGridSpec(columns = bestColumns, rows = bestRows, diceSize = bestSize)
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

internal fun calculateDiceSize(
    availableWidth: Dp,
    availableHeight: Dp,
    diceCount: Int,
    spacing: Dp,
    columns: Int
): Dp {
    if (diceCount <= 0 || columns <= 0) return 0.dp
    val rows = ((diceCount + columns - 1) / columns).coerceAtLeast(1)
    val widthBasedSize = (availableWidth - spacing * (columns - 1).coerceAtLeast(0)) / columns
    val heightBasedSize = (availableHeight - spacing * (rows - 1).coerceAtLeast(0)) / rows
    return minOf(widthBasedSize, heightBasedSize).coerceAtLeast(0.dp)
}

internal fun setValueRowValues(
    optionCount: Int,
    optionsPerRow: Int,
    rowIndex: Int,
    currentValue: Int
): List<Int?> {
    if (optionCount <= 0 || optionsPerRow <= 0 || rowIndex < 0) return emptyList()
    val startValue = rowIndex * optionsPerRow + 1
    val endValue = minOf(startValue + optionsPerRow - 1, optionCount)
    if (startValue > optionCount) return emptyList()
    return (startValue..endValue).map { value ->
        if (value == currentValue) null else value
    }
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
    if (diceCount <= 0 || diceSize <= 0.dp) return emptyList()
    val sizeWithSpacing = diceSize + minSpacing
    val columns = ((availableWidth + minSpacing) / sizeWithSpacing).toInt().coerceAtLeast(1)
    val rows = ((availableHeight + minSpacing) / sizeWithSpacing).toInt().coerceAtLeast(1)
    val totalCells = columns * rows
    if (totalCells <= 0) return emptyList()
    val gridWidth = (diceSize * columns) + (minSpacing * (columns - 1).coerceAtLeast(0))
    val gridHeight = (diceSize * rows) + (minSpacing * (rows - 1).coerceAtLeast(0))
    val horizontalInset = ((availableWidth - gridWidth) / 2f).coerceAtLeast(0.dp)
    val verticalInset = ((availableHeight - gridHeight) / 2f).coerceAtLeast(0.dp)
    val cells = buildList(totalCells) {
        for (row in 0 until rows) {
            for (column in 0 until columns) {
                add(column to row)
            }
        }
    }.shuffled(Random(seed))
    return cells.take(minOf(diceCount, totalCells)).map { (column, row) ->
        val x = horizontalInset + (sizeWithSpacing * column)
        val y = verticalInset + (sizeWithSpacing * row)
        DicePosition(
            x = x.coerceIn(0.dp, availableWidth - diceSize),
            y = y.coerceIn(0.dp, availableHeight - diceSize)
        )
    }
}

internal fun calculatePackedDicePositions(
    diceCount: Int,
    availableWidth: Dp,
    availableHeight: Dp,
    diceSize: Dp,
    spacing: Dp,
    columns: Int,
    rows: Int,
    seed: Long
): List<DicePosition> {
    if (diceCount <= 0) return emptyList()
    val safeColumns = columns.coerceAtLeast(1)
    val safeRows = rows.coerceAtLeast(1)
    val totalCells = safeColumns * safeRows
    val gridWidth = (diceSize * safeColumns) + (spacing * (safeColumns - 1).coerceAtLeast(0))
    val gridHeight = (diceSize * safeRows) + (spacing * (safeRows - 1).coerceAtLeast(0))
    val horizontalInset = ((availableWidth - gridWidth) / 2f).coerceAtLeast(0.dp)
    val verticalInset = ((availableHeight - gridHeight) / 2f).coerceAtLeast(0.dp)
    val allCells = buildList(totalCells) {
        for (row in 0 until safeRows) {
            for (column in 0 until safeColumns) {
                add(column to row)
            }
        }
    }
    val corners = listOf(
        0 to 0,
        (safeColumns - 1) to 0,
        0 to (safeRows - 1),
        (safeColumns - 1) to (safeRows - 1)
    ).filter { (column, row) ->
        column in 0 until safeColumns && row in 0 until safeRows
    }
    val remainingCells = allCells.filterNot { it in corners }
    val shuffledRemaining = remainingCells.shuffled(Random(seed))
    val orderedCells = corners + shuffledRemaining
    return orderedCells.take(minOf(diceCount, totalCells)).map { (column, row) ->
        val baseX = (horizontalInset + (diceSize + spacing) * column)
            .coerceAtMost(availableWidth - diceSize)
        val baseY = (verticalInset + (diceSize + spacing) * row)
            .coerceAtMost(availableHeight - diceSize)
        DicePosition(
            x = baseX.coerceIn(0.dp, availableWidth - diceSize),
            y = baseY.coerceIn(0.dp, availableHeight - diceSize)
        )
    }
}

internal fun calculateDiceTextScale(diceSize: Dp, referenceSize: Dp = 72.dp): Float {
    if (referenceSize.value == 0f) return 1f
    return (diceSize.value / referenceSize.value).coerceAtMost(1f)
}

internal fun diceNumberYOffset(faceDrawable: Int): Dp {
    return when (faceDrawable) {
        R.drawable.eigth_sides,
        R.drawable.eigth_sides_contrast -> 6.dp
        else -> 0.dp
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
        DiceType.D6 -> R.drawable.six_sides_contrast
        DiceType.D8 -> R.drawable.eigth_sides_contrast
        DiceType.D10 -> R.drawable.ten_sides_contrast
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
