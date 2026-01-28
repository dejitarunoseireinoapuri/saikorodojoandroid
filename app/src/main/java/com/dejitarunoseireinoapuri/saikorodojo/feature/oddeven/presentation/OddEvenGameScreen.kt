package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoThemeColors

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
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.odd_even_subtitle),
                style = MaterialTheme.typography.bodyLarge,
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.odd_even_hits_status,
                        uiState.correctCount,
                        uiState.targetCorrect
                    ),
                    style = MaterialTheme.typography.bodyLarge,
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
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = stringResource(R.string.odd_even_start),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.diceValue != null,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val rotation = remember { Animatable(0f) }
            val scale = remember { Animatable(0.85f) }
            val isRolling by rememberUpdatedState(uiState.isRolling)
            LaunchedEffect(uiState.diceValue, uiState.isRolling) {
                if (isRolling) {
                    rotation.snapTo(0f)
                    scale.snapTo(0.85f)
                    rotation.animateTo(
                        targetValue = 360f,
                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                    )
                }
            }
            uiState.diceValue?.let { value ->
                OddEvenDiceFace(
                    value = value,
                    size = 140.dp,
                    modifier = Modifier
                        .rotate(rotation.value)
                        .scale(scale.value)
                )
            }
        }

        if (uiState.showFireworks) {
            FireworksEffect(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            )
        }

        if (uiState.showFailure) {
            FailureEffect(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f)
            )
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.height(56.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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

@Composable
private fun FireworksEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_500, easing = FastOutSlowInEasing)
        )
    }
    val bursts = remember {
        listOf(
            FireworkBurst(0.2f, 0.25f, Color(0xFFFFC107)),
            FireworkBurst(0.75f, 0.3f, Color(0xFFFF4081)),
            FireworkBurst(0.35f, 0.6f, Color(0xFF4CAF50)),
            FireworkBurst(0.7f, 0.7f, Color(0xFF40C4FF))
        )
    }
    BoxWithConstraints(modifier = modifier) {
        bursts.forEach { burst ->
            val size = lerpDp(24.dp, 72.dp, progress.value)
            val alpha = (1f - progress.value).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .offset(
                        x = maxWidth * burst.xFraction - size / 2,
                        y = maxHeight * burst.yFraction - size / 2
                    )
                    .size(size)
                    .clip(CircleShape)
                    .background(burst.color.copy(alpha = alpha))
            )
        }
    }
}

@Composable
private fun FailureEffect(
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_500, easing = FastOutSlowInEasing)
        )
    }
    val alpha = (1f - progress.value).coerceIn(0f, 1f)
    val scale = 0.9f + (0.3f * (1f - progress.value))
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.odd_even_fail),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        )
    }
}

private data class FireworkBurst(
    val xFraction: Float,
    val yFraction: Float,
    val color: Color
)

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}
