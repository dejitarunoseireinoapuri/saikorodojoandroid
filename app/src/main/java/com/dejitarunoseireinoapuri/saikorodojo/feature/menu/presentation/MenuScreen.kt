package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer
import com.dejitarunoseireinoapuri.saikorodojo.ui.components.MetallicButton
import com.dejitarunoseireinoapuri.saikorodojo.ui.components.MetallicIconButton
import com.dejitarunoseireinoapuri.saikorodojo.ui.components.MetallicTextButton
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceSetValueOuterColor

internal const val MENU_TOP_APP_BAR_TAG = "menu_top_app_bar"
internal const val MENU_PLAY_BUTTON_TAG = "menu_play_button"
internal const val MENU_RULES_BUTTON_TAG = "menu_rules_button"
internal const val MENU_DIE_IMAGE_TAG = "menu_die_image"
internal const val MENU_HAPTICS_BUTTON_TAG = "menu_haptics_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    showContinueDialog: Boolean,
    isSoundEnabled: Boolean,
    isHapticsEnabled: Boolean,
    onPlayClick: () -> Unit,
    onRulesClick: () -> Unit,
    onContinueGame: () -> Unit,
    onStartNewGame: () -> Unit,
    onDismissDialog: () -> Unit,
    onSoundToggleClick: () -> Unit,
    onHapticsToggleClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val soundPlayer = rememberSoundPlayer()
    var scaffoldModifier = modifier
    if (applySystemBarsPadding) {
        scaffoldModifier = scaffoldModifier.systemBarsPadding()
    }
    scaffoldModifier = scaffoldModifier.padding(contentPadding)
    scaffoldModifier = scaffoldModifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)

    Scaffold(
        modifier = scaffoldModifier,
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                modifier = Modifier.testTag(MENU_TOP_APP_BAR_TAG),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { },
                actions = {
                    MetallicIconButton(
                        onClick = {
                            val shouldPlayActivationSound = !isSoundEnabled
                            onSoundToggleClick()
                            if (shouldPlayActivationSound) {
                                soundPlayer.play(SoundEffect.USE)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (!isSoundEnabled) {
                                Icons.AutoMirrored.Filled.VolumeOff
                            } else {
                                Icons.AutoMirrored.Filled.VolumeUp
                            },
                            contentDescription = if (!isSoundEnabled) {
                                stringResource(R.string.cd_sound_off)
                            } else {
                                stringResource(R.string.cd_sound_on)
                            },
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    MetallicIconButton(
                        onClick = onHapticsToggleClick,
                        modifier = Modifier.testTag(MENU_HAPTICS_BUTTON_TAG)
                    ) {
                        HapticsStatusIcon(isEnabled = isHapticsEnabled)
                    }
                    MetallicIconButton(
                        onClick = {
                            soundPlayer.play(SoundEffect.QUESTION)
                            onSettingsClick()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_settings),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 40.dp, start = 48.dp, end = 48.dp, bottom = 64.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground)) {
                        append("DICE ")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append("DECK")
                    }
                },
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.6).sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.75f),
                        offset = Offset(0f, 4f),
                        blurRadius = 4f
                    )
                ),
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Image(
                painter = painterResource(R.drawable.saikoro_dojo_die),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(168.dp)
                    .testTag(MENU_DIE_IMAGE_TAG)
            )

            Column(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetallicButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onPlayClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(MENU_PLAY_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.play),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                MetallicButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onRulesClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(MENU_RULES_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.rules),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }

    if (showContinueDialog) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            containerColor = MaterialTheme.colorScheme.background,
            title = {
                Text(
                    text = stringResource(R.string.menu_continue_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.menu_continue_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            confirmButton = {
                MetallicTextButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onContinueGame()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.menu_continue_confirm),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = DiceSetValueOuterColor
                    )
                }
            },
            dismissButton = {
                MetallicTextButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onStartNewGame()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.menu_continue_new_game),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )
    }
}

@Composable
private fun HapticsStatusIcon(isEnabled: Boolean) {
    val gold = MaterialTheme.colorScheme.primary
    Box(modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Default.Vibration,
            contentDescription = if (isEnabled) {
                stringResource(R.string.cd_haptics_on)
            } else {
                stringResource(R.string.cd_haptics_off)
            },
            tint = if (isEnabled) gold else gold.copy(alpha = 0.45f),
            modifier = Modifier.matchParentSize()
        )
        if (!isEnabled) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawLine(
                    color = gold,
                    start = Offset(size.width * 0.18f, size.height * 0.18f),
                    end = Offset(size.width * 0.82f, size.height * 0.82f),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun MenuRoute(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(),
    isHapticsEnabled: Boolean,
    onHapticsToggleClick: () -> Unit,
    onNavigateToDestination: (MenuDestination) -> Unit,
    onRulesClick: () -> Unit,
    onPlayClick: (((() -> Unit) -> Unit))? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshSavedSession()
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MenuUiEffect.NavigateTo -> onNavigateToDestination(effect.destination)
            }
        }
    }

    MenuScreen(
        modifier = modifier,
        showContinueDialog = uiState.showContinueDialog,
        isSoundEnabled = uiState.isSoundEnabled,
        isHapticsEnabled = isHapticsEnabled,
        onPlayClick = {
            val proceed = { viewModel.onEvent(MenuUiEvent.PlayClicked) }
            if (onPlayClick == null) {
                proceed()
            } else {
                onPlayClick(proceed)
            }
        },
        onRulesClick = onRulesClick,
        onContinueGame = { viewModel.onEvent(MenuUiEvent.ContinueGame) },
        onStartNewGame = { viewModel.onEvent(MenuUiEvent.StartNewGame) },
        onDismissDialog = { viewModel.onEvent(MenuUiEvent.DismissDialog) },
        onSoundToggleClick = { viewModel.onEvent(MenuUiEvent.SoundToggleClicked) },
        onHapticsToggleClick = onHapticsToggleClick,
        onSettingsClick = { viewModel.onEvent(MenuUiEvent.SettingsClicked) }
    )
}
