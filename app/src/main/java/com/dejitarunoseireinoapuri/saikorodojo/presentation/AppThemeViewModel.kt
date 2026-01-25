package com.dejitarunoseireinoapuri.saikorodojo.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class AppThemeUiState(
    val isDarkTheme: Boolean = false
)

sealed interface AppThemeUiEvent {
    data object ToggleTheme : AppThemeUiEvent
}

class AppThemeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppThemeUiState())
    val uiState: StateFlow<AppThemeUiState> = _uiState

    fun onEvent(event: AppThemeUiEvent) {
        when (event) {
            AppThemeUiEvent.ToggleTheme -> _uiState.update { state ->
                state.copy(isDarkTheme = !state.isDarkTheme)
            }
        }
    }
}
