package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ResetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectStartingCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.InMemoryGameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.HasSavedGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MenuDestination {
    data class MainGame(val resetSession: Boolean) : MenuDestination
    data class Minigame(val minigameType: MinigameType) : MenuDestination
}

data class MenuUiState(
    val hasSavedSession: Boolean = false,
    val showContinueDialog: Boolean = false
)

sealed interface MenuUiEvent {
    data object PlayClicked : MenuUiEvent
    data object ContinueGame : MenuUiEvent
    data object StartNewGame : MenuUiEvent
    data object DismissDialog : MenuUiEvent
}

sealed interface MenuUiEffect {
    data class NavigateTo(val destination: MenuDestination) : MenuUiEffect
}

class MenuViewModel(
    private val hasSavedGameSessionUseCase: HasSavedGameSessionUseCase =
        HasSavedGameSessionUseCase(InMemoryGameSessionRepository.shared),
    private val loadGameSessionUseCase: LoadGameSessionUseCase =
        LoadGameSessionUseCase(InMemoryGameSessionRepository.shared),
    private val clearGameSessionUseCase: ClearGameSessionUseCase =
        ClearGameSessionUseCase(InMemoryGameSessionRepository.shared),
    private val resetCardInventoryUseCase: ResetCardInventoryUseCase =
        ResetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val addCardsToInventoryUseCase: AddCardsToInventoryUseCase =
        AddCardsToInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val selectStartingCardsUseCase: SelectStartingCardsUseCase = SelectStartingCardsUseCase()
) : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState

    private val _effects = MutableSharedFlow<MenuUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MenuUiEffect> = _effects

    init {
        refreshSavedSession()
    }

    fun onEvent(event: MenuUiEvent) {
        when (event) {
            MenuUiEvent.PlayClicked -> handlePlay()
            MenuUiEvent.ContinueGame -> handleContinue()
            MenuUiEvent.StartNewGame -> startNewGame()
            MenuUiEvent.DismissDialog -> _uiState.update { it.copy(showContinueDialog = false) }
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
        val session = loadGameSessionUseCase.execute()
        val destination = when (session) {
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
