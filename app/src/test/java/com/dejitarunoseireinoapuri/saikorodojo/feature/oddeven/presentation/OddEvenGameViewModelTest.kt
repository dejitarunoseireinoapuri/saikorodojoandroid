package com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.presentation

import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.RollOddEvenUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OddEvenGameViewModelTest {
    @Test
    fun endsGameAfterLossDelayWhenWinIsImpossible() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val losingRoller = DiceRoller { 1 }
        val viewModel = OddEvenGameViewModel(
            rollOddEvenUseCase = RollOddEvenUseCase(losingRoller),
            dispatcher = testDispatcher,
            rollAnimationMs = 0L,
            resultAnimationMs = 0L,
            tickMs = 1L,
            lossMessageDelayMs = 1_000L,
            totalRounds = 3,
            targetCorrect = 3
        )

        viewModel.onEvent(OddEvenGameUiEvent.StartGame)
        viewModel.onEvent(OddEvenGameUiEvent.SelectChoice(OddEvenChoice.EVEN))
        runCurrent()

        assertFalse(viewModel.uiState.value.isComplete)

        advanceTimeBy(999L)
        runCurrent()
        assertFalse(viewModel.uiState.value.isComplete)

        advanceTimeBy(1L)
        runCurrent()
        assertTrue(viewModel.uiState.value.isComplete)
    }

    @Test
    fun clearSessionDelegatesToRepository() = runTest {
        val repository = TestGameSessionRepository()
        val viewModel = OddEvenGameViewModel(
            clearGameSessionUseCase = ClearGameSessionUseCase(repository)
        )

        viewModel.clearSession()

        assertTrue(repository.clearCalls > 0)
    }

    private class TestGameSessionRepository : GameSessionRepository {
        var clearCalls = 0

        override fun saveSession(session: SavedSession) = Unit

        override fun loadSession(): SavedSession? = null

        override fun clearSession() {
            clearCalls += 1
        }

        override fun hasSession(): Boolean = false

        override fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot) = Unit

        override fun getPendingMainGameSnapshot(): MainGameSnapshot? = null
    }
}
