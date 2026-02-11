package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ResetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectStartingCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.HasSavedGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.data.SoundSettingsRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.GetSoundEnabledUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.ObserveSoundEnabledUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sound.domain.ToggleSoundEnabledUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MenuDestination {
    data class MainGame(val resetSession: Boolean) : MenuDestination
    data class Minigame(val minigameType: MinigameType) : MenuDestination
    data object Settings : MenuDestination
}

data class MenuUiState(
    val hasSavedSession: Boolean = false,
    val showContinueDialog: Boolean = false,
    val isSoundEnabled: Boolean = true
)

sealed interface MenuUiEvent {
    data object PlayClicked : MenuUiEvent
    data object ContinueGame : MenuUiEvent
    data object StartNewGame : MenuUiEvent
    data object DismissDialog : MenuUiEvent
    data object SoundToggleClicked : MenuUiEvent
    data object SettingsClicked : MenuUiEvent
}

sealed interface MenuUiEffect {
    data class NavigateTo(val destination: MenuDestination) : MenuUiEffect
}

class MenuViewModel(
    private val hasSavedGameSessionUseCase: HasSavedGameSessionUseCase =
        HasSavedGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val loadGameSessionUseCase: LoadGameSessionUseCase =
        LoadGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val clearGameSessionUseCase: ClearGameSessionUseCase =
        ClearGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val resetCardInventoryUseCase: ResetCardInventoryUseCase =
        ResetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val addCardsToInventoryUseCase: AddCardsToInventoryUseCase =
        AddCardsToInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val selectStartingCardsUseCase: SelectStartingCardsUseCase = SelectStartingCardsUseCase(),
    private val getSoundEnabledUseCase: GetSoundEnabledUseCase =
        GetSoundEnabledUseCase(SoundSettingsRepositoryProvider.provide()),
    private val observeSoundEnabledUseCase: ObserveSoundEnabledUseCase =
        ObserveSoundEnabledUseCase(SoundSettingsRepositoryProvider.provide()),
    private val toggleSoundEnabledUseCase: ToggleSoundEnabledUseCase =
        ToggleSoundEnabledUseCase(SoundSettingsRepositoryProvider.provide())
) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState

    private val _effects = MutableSharedFlow<MenuUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MenuUiEffect> = _effects

    init {
        refreshSavedSession()
        _uiState.update { it.copy(isSoundEnabled = getSoundEnabledUseCase.execute()) }
        viewModelScope.launch {
            observeSoundEnabledUseCase.execute().collect { isEnabled ->
                _uiState.update { it.copy(isSoundEnabled = isEnabled) }
            }
        }
    }

    fun onEvent(event: MenuUiEvent) {
        when (event) {
            MenuUiEvent.PlayClicked -> handlePlay()
            MenuUiEvent.ContinueGame -> handleContinue()
            MenuUiEvent.StartNewGame -> startNewGame()
            MenuUiEvent.DismissDialog -> _uiState.update { it.copy(showContinueDialog = false) }
            MenuUiEvent.SoundToggleClicked -> handleSoundToggle()
            MenuUiEvent.SettingsClicked -> navigateToSettings()
        }
    }

    fun refreshSavedSession() {
        _uiState.update { it.copy(hasSavedSession = hasSavedGameSessionUseCase.execute()) }
    }

    private fun handlePlay() {
        if (_uiState.value.hasSavedSession) {
            _uiState.update { it.copy(showContinueDialog = true) }
        } else {
            startNewGame()
        }
    }

    private fun handleContinue() {
        val destination = when (val session = loadGameSessionUseCase.execute()) {
            is SavedSession.MainGame -> MenuDestination.MainGame(resetSession = false)
            is SavedSession.Minigame -> MenuDestination.Minigame(session.minigameType)
            null -> {
                startNewGame()
                return
            }
        }
        _uiState.update { it.copy(showContinueDialog = false) }
        viewModelScope.launch {
            _effects.emit(MenuUiEffect.NavigateTo(destination))
        }
    }

    private fun handleSoundToggle() {
        val isEnabled = toggleSoundEnabledUseCase.execute()
        _uiState.update { it.copy(isSoundEnabled = isEnabled) }
    }


    private fun navigateToSettings() {
        viewModelScope.launch {
            _effects.emit(MenuUiEffect.NavigateTo(MenuDestination.Settings))
        }
    }

    private fun startNewGame() {
        clearGameSessionUseCase.execute()
        resetCardInventoryUseCase.execute()
        val startingCards = selectStartingCardsUseCase.execute()
        addCardsToInventoryUseCase.execute(startingCards)
        _uiState.update { it.copy(hasSavedSession = false, showContinueDialog = false) }
        viewModelScope.launch {
            _effects.emit(MenuUiEffect.NavigateTo(MenuDestination.MainGame(resetSession = true)))
        }
    }
}
