package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.SoundEffect
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.presentation.rememberSoundPlayer

internal const val MENU_TOP_APP_BAR_TAG = "menu_top_app_bar"
internal const val MENU_PLAY_BUTTON_TAG = "menu_play_button"
internal const val MENU_RULES_BUTTON_TAG = "menu_rules_button"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    applySystemBarsPadding: Boolean = true,
    showContinueDialog: Boolean,
    isSoundEnabled: Boolean,
    onPlayClick: () -> Unit,
    onRulesClick: () -> Unit,
    onContinueGame: () -> Unit,
    onStartNewGame: () -> Unit,
    onDismissDialog: () -> Unit,
    onSoundToggleClick: () -> Unit
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
                    IconButton(
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
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { soundPlayer.play(SoundEffect.QUESTION) }) {
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
                .padding(top = 96.dp, start = 48.dp, end = 48.dp, bottom = 64.dp)
        ) {
            Text(
                text = stringResource(R.string.game_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onPlayClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(MENU_PLAY_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.play),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                OutlinedButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onRulesClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag(MENU_RULES_BUTTON_TAG)
                ) {
                    Text(
                        text = stringResource(R.string.rules),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
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
                TextButton(
                    onClick = {
                        soundPlayer.play(SoundEffect.USE)
                        onContinueGame()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.menu_continue_confirm),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(
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
fun MenuRoute(
    modifier: Modifier = Modifier,
    viewModel: MenuViewModel = viewModel(),
    onNavigateToDestination: (MenuDestination) -> Unit,
    onRulesClick: () -> Unit
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
        onPlayClick = { viewModel.onEvent(MenuUiEvent.PlayClicked) },
        onRulesClick = onRulesClick,
        onContinueGame = { viewModel.onEvent(MenuUiEvent.ContinueGame) },
        onStartNewGame = { viewModel.onEvent(MenuUiEvent.StartNewGame) },
        onDismissDialog = { viewModel.onEvent(MenuUiEvent.DismissDialog) },
        onSoundToggleClick = { viewModel.onEvent(MenuUiEvent.SoundToggleClicked) }
    )
}
