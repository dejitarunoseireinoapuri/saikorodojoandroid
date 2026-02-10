package com.dejitarunoseireinoapuri.saikorodojo.feature.minigame.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DiceRoller as BlackjackDiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.RollBlackjackDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation.BlackjackGameUiEvent
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation.BlackjackGameViewModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.DiceRoller as HigherLowerDiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.HigherLowerChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.domain.RollHigherLowerUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation.HigherLowerGameUiEvent
import com.dejitarunoseireinoapuri.saikorodojo.feature.higherlower.presentation.HigherLowerGameViewModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller as OddEvenDiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation.OddEvenGameUiEvent
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation.OddEvenGameViewModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.DiceRoller as SequenceDiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.domain.RollSequenceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation.SequenceGameUiEvent
import com.dejitarunoseireinoapuri.saikorodojo.feature.sequence.presentation.SequenceGameViewModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GetPendingMainGameSnapshotUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.LoadGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MinigameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SaveGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MinigameSessionClearingTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun oddEvenSavesCompletedSessionWhenAppSavesState() = runTest {
        val repository = FakeGameSessionRepository()
        repository.savePendingMainGameSnapshot(TEST_MAIN_GAME_SNAPSHOT)
        val viewModel = OddEvenGameViewModel(
            rollOddEvenUseCase = RollOddEvenUseCase(diceRoller = OddEvenDiceRoller { 2 }),
            loadGameSessionUseCase = LoadGameSessionUseCase(repository),
            saveGameSessionUseCase = SaveGameSessionUseCase(repository),
            getPendingMainGameSnapshotUseCase = GetPendingMainGameSnapshotUseCase(repository),
            clearGameSessionUseCase = ClearGameSessionUseCase(repository),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L,
            lossMessageDelayMs = 0L,
            totalRounds = 1,
            targetCorrect = 1
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        advanceUntilIdle()
        viewModel.saveSession()

        val saved = repository.savedSession as SavedSession.Minigame
        val snapshot = saved.minigameSnapshot as MinigameSnapshot.OddEven
        assertEquals(MinigameType.ODD_EVEN, saved.minigameType)
        assertTrue(snapshot.isComplete)
        assertEquals(0, repository.clearCalls)
    }

    @Test
    fun sequenceSavesCompletedSessionWhenAppSavesState() = runTest {
        val repository = FakeGameSessionRepository()
        repository.savePendingMainGameSnapshot(TEST_MAIN_GAME_SNAPSHOT)
        val viewModel = SequenceGameViewModel(
            rollSequenceUseCase = RollSequenceUseCase(diceRoller = SequenceDiceRoller { 1 }),
            loadGameSessionUseCase = LoadGameSessionUseCase(repository),
            saveGameSessionUseCase = SaveGameSessionUseCase(repository),
            getPendingMainGameSnapshotUseCase = GetPendingMainGameSnapshotUseCase(repository),
            clearGameSessionUseCase = ClearGameSessionUseCase(repository),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            rewardRevealDelayMs = 0L,
            totalRolls = 1,
            targetSequence = 1,
            maxDiscards = 1
        )

        viewModel.onEvent(SequenceGameUiEvent.StartGame)
        advanceUntilIdle()
        viewModel.onEvent(SequenceGameUiEvent.SaveRoll)
        advanceUntilIdle()
        viewModel.saveSession()

        val saved = repository.savedSession as SavedSession.Minigame
        val snapshot = saved.minigameSnapshot as MinigameSnapshot.Sequence
        assertEquals(MinigameType.SEQUENCE, saved.minigameType)
        assertTrue(snapshot.isComplete)
        assertEquals(0, repository.clearCalls)
    }

    @Test
    fun higherLowerSavesCompletedSessionWhenAppSavesState() = runTest {
        val repository = FakeGameSessionRepository()
        repository.savePendingMainGameSnapshot(TEST_MAIN_GAME_SNAPSHOT)
        val viewModel = HigherLowerGameViewModel(
            rollHigherLowerUseCase = RollHigherLowerUseCase(diceRoller = HigherLowerDiceRoller { 3 }),
            loadGameSessionUseCase = LoadGameSessionUseCase(repository),
            saveGameSessionUseCase = SaveGameSessionUseCase(repository),
            getPendingMainGameSnapshotUseCase = GetPendingMainGameSnapshotUseCase(repository),
            clearGameSessionUseCase = ClearGameSessionUseCase(repository),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            resultDelayMs = 0L,
            transitionMs = 0L,
            successHighlightMs = 0L,
            successResultDelayMs = 0L,
            postTransitionHoldMs = 0L,
            totalRounds = 1,
            targetCorrect = 1
        )

        viewModel.onEvent(HigherLowerGameUiEvent.StartGame)
        advanceUntilIdle()
        viewModel.onEvent(HigherLowerGameUiEvent.SelectChoice(HigherLowerChoice.HIGHER))
        advanceUntilIdle()
        viewModel.saveSession()

        val saved = repository.savedSession as SavedSession.Minigame
        val snapshot = saved.minigameSnapshot as MinigameSnapshot.HigherLower
        assertEquals(MinigameType.HIGHER_LOWER, saved.minigameType)
        assertTrue(snapshot.isComplete)
        assertEquals(0, repository.clearCalls)
    }

    @Test
    fun blackjackSavesCompletedSessionWhenAppSavesState() = runTest {
        val repository = FakeGameSessionRepository()
        repository.savePendingMainGameSnapshot(TEST_MAIN_GAME_SNAPSHOT)
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(diceRoller = BlackjackDiceRoller { 10 }),
            loadGameSessionUseCase = LoadGameSessionUseCase(repository),
            saveGameSessionUseCase = SaveGameSessionUseCase(repository),
            getPendingMainGameSnapshotUseCase = GetPendingMainGameSnapshotUseCase(repository),
            clearGameSessionUseCase = ClearGameSessionUseCase(repository),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            dealerStandTotal = 0,
            initialPlayerDice = 1,
            initialDealerDice = 1,
            rewardRevealDelayMs = 0L
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()
        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        advanceUntilIdle()
        viewModel.saveSession()

        val saved = repository.savedSession as SavedSession.Minigame
        val snapshot = saved.minigameSnapshot as MinigameSnapshot.Blackjack
        assertEquals(MinigameType.BLACKJACK, saved.minigameType)
        assertTrue(snapshot.isComplete)
        assertEquals(0, repository.clearCalls)
    }
}


