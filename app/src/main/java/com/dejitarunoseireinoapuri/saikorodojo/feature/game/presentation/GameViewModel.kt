package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data.InMemoryCardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ConsumeCardFromInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.GetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.defaultCardUiModels
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateLevelUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateObjectiveUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelDefinition
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RollDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.RunDiceRollTurnUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.data.GameSessionRepositoryProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavePendingMainGameSnapshotUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val DEFAULT_ROLL_DURATION_MS = 1_500L
private const val DEFAULT_TICK_MS = 150L
private const val LEVEL_COMPLETE_DELAY_MS = 1_000L
private const val MINIGAMES_REWARD_AMOUNT = 3
private const val LEVEL_MINIGAMES_REWARD_AMOUNT = 2
private const val MINIGAMES_FOR_INTERSTITIAL = 7

class GameViewModel(
    private val rollDiceUseCase: RollDiceUseCase = RollDiceUseCase(),
    private val generateLevelUseCase: GenerateLevelUseCase = GenerateLevelUseCase(),
    private val generateObjectiveUseCase: GenerateObjectiveUseCase = GenerateObjectiveUseCase(),
    private val runDiceRollTurnUseCase: RunDiceRollTurnUseCase = RunDiceRollTurnUseCase(),
    private val getCardInventoryUseCase: GetCardInventoryUseCase =
        GetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val consumeCardFromInventoryUseCase: ConsumeCardFromInventoryUseCase =
        ConsumeCardFromInventoryUseCase(InMemoryCardInventoryRepository.shared),
    private val setCardInventoryUseCase: SetCardInventoryUseCase =
        SetCardInventoryUseCase(InMemoryCardInventoryRepository.shared),
    loadGameSessionUseCase: LoadGameSessionUseCase =
        LoadGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val saveGameSessionUseCase: SaveGameSessionUseCase =
        SaveGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val clearGameSessionUseCase: ClearGameSessionUseCase =
        ClearGameSessionUseCase(GameSessionRepositoryProvider.provide()),
    private val savePendingMainGameSnapshotUseCase: SavePendingMainGameSnapshotUseCase =
        SavePendingMainGameSnapshotUseCase(GameSessionRepositoryProvider.provide()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val rollDurationMs: Long = DEFAULT_ROLL_DURATION_MS,
    private val tickMs: Long = DEFAULT_TICK_MS,
    private val layoutSeedProvider: () -> Long = { Random.nextLong() },
    baseSeedProvider: () -> Long = { Random.nextLong() },
    initialLevelDefinition: LevelDefinition? = null,
    cardUiModels: List<CardUiModel> = emptyList()
) : ViewModel() {
    private var baseSeed = 0L
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState

    private val _effects = MutableSharedFlow<GameUiEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<GameUiEffect> = _effects

    private var rollJob: Job? = null
    private var initialRollSnapshot: GameRollSnapshot? = null
    private var completionJob: Job? = null
    private var currentObjective: LevelObjective? = null
    private var currentLevelNumber: Int = 1
    private var shouldAutoStartRoll = true
    private var allowSessionSaving = true
    private var awaitingLevelInterstitialAd = false
    private val minigameTypes = MinigameType.entries

    init {
        baseSeed = baseSeedProvider()
        val restoredSession = loadGameSessionUseCase.execute()
        val restoredSnapshot = when (restoredSession) {
            is SavedSession.MainGame -> restoredSession.snapshot
            is SavedSession.Minigame -> restoredSession.mainGameSnapshot
            null -> null
        }
        if (restoredSnapshot != null) {
            shouldAutoStartRoll = restoreFromSnapshot(restoredSnapshot)
            if (restoredSession is SavedSession.Minigame) {
                savePendingMainGameSnapshotUseCase.execute(restoredSnapshot)
            }
        } else {
            val initialCards = cardUiModels.ifEmpty { loadInventoryCardModels() }
            applyLevelDefinition(
                levelDefinition = initialLevelDefinition
                    ?: generateLevelUseCase.execute(levelNumber = 1, seedBase = baseSeed),
                cardUiModels = initialCards
            )
        }
    }

    fun onEvent(event: GameUiEvent) {
        when (event) {
            GameUiEvent.StartRoll -> startRolling()
            GameUiEvent.RefreshInventory -> refreshCardInventory()
            is GameUiEvent.DiceClicked -> handleDiceClick(event.index)
            is GameUiEvent.SelectCard -> {
                if (!isCardInteractionBlocked(_uiState.value)) {
                    selectCard(event.index)
                }
            }
            is GameUiEvent.ApplyCard -> {
                if (!isCardInteractionBlocked(_uiState.value)) {
                    applyCard(event.index)
                }
            }
            is GameUiEvent.AdjustSelectedDie -> adjustSelectedDie(event.delta)
            is GameUiEvent.SetSelectedDieValue -> setSelectedDieValue(event.value)
            GameUiEvent.RollSelectedDice -> rollSelectedDice()
            GameUiEvent.RollSingleDie -> rollSingleDie()
            GameUiEvent.FlipSelectedDie -> flipSelectedDie()
            GameUiEvent.DismissSelectedCard -> {
                if (!isCardInteractionBlocked(_uiState.value)) {
                    dismissSelectedCard()
                }
            }
            GameUiEvent.IncreaseDiceCount -> increaseDiceCount()
            GameUiEvent.ConfirmSurrender -> confirmSurrender()
            GameUiEvent.ConfirmExit -> confirmExit()
            GameUiEvent.OpenRandomMinigame -> openRandomMinigame()
            GameUiEvent.ConfirmMinigamesAd -> confirmMinigamesAd()
            GameUiEvent.DismissMinigamesAdPrompt -> dismissMinigamesAdPrompt()
            GameUiEvent.MinigamesAdCompleted -> grantMinigamesFromAd()
            GameUiEvent.LevelInterstitialAdCompleted -> handleLevelInterstitialAdCompleted()
        }
    }

    fun saveSession() {
        if (!allowSessionSaving) return
        val snapshot = buildMainGameSnapshot()
        saveGameSessionUseCase.execute(SavedSession.MainGame(snapshot))
    }

    private fun openRandomMinigame() {
        val currentState = _uiState.value
        if (currentState.isMinigameButtonLocked) {
            return
        }
        if (currentState.minigamesAvailable <= 0) {
            _uiState.update { it.copy(showMinigamesAdPrompt = true) }
            return
        }
        _uiState.update {
            it.copy(
                minigamesAvailable = (it.minigamesAvailable - 1).coerceAtLeast(0),
                showMinigamesAdPrompt = false,
                minigamesPlayedSinceInterstitial = it.minigamesPlayedSinceInterstitial + 1,
                isMinigameButtonLocked = true
            )
        }
        val snapshot = buildMainGameSnapshot()
        savePendingMainGameSnapshotUseCase.execute(snapshot)
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMinigame(pickMinigame()))
        }
    }

    private fun confirmMinigamesAd() {
        _uiState.update { it.copy(showMinigamesAdPrompt = false) }
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.ShowMinigamesRewardedAd)
        }
    }

    private fun dismissMinigamesAdPrompt() {
        _uiState.update { it.copy(showMinigamesAdPrompt = false) }
    }

    private fun grantMinigamesFromAd() {
        _uiState.update {
            it.copy(minigamesAvailable = clampMinigamesAvailable(it.minigamesAvailable + MINIGAMES_REWARD_AMOUNT))
        }
    }

    private fun handleLevelInterstitialAdCompleted() {
        if (!awaitingLevelInterstitialAd) return
        awaitingLevelInterstitialAd = false
        advanceToNextLevel(resetMinigamesPlayed = true)
    }

    private fun confirmSurrender() {
        allowSessionSaving = false
        clearGameSessionUseCase.execute()
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMenu(resetProgress = true))
        }
    }

    private fun confirmExit() {
        val snapshot = buildMainGameSnapshot()
        saveGameSessionUseCase.execute(SavedSession.MainGame(snapshot))
        viewModelScope.launch(dispatcher) {
            _effects.emit(GameUiEffect.NavigateToMenu(resetProgress = false))
        }
    }

    private fun startRolling() {
        if (rollJob?.isActive == true) return
        if (initialRollSnapshot != null && currentObjective != null) {
            refreshCardInventory()
            return
        }

        rollJob = viewModelScope.launch(dispatcher) {
            val currentState = _uiState.value
            val seed = layoutSeedProvider()
            val plan = runDiceRollTurnUseCase.prepare(
                durationMs = rollDurationMs,
                tickMs = tickMs,
                diceTypes = currentState.diceTypes
            )
            val diceTypes = plan.selectedDiceTypes
            _uiState.update {
                it.copy(
                    isRolling = true,
                    layoutSeed = seed,
                    diceTypes = diceTypes,
                    interactionMode = DiceInteractionMode.Normal,
                    selectedRerollSingleDieIndex = null,
                    selectedFlipDieIndex = null,
                    selectedAdjustmentDieIndex = null,
                    selectedSetValueDieIndex = null
                )
            }

            repeat(plan.steps) {
                val values = rollDiceUseCase.execute(diceTypes)
                _uiState.update {
                    it.copy(
                        diceValues = values,
                        selectedDiceSum = calculateSelectedDiceSum(values, it.selectedDice)
                    )
                }
                delay(tickMs)
            }

            _uiState.update { it.copy(isRolling = false) }
            if (initialRollSnapshot == null) {
                val snapshotState = _uiState.value
                initialRollSnapshot = GameRollSnapshot(
                    diceValues = snapshotState.diceValues.toList(),
                    diceTypes = snapshotState.diceTypes.toList(),
                    layoutSeed = snapshotState.layoutSeed
                )
            }
            if (currentObjective == null) {
                currentObjective = generateObjectiveUseCase.execute(
                    levelNumber = currentLevelNumber,
                    diceTypes = _uiState.value.diceTypes,
                    seedBase = baseSeed
                )
            }
            refreshObjectiveProgress()
        }
    }

    private fun handleDiceClick(index: Int) {
        val state = _uiState.value
        if (isDiceInteractionBlocked(state)) return
        if (state.isAwaitingRerollSingle) {
            selectRerollSingleDie(index)
        } else if (state.isAwaitingRerollSelected) {
            toggleRerollDiceSelection(index)
        } else if (state.isAwaitingFlipFace) {
            selectFlipDie(index)
        } else if (state.isAwaitingAdjustPlusMinus) {
            selectAdjustmentDie(index)
        } else if (state.isAwaitingSetValue) {
            selectSetValueDie(index)
        } else {
            toggleDiceSelection(index)
        }
    }

    private fun toggleRerollDiceSelection(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedSelection = if (state.selectedRerollDice.contains(index)) {
                    state.selectedRerollDice - index
                } else {
                    state.selectedRerollDice + index
                }
                state.copy(selectedRerollDice = updatedSelection)
            }
        }
    }

    private fun toggleDiceSelection(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedSelection = if (state.selectedDice.contains(index)) {
                    state.selectedDice - index
                } else {
                    state.selectedDice + index
                }
                state.copy(
                    selectedDice = updatedSelection,
                    selectedDiceSum = calculateSelectedDiceSum(state.diceValues, updatedSelection)
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun selectCard(index: Int) {
        _uiState.update { state ->
            if (index !in state.cardUiModels.indices) {
                state
            } else {
                state.copy(selectedCardIndex = index)
            }
        }
    }

    private fun dismissSelectedCard() {
        _uiState.update { it.copy(selectedCardIndex = null) }
    }

    private fun increaseDiceCount() {
        val currentState = _uiState.value
        if (currentState.isRolling) return
        val newCount = currentState.diceCount + 1
        val newDiceValues = currentState.diceValues + 1
        val newDiceTypes = currentState.diceTypes + currentState.diceType
        val updatedSelection = currentState.selectedDice.filter { it < newCount }.toSet()
        _uiState.update { state ->
            state.copy(
                diceCount = newCount,
                diceValues = newDiceValues,
                diceTypes = newDiceTypes,
                interactionMode = DiceInteractionMode.Normal,
                selectedDice = updatedSelection,
                selectedDiceSum = calculateSelectedDiceSum(newDiceValues, updatedSelection),
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null
            )
        }
        initialRollSnapshot = null
        startRolling()
    }

    private fun applyCard(index: Int) {
        val repeatedCardId = applyRepeatLastCard(index)
        if (repeatedCardId != null) {
            return
        }
        val applied = applyRerollAllCard(index)
        if (applied) {
            return
        }
        if (applyRerollSingleCard(index)) {
            return
        }
        if (applyFlipFaceCard(index)) {
            return
        }
        if (applyAdjustPlusMinusCard(index)) {
            return
        }
        if (applyMinigamesCard(index)) {
            return
        }
        applySetValueCard(index)
    }

    private fun applyRepeatLastCard(index: Int): CardId? {
        var repeatedCardId: CardId? = null
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                val lastCardId = state.lastAppliedCardId
                if (card.id != CardId.REPEAT_LAST || lastCardId == null) {
                    state
                } else {
                    repeatedCardId = lastCardId
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, lastCardId, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = lastCardId
                    )
                }
            }
        }
        return repeatedCardId
    }

    private fun applyRerollSingleCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.REROLL_SINGLE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, card.id, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyAdjustPlusMinusCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.ADJUST_PLUS_MINUS_ONE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, card.id, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applySetValueCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.SET_VALUE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, card.id, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyMinigamesCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.MINIGAMES) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = state.copy(
                        selectedCardIndex = null,
                        minigamesAvailable = clampMinigamesAvailable(state.minigamesAvailable + MINIGAMES_REWARD_AMOUNT),
                        showMinigamesAdPrompt = false
                    )
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun selectRerollSingleDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedRerollSingleDieIndex = index)
            }
        }
    }

    private fun rollSingleDie() {
        val state = _uiState.value
        val selectedIndex = state.selectedRerollSingleDieIndex
        if (!state.isAwaitingRerollSingle || selectedIndex == null || rollJob?.isActive == true) return
        rollJob = viewModelScope.launch(dispatcher) {
            val plan = runDiceRollTurnUseCase.prepare(
                durationMs = rollDurationMs,
                tickMs = tickMs,
                diceTypes = state.diceTypes,
                selectedIndices = listOf(selectedIndex)
            )
            _uiState.update {
                it.copy(
                    isRolling = true,
                    interactionMode = DiceInteractionMode.Normal,
                    selectedRerollSingleDieIndex = null
                )
            }
            repeat(plan.steps) {
                val rolledValues = rollDiceUseCase.execute(plan.selectedDiceTypes)
                _uiState.update { currentState ->
                    val updatedValues = runDiceRollTurnUseCase.applyValues(
                        currentValues = currentState.diceValues,
                        selectedIndices = plan.selectedIndices,
                        rolledValues = rolledValues
                    )
                    currentState.copy(
                        diceValues = updatedValues,
                        selectedDiceSum = calculateSelectedDiceSum(updatedValues, currentState.selectedDice)
                    )
                }
                delay(tickMs)
            }
            _uiState.update { it.copy(isRolling = false) }
            refreshObjectiveProgress()
        }
    }

    private fun selectFlipDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                val updatedIndex = if (state.selectedFlipDieIndex == index) null else index
                state.copy(selectedFlipDieIndex = updatedIndex)
            }
        }
    }

    private fun flipSelectedDie() {
        _uiState.update { state ->
            val index = state.selectedFlipDieIndex
            if (!state.isAwaitingFlipFace || index == null || index !in state.diceValues.indices) {
                state
            } else {
                val currentValue = state.diceValues[index]
                val diceType = state.diceTypes.getOrElse(index) { state.diceType }
                val flippedValue = (diceType.sides + 1 - currentValue).coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[index] = flippedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    interactionMode = DiceInteractionMode.Normal,
                    selectedFlipDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun selectAdjustmentDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedAdjustmentDieIndex = index)
            }
        }
    }

    private fun selectSetValueDie(index: Int) {
        _uiState.update { state ->
            if (index !in state.diceValues.indices) {
                state
            } else {
                state.copy(selectedSetValueDieIndex = index)
            }
        }
    }

    private fun adjustSelectedDie(delta: Int) {
        _uiState.update { state ->
            val selectedIndex = state.selectedAdjustmentDieIndex
            if (!state.isAwaitingAdjustPlusMinus || selectedIndex == null) {
                state
            } else if (selectedIndex !in state.diceValues.indices) {
                state.copy(
                    interactionMode = DiceInteractionMode.Normal,
                    selectedAdjustmentDieIndex = null
                )
            } else {
                val diceType = state.diceTypes.getOrElse(selectedIndex) { state.diceType }
                val currentValue = state.diceValues[selectedIndex]
                val updatedValue = (currentValue + delta).coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[selectedIndex] = updatedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    interactionMode = DiceInteractionMode.Normal,
                    selectedAdjustmentDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun setSelectedDieValue(value: Int) {
        _uiState.update { state ->
            val selectedIndex = state.selectedSetValueDieIndex
            if (!state.isAwaitingSetValue || selectedIndex == null) {
                state
            } else if (selectedIndex !in state.diceValues.indices) {
                state.copy(
                    interactionMode = DiceInteractionMode.Normal,
                    selectedSetValueDieIndex = null
                )
            } else {
                val diceType = state.diceTypes.getOrElse(selectedIndex) { state.diceType }
                val updatedValue = value.coerceIn(1, diceType.sides)
                val updatedValues = state.diceValues.toMutableList().apply {
                    this[selectedIndex] = updatedValue
                }
                state.copy(
                    diceValues = updatedValues,
                    selectedDiceSum = calculateSelectedDiceSum(updatedValues, state.selectedDice),
                    interactionMode = DiceInteractionMode.Normal,
                    selectedSetValueDieIndex = null
                )
            }
        }
        refreshObjectiveProgress()
    }

    private fun applyRerollAllCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.REROLL_ALL) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, card.id, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun applyFlipFaceCard(index: Int): Boolean {
        var applied = false
        _uiState.update { state ->
            val cards = state.cardUiModels
            if (index !in cards.indices) {
                state
            } else {
                val card = cards[index]
                if (card.id != CardId.FLIP_FACE) {
                    state
                } else {
                    applied = true
                    val updatedCards = consumeCard(cards, index)
                    val updatedState = GameCardEffectReducer.reduce(state, card.id, MINIGAMES_REWARD_AMOUNT)
                    updatedState.copy(
                        cardUiModels = updatedCards,
                        lastAppliedCardId = card.id
                    )
                }
            }
        }
        return applied
    }

    private fun rollSelectedDice() {
        val state = _uiState.value
        if (!state.isAwaitingRerollSelected || rollJob?.isActive == true) return
        val selectedIndices = state.selectedRerollDice.toList()
        if (selectedIndices.isEmpty()) return
        rollJob = viewModelScope.launch(dispatcher) {
            val plan = runDiceRollTurnUseCase.prepare(
                durationMs = rollDurationMs,
                tickMs = tickMs,
                diceTypes = state.diceTypes,
                selectedIndices = selectedIndices
            )
            _uiState.update {
                it.copy(
                    isRolling = true,
                    interactionMode = DiceInteractionMode.Normal,
                    selectedRerollDice = emptySet()
                )
            }
            repeat(plan.steps) {
                val rolledValues = rollDiceUseCase.execute(plan.selectedDiceTypes)
                _uiState.update { currentState ->
                    val updatedValues = runDiceRollTurnUseCase.applyValues(
                        currentValues = currentState.diceValues,
                        selectedIndices = plan.selectedIndices,
                        rolledValues = rolledValues
                    )
                    currentState.copy(
                        diceValues = updatedValues,
                        selectedDiceSum = calculateSelectedDiceSum(updatedValues, currentState.selectedDice)
                    )
                }
                delay(tickMs)
            }
            _uiState.update { it.copy(isRolling = false) }
            refreshObjectiveProgress()
        }
    }

    private fun consumeCard(cards: List<CardUiModel>, index: Int): List<CardUiModel> {
        val updatedCards = cards.toMutableList()
        val card = updatedCards.getOrNull(index) ?: return cards
        consumeCardFromInventoryUseCase.execute(card.id)
        if (card.count > 1) {
            updatedCards[index] = card.copy(count = card.count - 1)
        } else {
            updatedCards.removeAt(index)
        }
        return updatedCards
    }

    private fun applyLevelDefinition(
        levelDefinition: LevelDefinition,
        cardUiModels: List<CardUiModel> = _uiState.value.cardUiModels,
        minigamesAvailable: Int = _uiState.value.minigamesAvailable,
        minigamesPlayedSinceInterstitial: Int = _uiState.value.minigamesPlayedSinceInterstitial
    ) {
        currentLevelNumber = levelDefinition.levelNumber
        currentObjective = null
        val diceValues = List(levelDefinition.diceCount) { 1 }
        _uiState.update { state ->
            state.copy(
                diceValues = diceValues,
                diceCount = levelDefinition.diceCount,
                diceType = levelDefinition.diceTypes.firstOrNull() ?: DiceType.D6,
                diceTypes = levelDefinition.diceTypes,
                layoutSeed = 0L,
                isRolling = false,
                interactionMode = DiceInteractionMode.Normal,
                selectedDice = emptySet(),
                selectedRerollDice = emptySet(),
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null,
                selectedDiceSum = 0,
                shouldShowSelectedSum = false,
                cardUiModels = cardUiModels,
                selectedCardIndex = null,
                lastAppliedCardId = null,
                levelNumber = levelDefinition.levelNumber,
                objectiveLines = emptyList(),
                isLevelComplete = false,
                showLevelCompleteMessage = false,
                minigamesAvailable = clampMinigamesAvailable(minigamesAvailable),
                showMinigamesAdPrompt = false,
                minigamesPlayedSinceInterstitial = minigamesPlayedSinceInterstitial,
                isMinigameButtonLocked = false
            )
        }
        initialRollSnapshot = null
    }

    private fun refreshCardInventory() {
        val updatedCards = loadInventoryCardModels()
        _uiState.update {
            it.copy(
                cardUiModels = updatedCards,
                isMinigameButtonLocked = false
            )
        }
    }

    private fun loadInventoryCardModels(): List<CardUiModel> {
        val counts = getCardInventoryUseCase.execute()
        return defaultCardUiModels().mapNotNull { card ->
            val count = counts[card.id] ?: 0
            if (count > 0) {
                card.copy(count = count)
            } else {
                null
            }
        }
    }

    private fun refreshObjectiveProgress() {
        val objective = currentObjective ?: return
        val state = _uiState.value
        val showSelectedSum = shouldShowSelectedSum(objective.conditions)
        val selectedIndices = state.selectedDice.toList().sorted()
        val selectedValues = selectedIndices.mapNotNull { index -> state.diceValues.getOrNull(index) }
        val selectedSides = selectedIndices.mapNotNull { index -> state.diceTypes.getOrNull(index)?.sides }
        if (selectedValues.isEmpty()) {
            val lines = buildObjectiveLines(objective, emptyList(), emptyList()).map { line ->
                line.copy(isMet = false)
            }
            _uiState.update {
                it.copy(
                    objectiveLines = lines,
                    isLevelComplete = false,
                    shouldShowSelectedSum = showSelectedSum
                )
            }
            return
        }
        val lines = buildObjectiveLines(objective, selectedValues, selectedSides)
        val completed = lines.all { it.isMet }
        val wasComplete = _uiState.value.isLevelComplete
        _uiState.update {
            it.copy(
                objectiveLines = lines,
                isLevelComplete = completed,
                shouldShowSelectedSum = showSelectedSum
            )
        }
        if (completed && !wasComplete && !_uiState.value.isRolling) {
            handleLevelComplete()
        }
    }

    internal fun shouldStartRollOnLaunch(): Boolean {
        return shouldAutoStartRoll
    }

    private fun restoreFromSnapshot(snapshot: MainGameSnapshot): Boolean {
        val uiSnapshot = snapshot.uiSnapshot
        val shouldAdvanceCompletedLevel = uiSnapshot.isLevelComplete
        val shouldResumeInterruptedRoll = uiSnapshot.isRolling
        val shouldStartInitialRoll = snapshot.currentObjective == null && snapshot.initialRollSnapshot == null
        baseSeed = snapshot.baseSeed
        currentObjective = snapshot.currentObjective
        initialRollSnapshot = snapshot.initialRollSnapshot
        currentLevelNumber = uiSnapshot.levelNumber
        setCardInventoryUseCase.execute(uiSnapshot.cardCounts)
        val restoredCards = buildCardUiModels(uiSnapshot.cardCounts)
        _uiState.update {
            it.copy(
                diceValues = uiSnapshot.diceValues,
                diceCount = uiSnapshot.diceCount,
                diceType = uiSnapshot.diceType,
                diceTypes = uiSnapshot.diceTypes,
                layoutSeed = uiSnapshot.layoutSeed,
                isRolling = false,
                interactionMode = restoreInteractionMode(uiSnapshot),
                selectedDice = uiSnapshot.selectedDice,
                selectedRerollDice = uiSnapshot.selectedRerollDice,
                selectedRerollSingleDieIndex = uiSnapshot.selectedRerollSingleDieIndex,
                selectedFlipDieIndex = uiSnapshot.selectedFlipDieIndex,
                selectedAdjustmentDieIndex = uiSnapshot.selectedAdjustmentDieIndex,
                selectedSetValueDieIndex = uiSnapshot.selectedSetValueDieIndex,
                selectedDiceSum = uiSnapshot.selectedDiceSum,
                shouldShowSelectedSum = uiSnapshot.shouldShowSelectedSum,
                cardUiModels = restoredCards,
                selectedCardIndex = uiSnapshot.selectedCardIndex,
                lastAppliedCardId = uiSnapshot.lastAppliedCardId,
                levelNumber = uiSnapshot.levelNumber,
                objectiveLines = emptyList(),
                isLevelComplete = uiSnapshot.isLevelComplete,
                showLevelCompleteMessage = uiSnapshot.showLevelCompleteMessage,
                minigamesAvailable = clampMinigamesAvailable(uiSnapshot.minigamesAvailable),
                showMinigamesAdPrompt = false,
                minigamesPlayedSinceInterstitial = uiSnapshot.minigamesPlayedSinceInterstitial,
                isMinigameButtonLocked = false
            )
        }
        refreshObjectiveProgress()
        if (shouldAdvanceCompletedLevel && _uiState.value.isLevelComplete) {
            advanceToNextLevel(resetMinigamesPlayed = false)
            return false
        }
        return shouldResumeInterruptedRoll || shouldStartInitialRoll
    }

    private fun buildMainGameSnapshot(): MainGameSnapshot {
        val state = _uiState.value
        val cardCounts = getCardInventoryUseCase.execute()
        val uiSnapshot = GameUiSnapshot(
            diceValues = state.diceValues,
            diceCount = state.diceCount,
            diceType = state.diceType,
            diceTypes = state.diceTypes,
            layoutSeed = state.layoutSeed,
            isRolling = state.isRolling,
            isAwaitingRerollSingle = state.isAwaitingRerollSingle,
            isAwaitingRerollSelected = state.isAwaitingRerollSelected,
            isAwaitingFlipFace = state.isAwaitingFlipFace,
            isAwaitingAdjustPlusMinus = state.isAwaitingAdjustPlusMinus,
            isAwaitingSetValue = state.isAwaitingSetValue,
            selectedDice = state.selectedDice,
            selectedRerollDice = state.selectedRerollDice,
            selectedRerollSingleDieIndex = state.selectedRerollSingleDieIndex,
            selectedFlipDieIndex = state.selectedFlipDieIndex,
            selectedAdjustmentDieIndex = state.selectedAdjustmentDieIndex,
            selectedSetValueDieIndex = state.selectedSetValueDieIndex,
            selectedDiceSum = state.selectedDiceSum,
            shouldShowSelectedSum = state.shouldShowSelectedSum,
            cardCounts = cardCounts,
            selectedCardIndex = state.selectedCardIndex,
            lastAppliedCardId = state.lastAppliedCardId,
            levelNumber = state.levelNumber,
            isLevelComplete = state.isLevelComplete,
            showLevelCompleteMessage = state.showLevelCompleteMessage,
            minigamesAvailable = state.minigamesAvailable,
            minigamesPlayedSinceInterstitial = state.minigamesPlayedSinceInterstitial
        )
        return MainGameSnapshot(
            uiSnapshot = uiSnapshot,
            baseSeed = baseSeed,
            currentObjective = currentObjective,
            initialRollSnapshot = initialRollSnapshot
        )
    }

    private fun buildCardUiModels(cardCounts: Map<CardId, Int>): List<CardUiModel> {
        return defaultCardUiModels().mapNotNull { card ->
            val count = cardCounts[card.id] ?: 0
            if (count > 0) {
                card.copy(count = count)
            } else {
                null
            }
        }
    }

    private fun handleLevelComplete() {
        if (completionJob?.isActive == true) return
        completionJob = viewModelScope.launch(dispatcher) {
            delay(LEVEL_COMPLETE_DELAY_MS)
            val minigamesPlayed = _uiState.value.minigamesPlayedSinceInterstitial
            if (minigamesPlayed >= MINIGAMES_FOR_INTERSTITIAL) {
                awaitingLevelInterstitialAd = true
                _effects.emit(GameUiEffect.ShowLevelInterstitialAd)
            } else {
                advanceToNextLevel(resetMinigamesPlayed = false)
            }
        }
    }

    private fun advanceToNextLevel(resetMinigamesPlayed: Boolean) {
        val updatedMinigames = clampMinigamesAvailable(_uiState.value.minigamesAvailable + LEVEL_MINIGAMES_REWARD_AMOUNT)
        val nextLevel = (_uiState.value.levelNumber + 1).coerceAtLeast(1)
        val nextDefinition = generateLevelUseCase.execute(
            levelNumber = nextLevel,
            seedBase = baseSeed
        )
        val updatedMinigamesPlayed = if (resetMinigamesPlayed) {
            0
        } else {
            _uiState.value.minigamesPlayedSinceInterstitial
        }
        applyLevelDefinition(
            levelDefinition = nextDefinition,
            minigamesAvailable = updatedMinigames,
            minigamesPlayedSinceInterstitial = updatedMinigamesPlayed
        )
        startRolling()
    }

    private fun pickMinigame(): MinigameType {
        return minigameTypes.random()
    }

}
