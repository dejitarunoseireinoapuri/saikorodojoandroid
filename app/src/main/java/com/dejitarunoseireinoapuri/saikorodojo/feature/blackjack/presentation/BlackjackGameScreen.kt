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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.RewardCardStack
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameMessageType
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.MinigameButtonPrimaryColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameButtonColors
import com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation.minigameMessageColor
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.FailureMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SequenceSaveMatBorder
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.VictoryMatBackground

internal const val BLACKJACK_HIT_BUTTON_TAG = "blackjack_hit_button"
internal const val BLACKJACK_STAND_BUTTON_TAG = "blackjack_stand_button"
internal const val BLACKJACK_START_BUTTON_TAG = "blackjack_start_button"
internal const val BLACKJACK_DEALER_MAT_TAG = "blackjack_dealer_mat"
internal const val BLACKJACK_PLAYER_MAT_TAG = "blackjack_player_mat"
internal const val BLACKJACK_REWARD_STACK_TAG = "blackjack_reward_stack"

@Composable
fun BlackjackGameRoute(
    modifier: Modifier = Modifier,
    viewModel: BlackjackGameViewModel = viewModel(),
    onContinueClick: () -> Unit,
    onNavigateToMenu: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.saveSession()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BlackjackGameScreen(
        modifier = modifier,
        uiState = uiState,
        onStartClick = { viewModel.onEvent(BlackjackGameUiEvent.StartGame) },
        onHitClick = { viewModel.onEvent(BlackjackGameUiEvent.Hit) },
        onStandClick = { viewModel.onEvent(BlackjackGameUiEvent.Stand) },
        onContinueClick = onContinueClick,
        onExitToMenu = {
            viewModel.saveSession()
            onNavigateToMenu()
        }
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
    onContinueClick: () -> Unit,
    onExitToMenu: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var hadRewardCards by remember { mutableStateOf(false) }
    var previousPlayerDiceCount by remember { mutableStateOf(0) }
    var previousDealerDiceCount by remember { mutableStateOf(0) }
    var previousResult by remember { mutableStateOf<BlackjackOutcome?>(null) }
    LaunchedEffect(uiState.playerDice.size, uiState.dealerDice.size, uiState.isRolling) {
        if (shouldPlayDiceRollSound(
                previousPlayerCount = previousPlayerDiceCount,
                currentPlayerCount = uiState.playerDice.size,
                previousDealerCount = previousDealerDiceCount,
                currentDealerCount = uiState.dealerDice.size,
                isRolling = uiState.isRolling
            )
        ) {
            soundPlayer.play(SoundEffect.DICE_ROLL)
        }
        previousPlayerDiceCount = uiState.playerDice.size
        previousDealerDiceCount = uiState.dealerDice.size
    }
    LaunchedEffect(uiState.result, uiState.isStarted) {
        if (shouldPlayOutcomeSound(
                previousOutcome = previousResult,
                currentOutcome = uiState.result,
                isStarted = uiState.isStarted
            )
        ) {
            val effect = when (uiState.result) {
                BlackjackOutcome.PLAYER_WIN -> SoundEffect.SUCCESS
                BlackjackOutcome.PLAYER_LOSE -> SoundEffect.LOSS
                null -> null
            }
            if (effect != null) {
                soundPlayer.play(effect)
            }
        }
        previousResult = uiState.result
    }
    LaunchedEffect(uiState.rewardCards) {
        val hasRewards = uiState.rewardCards.isNotEmpty()
        if (hasRewards && !hadRewardCards) {
            soundPlayer.play(SoundEffect.CARD_DRAW)
        }
        hadRewardCards = hasRewards
    }
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }
    var containerModifier = modifier.fillMaxSize()
    if (applySystemBarsPadding) {
        containerModifier = containerModifier.systemBarsPadding()
    }
    containerModifier = containerModifier
        .padding(contentPadding)
        .background(MaterialTheme.colorScheme.background)
    Box(modifier = containerModifier) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 20.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = {
                    soundPlayer.play(SoundEffect.QUESTION)
                    showExitDialog = true
                },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.cd_exit_home),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = stringResource(R.string.rules_minigame_blackjack_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 24.dp)
                .then(
                    if (!uiState.isStarted) {
                        Modifier.verticalScroll(rememberScrollState())
                    } else {
                        Modifier
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            val titleColor = MaterialTheme.colorScheme.onBackground
            val showEndState = uiState.isComplete
            val resultTextRes = when (uiState.result) {
                BlackjackOutcome.PLAYER_WIN -> R.string.minigame_win_message
                BlackjackOutcome.PLAYER_LOSE -> R.string.minigame_lose_message
                null -> null
            }
            val resultTextColor = blackjackResultTextColor(
                result = uiState.result,
                defaultColor = titleColor
            )
            val hasReward = uiState.rewardCards.isNotEmpty()
            val showRules = !uiState.isStarted
            val rulesModifier = if (showRules) {
                Modifier
            } else {
                Modifier.alpha(0f).clearAndSetSemantics { }
            }
            when {
                hasReward -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_message),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.Win,
                            titleColor = titleColor
                        ),
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
                        color = MaterialTheme.colorScheme.onBackground,
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
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.minigame_win_cards_message),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                        color = minigameMessageColor(
                            MinigameMessageType.WinCards,
                            titleColor = titleColor
                        )
                    )
                }
                resultTextRes != null && showEndState -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(resultTextRes),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 22.sp),
                        color = resultTextColor,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    Text(
                        text = stringResource(R.string.rules_minigame_blackjack_body),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = titleColor,
                        textAlign = TextAlign.Start,
                        modifier = rulesModifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                    )
                }
            }
            if (!uiState.isStarted) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        onStartClick()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = minigameButtonColors(),
                    modifier = Modifier
                        .height(64.dp)
                        .testTag(BLACKJACK_START_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.blackjack_start),
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (uiState.isStarted && uiState.rewardCards.isEmpty() && (!uiState.isComplete || uiState.result == BlackjackOutcome.PLAYER_LOSE)) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(120.dp))
                ScoreLabel(
                    text = stringResource(R.string.blackjack_dealer_score, uiState.dealerTotal)
                )
                Spacer(modifier = Modifier.height(8.dp))
                BlackjackMat(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag(BLACKJACK_DEALER_MAT_TAG),
                    contentAlignment = Alignment.Center,
                    backgroundColor = SequenceSaveMatBackground,
                    borderColor = SequenceSaveMatBorder
                ) {
                    DiceRow(
                        values = uiState.dealerDice
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                if (uiState.isAwaitingDecision) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BlackjackActionButton(
                            label = stringResource(R.string.blackjack_stand),
                            testTag = BLACKJACK_STAND_BUTTON_TAG,
                            onClick = {
                                onStandClick()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BlackjackActionButton(
                            label = stringResource(R.string.blackjack_hit),
                            testTag = BLACKJACK_HIT_BUTTON_TAG,
                            onClick = {
                                onHitClick()
                            },
                            modifier = Modifier.weight(1f)
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
                        .height(140.dp)
                        .testTag(BLACKJACK_PLAYER_MAT_TAG),
                    contentAlignment = Alignment.Center,
                    backgroundColor = when (uiState.result) {
                        BlackjackOutcome.PLAYER_WIN -> VictoryMatBackground
                        BlackjackOutcome.PLAYER_LOSE -> FailureMatBackground
                        null -> SequenceSaveMatBackground
                    },
                    borderColor = when (uiState.result) {
                        BlackjackOutcome.PLAYER_WIN -> VictoryMatBackground
                        BlackjackOutcome.PLAYER_LOSE -> FailureMatBackground
                        null -> SequenceSaveMatBorder
                    }
                ) {
                    DiceRow(
                        values = uiState.playerDice
                    )
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        if (uiState.rewardCards.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 96.dp)
                    .testTag(BLACKJACK_REWARD_STACK_TAG)
                    .zIndex(3f),
                contentAlignment = Alignment.Center
            ) {
                RewardCardStack(cards = uiState.rewardCards)
            }
        }

        if (uiState.isComplete && uiState.isStarted) {
            Button(
                onClick = {
                    soundPlayer.play(SoundEffect.USE)
                    onContinueClick()
                },
                shape = RoundedCornerShape(20.dp),
                colors = minigameButtonColors(),
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
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                containerColor = MaterialTheme.colorScheme.background,
                title = {
                    Text(
                        text = stringResource(R.string.exit_title),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.exit_message),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            showExitDialog = false
                            onExitToMenu()
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.exit_confirm),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.USE)
                            showExitDialog = false
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_cancel),
                            style = MaterialTheme.typography.titleMedium,
                            color = MinigameButtonPrimaryColor
                        )
                    }
                }
            )
        }
    }
}

internal fun blackjackResultTextColor(
    result: BlackjackOutcome?,
    defaultColor: Color
): Color {
    return when (result) {
        BlackjackOutcome.PLAYER_LOSE -> minigameMessageColor(
            MinigameMessageType.Lose,
            titleColor = defaultColor
        )
        BlackjackOutcome.PLAYER_WIN -> minigameMessageColor(
            MinigameMessageType.Win,
            titleColor = defaultColor
        )
        null -> defaultColor
    }
}

@Composable
private fun ScoreLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun BlackjackActionButton(
    label: String,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = minigameButtonColors(),
        modifier = modifier
            .height(56.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BlackjackMat(
    modifier: Modifier,
    contentAlignment: Alignment,
    backgroundColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(24.dp))
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(8.dp),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

@Composable
private fun DiceRow(
    values: List<Int>
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
                            size = diceSize.coerceAtMost(96.dp)
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
    size: Dp
) {
    val fontSize = (size.value * 0.32f).coerceIn(14f, 22f).sp
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ten_sides),
            contentDescription = stringResource(R.string.cd_dice_face, value),
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
