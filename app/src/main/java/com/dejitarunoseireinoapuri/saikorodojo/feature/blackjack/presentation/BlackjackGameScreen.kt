package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

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
import androidx.compose.ui.graphics.ColorFilter
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
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardItem
import com.dejitarunoseireinoapuri.saikorodojo.ui.ads.BannerAd
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder

internal const val BLACKJACK_HIT_BUTTON_TAG = "blackjack_hit_button"
internal const val BLACKJACK_STAND_BUTTON_TAG = "blackjack_stand_button"
internal const val BLACKJACK_START_BUTTON_TAG = "blackjack_start_button"

@Composable
fun BlackjackGameRoute(
    modifier: Modifier = Modifier,
    viewModel: BlackjackGameViewModel = viewModel(),
    onContinueClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BlackjackGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(BlackjackGameUiEvent.StartGame) },
        onHitClick = { viewModel.onEvent(BlackjackGameUiEvent.Hit) },
        onStandClick = { viewModel.onEvent(BlackjackGameUiEvent.Stand) },
        onContinueClick = onContinueClick
    )
}

@Composable
fun BlackjackGameScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    uiState: BlackjackGameUiState,
    onStartClick: () -> Unit,
    onHitClick: () -> Unit,
    onStandClick: () -> Unit,
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
    Column(modifier = containerModifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.blackjack_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                val resultTextRes = when (uiState.result) {
                    BlackjackOutcome.PLAYER_WIN -> R.string.blackjack_win
                    BlackjackOutcome.PLAYER_LOSE -> R.string.blackjack_lose
                    null -> null
                }
                val hasReward = uiState.rewardCard != null
                when {
                    hasReward -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.blackjack_win),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                            color = Color(0xFFFFF176),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.blackjack_dealer_score,
                                uiState.dealerTotal
                            ),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.blackjack_player_score,
                                uiState.playerTotal
                            ),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.blackjack_reward_subtitle),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            color = Color.White
                        )
                    }
                    resultTextRes != null -> {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(resultTextRes),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                            color = Color(0xFFFFF176),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = stringResource(R.string.blackjack_subtitle),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
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
                        .testTag(BLACKJACK_START_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.blackjack_start),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp)
                    )
                }
            }

            if (uiState.isStarted && uiState.rewardCard == null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                Spacer(modifier = Modifier.height(64.dp))
                    ScoreLabel(
                        text = stringResource(R.string.blackjack_dealer_score, uiState.dealerTotal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlackjackMat(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DiceRow(
                            values = uiState.dealerDice,
                            isBust = uiState.showDealerBust
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    if (uiState.isAwaitingDecision) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BlackjackActionButton(
                                label = stringResource(R.string.blackjack_hit),
                                testTag = BLACKJACK_HIT_BUTTON_TAG,
                                onClick = onHitClick
                            )
                            BlackjackActionButton(
                                label = stringResource(R.string.blackjack_stand),
                                testTag = BLACKJACK_STAND_BUTTON_TAG,
                                onClick = onStandClick
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(56.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ScoreLabel(
                        text = stringResource(R.string.blackjack_player_score, uiState.playerTotal)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BlackjackMat(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DiceRow(
                            values = uiState.playerDice,
                            isBust = uiState.showPlayerBust
                        )
                    }
                Spacer(modifier = Modifier.height(24.dp))
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
                ) {
                    Text(
                        text = stringResource(R.string.blackjack_continue),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    )
                }
            }
        }
        BannerAd(modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ScoreLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BlackjackActionButton(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFF1744),
            contentColor = Color.White
        ),
        modifier = Modifier
            .height(56.dp)
            .testTag(testTag)
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
private fun BlackjackMat(
    modifier: Modifier,
    contentAlignment: Alignment,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(SequenceSaveMatBackground, RoundedCornerShape(24.dp))
            .border(2.dp, SequenceSaveMatBorder, RoundedCornerShape(24.dp))
            .padding(8.dp),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
private fun DiceRow(
    values: List<Int>,
    isBust: Boolean
) {
    if (values.isEmpty()) return
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth()
    ) {
        val maxPerRow = 5
        val rows = values.chunked(maxPerRow)
        val rowCount = rows.size.coerceAtLeast(1)
        val spacing = 10.dp
        val rowSpacing = 8.dp
        val horizontalPadding = 12.dp
        val verticalPadding = 8.dp
        val availableWidth = maxWidth - horizontalPadding * 2
        val availableHeight = maxHeight - verticalPadding * 2 - rowSpacing * (rowCount - 1)
        val columns = rows.maxOfOrNull { it.size }?.coerceAtLeast(1) ?: 1
        val widthBasedSize =
            (availableWidth - spacing * (columns - 1)).coerceAtLeast(0.dp) / columns
        val heightBasedSize =
            (availableHeight / rowCount.coerceAtLeast(1)).coerceAtLeast(0.dp)
        val diceSize = minOf(widthBasedSize, heightBasedSize)
            .coerceAtMost(96.dp)
            .coerceAtLeast(32.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            rows.forEach { rowValues ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowValues.forEach { value ->
                        BlackjackDieFace(
                            value = value,
                            size = diceSize.coerceAtMost(96.dp),
                            isBust = isBust
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BlackjackDieFace(
    value: Int,
    size: Dp,
    isBust: Boolean
) {
    val tint = if (isBust) Color(0xFFE53935) else Color.Unspecified
    val fontSize = (size.value * 0.32f).coerceIn(14f, 22f).sp
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ten_sides),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            colorFilter = if (isBust) ColorFilter.tint(tint) else null,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            color = Color.White
        )
    }
}
