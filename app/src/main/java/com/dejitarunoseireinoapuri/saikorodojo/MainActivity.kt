package com.dejitarunoseireinoapuri.saikorodojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
            SaikoroDojoTheme(darkTheme = uiState.value.isDarkTheme) {
                MenuScreen(
                    isDarkTheme = uiState.value.isDarkTheme,
                    onToggleTheme = { viewModel.onEvent(AppThemeUiEvent.ToggleTheme) },
                    onPlayClick = {},
                    onRulesClick = {}
                )
            }
        }
    }
}