private val TEST_MAIN_GAME_SNAPSHOT = MainGameSnapshot(
    uiSnapshot = GameUiSnapshot(
        diceValues = listOf(1, 2, 3),
        diceCount = 3,
        diceType = DiceType.D6,
        diceTypes = listOf(
            DiceType.D6,
            DiceType.D6,
            DiceType.D6
        ),
        layoutSeed = 1L,
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
        showLevelCompleteMessage = false,
        minigamesAvailable = 0,
        minigamesPlayedSinceInterstitial = 0
    ),
    baseSeed = 1L,
    currentObjective = null,
    initialRollSnapshot = GameRollSnapshot(
        diceValues = listOf(1, 2, 3),
        diceTypes = listOf(
            DiceType.D6,
            DiceType.D6,
            DiceType.D6
        ),
        layoutSeed = 1L
    )
)

private class FakeGameSessionRepository : GameSessionRepository {
    var clearCalls = 0
    var savedSession: SavedSession? = null
    private var pendingMainGameSnapshot: MainGameSnapshot? = null

    override fun saveSession(session: SavedSession) {
        savedSession = session
    }

    override fun loadSession(): SavedSession? = savedSession

    override fun clearSession() {
        clearCalls += 1
        savedSession = null
        pendingMainGameSnapshot = null
    }

    override fun hasSession(): Boolean = savedSession != null

    override fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot) {
        pendingMainGameSnapshot = snapshot
    }

    override fun getPendingMainGameSnapshot(): MainGameSnapshot? = pendingMainGameSnapshot
}
