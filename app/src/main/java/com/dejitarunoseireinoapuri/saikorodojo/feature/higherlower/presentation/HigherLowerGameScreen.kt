package com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice

internal const val HIGHER_LOWER_BUTTON_ROW_TAG = "higher_lower_button_row"
internal const val HIGHER_LOWER_MAT_ROW_TAG = "higher_lower_mat_row"
internal const val HIGHER_LOWER_CONTINUE_BUTTON_TAG = "higher_lower_continue_button"

@Composable
fun HigherLowerGameRoute(
    modifier: Modifier = Modifier,
    viewModel: HigherLowerGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HigherLowerGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(HigherLowerGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(choice))
        },
        onContinueClick = onContinueClick
    )
}

@Composable
fun HigherLowerGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: HigherLowerGameUiState,
    onStartClick: () -> Unit,
    onChoiceSelect: (HigherLowerChoice) -> Unit,
    onContinueClick: () -> Unit
) {
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1F2335),
            Color(0xFF2E2A6B),
            Color(0xFF2B5B8A)
        )
    )
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(backgroundBrush)
    Box(
        modifier = containerModifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.higher_lower_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            val hasReward = uiState.rewardCard != null
            val hasLoss = uiState.hasLoss && uiState.isComplete
            when {
                hasReward -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.odd_even_congrats),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = Color(0xFFFFF176)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.odd_even_reward_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White
                    )
                }
                hasLoss -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.odd_even_try_again),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                        color = Color(0xFFFFF176),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.higher_lower_subtitle),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            if (uiState.isStarted && !uiState.isComplete) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRound,
                        uiState.totalRounds
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (uiState.isChoiceVisible) {
                    Row(
                        modifier = Modifier.testTag(HIGHER_LOWER_BUTTON_ROW_TAG),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HigherLowerChoiceButton(
                            label = stringResource(R.string.higher_lower_higher),
                            isSelected = uiState.selectedChoice == HigherLowerChoice.HIGHER,
                            onClick = { onChoiceSelect(HigherLowerChoice.HIGHER) }
                        )
                        HigherLowerChoiceButton(
                            label = stringResource(R.string.higher_lower_lower),
                            isSelected = uiState.selectedChoice == HigherLowerChoice.LOWER,
                            onClick = { onChoiceSelect(HigherLowerChoice.LOWER) }
                        )
                    }
                }
            }
        }

        if (!uiState.isStarted) {
            Button(
                onClick = onStartClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1744),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(64.dp)
            ) {
                Text(
                    text = stringResource(R.string.higher_lower_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        val showMats = uiState.isStarted && uiState.rewardCard == null
        if (showMats) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(72.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag(HIGHER_LOWER_MAT_ROW_TAG),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HigherLowerMat(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        backgroundColor = Color(0xFFFFF8E1),
                        borderColor = Color(0xFFFFCC80)
                    ) {
                        HigherLowerDiceRow(
                            values = uiState.baseDiceValues,
                            diceRes = R.drawable.ten_sides
                        )
                    }
                    HigherLowerMat(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        backgroundColor = Color(0xFFE8F5E9),
                        borderColor = Color(0xFFA5D6A7)
                    ) {
                        HigherLowerDiceRow(
                            values = uiState.currentDiceValues,
                            diceRes = R.drawable.ten_sides_green
                        )
                    }
                }
            }
        }

        uiState.rewardCard?.let { reward ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                CardItem(
                    card = reward,
                    onApplyClick = {},
                    showActionButton = false,
                    showCount = false
                )
            }
        }

        if (uiState.isComplete && uiState.isStarted) {
            Button(
                onClick = onContinueClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF26C6DA),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .height(56.dp)
                    .zIndex(4f)
                    .testTag(HIGHER_LOWER_CONTINUE_BUTTON_TAG)
            ) {
                Text(
                    text = stringResource(R.string.odd_even_continue),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun HigherLowerChoiceButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSelected,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF1744),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFFF1744),
            disabledContentColor = Color.White
        ),
        modifier = Modifier.height(56.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
    }
}

@Composable
private fun HigherLowerMat(
    modifier: Modifier,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun HigherLowerDiceRow(
    values: List<Int>,
    diceRes: Int
) {
    if (values.isEmpty()) return
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val spacing = 12.dp
        val horizontalPadding = 12.dp
        val availableWidth = maxWidth - horizontalPadding * 2 - spacing
        val diceSize = (availableWidth / 2f).coerceAtMost(96.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            values.take(2).forEach { value ->
                HigherLowerDieFace(
                    value = value,
                    size = diceSize,
                    diceRes = diceRes
                )
            }
        }
    }
}

@Composable
private fun HigherLowerDieFace(
    value: Int,
    size: Dp,
    diceRes: Int
) {
    val fontSize = (size.value * 0.32f).coerceIn(14f, 22f).sp
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = diceRes),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            color = Color.White
        )
    }
}
