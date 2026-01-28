package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice

@Composable
fun OddEvenGameRoute(
    modifier: Modifier = Modifier,
    viewModel: OddEvenGameViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OddEvenGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(OddEvenGameUiEvent.StartGame) },
        onChoiceSelect = { choice ->
            viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(choice))
        }
    )
}

@Composable
fun OddEvenGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: OddEvenGameUiState,
    onStartClick: () -> Unit,
    onChoiceSelect: (OddEvenChoice) -> Unit
) {
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB00020),
            Color(0xFF4A148C),
            Color(0xFF0D47A1)
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
                text = stringResource(R.string.odd_even_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.odd_even_subtitle),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            if (uiState.isStarted) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_round_status,
                        uiState.currentRound,
                        uiState.totalRounds
                    ),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_hits_status,
                        uiState.correctCount,
                        uiState.targetCorrect
                    ),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (!uiState.isComplete) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.ODD,
                            label = stringResource(R.string.odd_even_even),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = { onChoiceSelect(OddEvenChoice.EVEN) }
                        )
                        OddEvenChoiceButton(
                            visible = uiState.selectedChoice != OddEvenChoice.EVEN,
                            label = stringResource(R.string.odd_even_odd),
                            isEnabled = uiState.selectedChoice == null,
                            onClick = { onChoiceSelect(OddEvenChoice.ODD) }
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
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.odd_even_start),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.diceValue != null,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.diceValue?.let { value ->
                    OddEvenDiceFace(
                        value = value,
                        size = 140.dp
                    )
                }
                val resultTextRes = when {
                    uiState.showFireworks -> R.string.odd_even_correct
                    uiState.showFailure -> R.string.odd_even_wrong
                    else -> null
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (resultTextRes != null) {
                        Text(
                            text = stringResource(resultTextRes),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else {
                        Text(
                            text = "",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            modifier = Modifier.alpha(0f)
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
    }
}

@Composable
private fun OddEvenChoiceButton(
    visible: Boolean,
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    AnimatedVisibility(visible = visible) {
        Button(
            onClick = onClick,
            enabled = isEnabled,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF1744),
                contentColor = Color.White
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
}

@Composable
private fun OddEvenDiceFace(
    value: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.six_sides),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            modifier = Modifier.offset(y = 0.dp)
        )
    }
}
