package com.dejitarunoseireinoapuri.saikorodojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation.GameRoute
import com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation.MenuScreen
import com.dejitarunoseireinoapuri.saikorodojo.presentation.AppThemeUiEvent
import com.dejitarunoseireinoapuri.saikorodojo.presentation.AppThemeViewModel
import com.dejitarunoseireinoapuri.saikorodojo.ui.theme.SaikoroDojoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppThemeViewModel = viewModel()
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            val navController = rememberNavController()
            SaikoroDojoTheme(darkTheme = uiState.value.isDarkTheme) {
                NavHost(
                    navController = navController,
                    startDestination = "menu"
                ) {
                    composable("menu") {
                        MenuScreen(
                            isDarkTheme = uiState.value.isDarkTheme,
                            onToggleTheme = { viewModel.onEvent(AppThemeUiEvent.ToggleTheme) },
                            onPlayClick = { navController.navigate("game") },
                            onRulesClick = {}
                        )
                    }
                    composable("game") {
                        GameRoute()
                    }
                }
            }
        }
    }
}
