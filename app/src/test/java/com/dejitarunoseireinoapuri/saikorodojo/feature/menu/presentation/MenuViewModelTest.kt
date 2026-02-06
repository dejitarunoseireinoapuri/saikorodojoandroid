package com.dejitarunoseireinoapuri.saikorodojo.feature.menu.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardInventoryRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectStartingCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.AddCardsToInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.ResetCardInventoryUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.HasSavedGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MinigameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `play starts new game when no save exists`() = runTest {
        val cardRepo = FakeCardInventoryRepository()
        val sessionRepo = FakeGameSessionRepository()
        val viewModel = MenuViewModel(
            hasSavedGameSessionUseCase = HasSavedGameSessionUseCase(sessionRepo),
            loadGameSessionUseCase = LoadGameSessionUseCase(sessionRepo),
            clearGameSessionUseCase = ClearGameSessionUseCase(sessionRepo),
            resetCardInventoryUseCase = ResetCardInventoryUseCase(cardRepo),
            addCardsToInventoryUseCase = AddCardsToInventoryUseCase(cardRepo),
            selectStartingCardsUseCase = SelectStartingCardsUseCase(
                randomProvider = RewardCardsRandomProvider { 0f }
            )
        )

        viewModel.onEvent(MenuUiEvent.PlayClicked)

        val effect = viewModel.effects.first()
        assertEquals(MenuUiEffect.NavigateTo(MenuDestination.MainGame(resetSession = true)), effect)
        assertEquals(3, cardRepo.getCounts().values.sum())
    }

    @Test
    fun `play shows continue dialog when save exists`() = runTest {
        val cardRepo = FakeCardInventoryRepository()
        val sessionRepo = FakeGameSessionRepository().apply {
            saveSession(SavedSession.Minigame(MinigameType.ODD_EVEN, buildOddEvenSnapshot(), buildMainSnapshot()))
        }
        val viewModel = MenuViewModel(
            hasSavedGameSessionUseCase = HasSavedGameSessionUseCase(sessionRepo),
            loadGameSessionUseCase = LoadGameSessionUseCase(sessionRepo),
            clearGameSessionUseCase = ClearGameSessionUseCase(sessionRepo),
            resetCardInventoryUseCase = ResetCardInventoryUseCase(cardRepo),
            addCardsToInventoryUseCase = AddCardsToInventoryUseCase(cardRepo),
            selectStartingCardsUseCase = SelectStartingCardsUseCase(
                randomProvider = RewardCardsRandomProvider { 0f }
            )
        )

        viewModel.onEvent(MenuUiEvent.PlayClicked)

        assertTrue(viewModel.uiState.value.showContinueDialog)
    }

    @Test
    fun `continue navigates to saved minigame`() = runTest {
        val cardRepo = FakeCardInventoryRepository()
        val sessionRepo = FakeGameSessionRepository().apply {
            saveSession(SavedSession.Minigame(MinigameType.SEQUENCE, buildSequenceSnapshot(), buildMainSnapshot()))
        }
        val viewModel = MenuViewModel(
            hasSavedGameSessionUseCase = HasSavedGameSessionUseCase(sessionRepo),
            loadGameSessionUseCase = LoadGameSessionUseCase(sessionRepo),
            clearGameSessionUseCase = ClearGameSessionUseCase(sessionRepo),
            resetCardInventoryUseCase = ResetCardInventoryUseCase(cardRepo),
            addCardsToInventoryUseCase = AddCardsToInventoryUseCase(cardRepo),
            selectStartingCardsUseCase = SelectStartingCardsUseCase(
                randomProvider = RewardCardsRandomProvider { 0f }
            )
        )

        viewModel.onEvent(MenuUiEvent.ContinueGame)

        val effect = viewModel.effects.first()
        assertEquals(MenuUiEffect.NavigateTo(MenuDestination.Minigame(MinigameType.SEQUENCE)), effect)
    }

    private fun buildMainSnapshot(): MainGameSnapshot {
        return MainGameSnapshot(
            uiSnapshot = GameUiSnapshot(
                diceValues = listOf(1, 2, 3),
                diceCount = 3,
                diceType = DiceType.D6,
                diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6),
                layoutSeed = 12L,
                isRolling = false,
                isAwaitingRerollSingle = false,
                isAwaitingRerollSelected = false,
                isAwaitingFlipFace = false,
                isAwaitingAdjustPlusMinus = false,
                isAwaitingSetValue = false,
                selectedDice = emptySet(),
                selectedRerollDice = emptySet(),
                selectedRerollSingleDieIndex = null,
                selectedFlipDieIndex = null,
                selectedAdjustmentDieIndex = null,
                selectedSetValueDieIndex = null,
                selectedDiceSum = 0,
                shouldShowSelectedSum = false,
                cardCounts = emptyMap(),
                selectedCardIndex = null,
                lastAppliedCardId = null,
                levelNumber = 1,
                isLevelComplete = false,
                showLevelCompleteMessage = false
            ),
            baseSeed = 7L,
            currentObjective = LevelObjective(emptyList()),
            initialRollSnapshot = GameRollSnapshot(
                diceValues = listOf(1, 1, 1),
                diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6),
                layoutSeed = 3L
            )
        )
    }

    private fun buildOddEvenSnapshot(): MinigameSnapshot.OddEven {
        return MinigameSnapshot.OddEven(
            isStarted = true,
            currentRound = 1,
            totalRounds = 5,
            correctCount = 0,
            wrongCount = 0,
            targetCorrect = 3,
            selectedChoice = null,
            diceValue = null,
            isRolling = false,
            showFireworks = false,
            showFailure = false,
            isComplete = false,
            rewardCardIds = emptyList()
        )
    }

    private fun buildSequenceSnapshot(): MinigameSnapshot.Sequence {
        return MinigameSnapshot.Sequence(
            isStarted = true,
            isRolling = false,
            isAwaitingDecision = false,
            currentRoll = 1,
            totalRolls = 5,
            targetSequence = 3,
            maxDiscards = 3,
            discardCount = 0,
            savedValues = emptyList(),
            diceValue = null,
            isComplete = false,
            rewardCardIds = emptyList(),
            pendingRewardCardIds = emptyList(),
            failureReason = null,
            failureDieValue = null,
            isLatestSavedValueHidden = false
        )
    }
}

private class FakeCardInventoryRepository : CardInventoryRepository {
    private val counts: MutableMap<CardId, Int> = mutableMapOf()

    override fun getCounts(): Map<CardId, Int> = counts.toMap()

    override fun addCards(cardIds: List<CardId>) {
        cardIds.forEach { cardId ->
            counts[cardId] = (counts[cardId] ?: 0) + 1
        }
    }

    override fun consumeCard(cardId: CardId) {
        val current = counts[cardId] ?: return
        if (current <= 1) {
            counts.remove(cardId)
        } else {
            counts[cardId] = current - 1
        }
    }

    override fun setCounts(counts: Map<CardId, Int>) {
        this.counts.clear()
        this.counts.putAll(counts)
    }
}

private class FakeGameSessionRepository : GameSessionRepository {
    private var session: SavedSession? = null

    override fun saveSession(session: SavedSession) {
        this.session = session
    }

    override fun loadSession(): SavedSession? {
        return session
    }

    override fun clearSession() {
        session = null
    }

    override fun hasSession(): Boolean {
        return session != null
    }

    override fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot) = Unit

    override fun getPendingMainGameSnapshot(): MainGameSnapshot? = null
}
