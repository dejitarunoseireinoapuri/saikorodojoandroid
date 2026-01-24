package com.dejitarunoseireinoapuri.saikorodojo.presentation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppThemeViewModelTest {
    @Test
    fun toggleThemeFlipsDarkThemeState() {
        val viewModel = AppThemeViewModel()

        assertFalse(viewModel.uiState.value.isDarkTheme)

        viewModel.onEvent(AppThemeUiEvent.ToggleTheme)

        assertTrue(viewModel.uiState.value.isDarkTheme)
    }
}
