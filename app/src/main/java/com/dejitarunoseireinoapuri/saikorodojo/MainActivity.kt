package com.dejitarunoseireinoapuri.saikorodojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dejitarunoseireinoapuri.saikorodojo.feature.ads.data.AdConsentManager
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation.BlackjackGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation.GameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation.HigherLowerGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation.MenuDestination
import com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation.MenuRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation.OddEvenGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.rules.presentation.RulesRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation.SequenceGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.settings.presentation.SettingsRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data.SoundPlayerProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data.SoundSettingsRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.navigation.AppRoutes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import com.dejitarunoseireinoapuri.saikorodojo.ui.SystemBarAppearance
import com.dejitarunoseireinoapuri.saikorodojo.ui.resolveSystemBarAppearance
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.AppBackground
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.DiceSetValueOuterColor
import com.dejitarunoseireinoapuri.saikorodojo.ui.components.MetallicTextButton
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.ads.MobileAds
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.data.HapticSettingsRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain.GetHapticsEnabledUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain.ObserveHapticsEnabledUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.domain.ToggleHapticsEnabledUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.haptics.presentation.LocalHapticsEnabled

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        GameSessionRepositoryProvider.initialize(this)
        SoundSettingsRepositoryProvider.initialize(this)
        HapticSettingsRepositoryProvider.initialize(this)
        SoundPlayerProvider.initialize(this)
        val hapticSettingsRepository = HapticSettingsRepositoryProvider.provide()
        val getHapticsEnabled = GetHapticsEnabledUseCase(hapticSettingsRepository)
        val observeHapticsEnabled = ObserveHapticsEnabledUseCase(hapticSettingsRepository)
        val toggleHapticsEnabled = ToggleHapticsEnabledUseCase(hapticSettingsRepository)
        val systemBarAppearance = resolveSystemBarAppearance(AppBackground.toArgb())
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle(systemBarAppearance),
            navigationBarStyle = systemBarStyle(systemBarAppearance)
        )
        AdConsentManager.initialize(this)
        AdConsentManager.requestConsentInfoUpdate(this)
        MobileAds.initialize(this)
        val activity = this
        var pendingPlayAction: (() -> Unit)? = null

        setContent {
            val navController = rememberNavController()
            val isHapticsEnabled by observeHapticsEnabled.execute().collectAsStateWithLifecycle(
                initialValue = getHapticsEnabled.execute()
            )
            SaikoroDojoTheme {
                CompositionLocalProvider(LocalHapticsEnabled provides isHapticsEnabled) {
                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.START_DESTINATION
                    ) {
                    composable(AppRoutes.MENU) {
                        var showInitialAdsNoticeDialog by rememberSaveable { mutableStateOf(false) }

                        if (showInitialAdsNoticeDialog) {
                            AlertDialog(
                                onDismissRequest = {},
                                containerColor = MaterialTheme.colorScheme.background,
                                text = {
                                    Text(
                                        text = stringResource(R.string.ads_notice_message),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                confirmButton = {
                                    MetallicTextButton(
                                        onClick = {
                                            showInitialAdsNoticeDialog = false
                                            AdConsentManager.markInitialAdsNoticeShown(activity)
                                            AdConsentManager.showConsentFormIfRequired(activity) {
                                                pendingPlayAction?.invoke()
                                                pendingPlayAction = null
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(R.string.ads_notice_accept),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DiceSetValueOuterColor
                                        )
                                    }
                                }
                            )
                        }

                        MenuRoute(
                            isHapticsEnabled = isHapticsEnabled,
                            onHapticsToggleClick = { toggleHapticsEnabled.execute() },
                            onNavigateToDestination = { destination ->
                                when (destination) {
                                    is MenuDestination.MainGame -> {
                                        if (destination.resetSession) {
                                            navController.popBackStack(
                                                route = AppRoutes.GAME,
                                                inclusive = true
                                            )
                                            navController.navigate(AppRoutes.PLAY_DESTINATION)
                                        } else {
                                            val returned = navController.popBackStack(
                                                route = AppRoutes.GAME,
                                                inclusive = false
                                            )
                                            if (!returned) {
                                                navController.navigate(AppRoutes.PLAY_DESTINATION)
                                            }
                                        }
                                    }

                                    is MenuDestination.Minigame -> {
                                        val returned = navController.popBackStack(
                                            route = AppRoutes.GAME,
                                            inclusive = false
                                        )
                                        if (!returned) {
                                            navController.navigate(AppRoutes.PLAY_DESTINATION)
                                        }
                                        navController.navigate(minigameRoute(destination.minigameType))
                                    }

                                    MenuDestination.Settings -> {
                                        navController.navigate(AppRoutes.SETTINGS)
                                    }
                                }
                            },
                            onRulesClick = {
                                navController.navigate(AppRoutes.RULES)
                            },
                            onPlayClick = { proceed ->
                                if (AdConsentManager.shouldShowConsentFormBeforePlay(activity)) {
                                    if (!AdConsentManager.hasShownInitialAdsNotice(activity)) {
                                        pendingPlayAction = proceed
                                        showInitialAdsNoticeDialog = true
                                    } else {
                                        AdConsentManager.showConsentFormIfRequired(activity) {
                                            proceed()
                                        }
                                    }
                                } else {
                                    proceed()
                                }
                            }
                        )
                    }
                    composable(AppRoutes.SETTINGS) {
                        SettingsRoute(
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onManageAdsClick = {
                                AdConsentManager.showPrivacyOptionsForm(activity) {}
                            }
                        )
                    }
                    composable(AppRoutes.RULES) {
                        RulesRoute(
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(AppRoutes.GAME) {
                        GameRoute(
                            onNavigateToMinigame = { minigame ->
                                navController.navigate(minigameRoute(minigame))
                            },
                            onNavigateToMenu = { reset ->
                                if (reset) {
                                    navController.navigate(AppRoutes.MENU) {
                                        popUpTo(AppRoutes.MENU) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(AppRoutes.MENU) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                    composable(AppRoutes.ODD_EVEN_GAME) {
                        OddEvenGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.GAME, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.MENU) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.SEQUENCE_GAME) {
                        SequenceGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.GAME, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.MENU) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.BLACKJACK_GAME) {
                        BlackjackGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.GAME, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.MENU) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.HIGHER_LOWER_GAME) {
                        HigherLowerGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.GAME, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.MENU) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    }
                }
            }
        }
    }
}

private fun minigameRoute(minigame: MinigameType): String {
    return when (minigame) {
        MinigameType.ODD_EVEN -> AppRoutes.ODD_EVEN_GAME
        MinigameType.SEQUENCE -> AppRoutes.SEQUENCE_GAME
        MinigameType.BLACKJACK -> AppRoutes.BLACKJACK_GAME
        MinigameType.HIGHER_LOWER -> AppRoutes.HIGHER_LOWER_GAME
    }
}

private fun systemBarStyle(appearance: SystemBarAppearance): SystemBarStyle {
    return if (appearance.useDarkIcons) {
        SystemBarStyle.light(
            scrim = appearance.backgroundColor,
            darkScrim = appearance.backgroundColor
        )
    } else {
        SystemBarStyle.dark(scrim = appearance.backgroundColor)
    }
}
