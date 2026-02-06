package com.dejitarunoseireinoapuri.saikorodojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation.BlackjackGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation.GameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation.HigherLowerGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation.MenuRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation.MenuDestination
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation.OddEvenGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation.SequenceGameRoute
import com.dejitarunoseireinoapuri.saikorodojo.navigation.AppRoutes
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            SaikoroDojoTheme {
                NavHost(
                    navController = navController,
                    startDestination = AppRoutes.StartDestination
                ) {
                    composable(AppRoutes.Menu) {
                        MenuRoute(
                            onNavigateToDestination = { destination ->
                                when (destination) {
                                    is MenuDestination.MainGame -> {
                                        if (destination.resetSession) {
                                            navController.popBackStack(
                                                route = AppRoutes.Game,
                                                inclusive = true
                                            )
                                            navController.navigate(AppRoutes.PlayDestination)
                                        } else {
                                            val returned = navController.popBackStack(
                                                route = AppRoutes.Game,
                                                inclusive = false
                                            )
                                            if (!returned) {
                                                navController.navigate(AppRoutes.PlayDestination)
                                            }
                                        }
                                    }
                                    is MenuDestination.Minigame -> {
                                        val returned = navController.popBackStack(
                                            route = AppRoutes.Game,
                                            inclusive = false
                                        )
                                        if (!returned) {
                                            navController.navigate(AppRoutes.PlayDestination)
                                        }
                                        navController.navigate(minigameRoute(destination.minigameType))
                                    }
                                }
                            },
                            onRulesClick = {}
                        )
                    }
                    composable(AppRoutes.Game) {
                        GameRoute(
                            onNavigateToMinigame = { minigame ->
                                navController.navigate(minigameRoute(minigame))
                            },
                            onNavigateToMenu = { reset ->
                                if (reset) {
                                    navController.navigate(AppRoutes.Menu) {
                                        popUpTo(AppRoutes.Menu) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    navController.navigate(AppRoutes.Menu) {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                    composable(AppRoutes.OddEvenGame) {
                        OddEvenGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.Game, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.Menu) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.SequenceGame) {
                        SequenceGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.Game, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.Menu) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.BlackjackGame) {
                        BlackjackGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.Game, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.Menu) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(AppRoutes.HigherLowerGame) {
                        HigherLowerGameRoute(
                            onContinueClick = {
                                navController.popBackStack(AppRoutes.Game, inclusive = false)
                            },
                            onNavigateToMenu = {
                                navController.navigate(AppRoutes.Menu) {
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

private fun minigameRoute(minigame: MinigameType): String {
    return when (minigame) {
        MinigameType.ODD_EVEN -> AppRoutes.OddEvenGame
        MinigameType.SEQUENCE -> AppRoutes.SequenceGame
        MinigameType.BLACKJACK -> AppRoutes.BlackjackGame
        MinigameType.HIGHER_LOWER -> AppRoutes.HigherLowerGame
    }
}
