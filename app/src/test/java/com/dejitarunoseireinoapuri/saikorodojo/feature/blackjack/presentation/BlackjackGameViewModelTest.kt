package com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.BlackjackOutcome
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.CalculateBlackjackScoreUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DetermineBlackjackOutcomeUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.DiceRoller
import com.dejitarunoseireinoapuri.saikorodojo.feature.blackjack.domain.RollBlackjackDiceUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.RewardCardsRandomProvider
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.SelectMinigameRewardCardsUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.ClearGameSessionUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlackjackGameViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dealer wins when reaching a higher total`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(9, 7, 6, 10, 2)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
            ),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isComplete)
        assertEquals(BlackjackOutcome.PLAYER_LOSE, state.result)
    }

    @Test
    fun `blackjack win grants reward cards`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(10, 1, 8, 9)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
            ),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(BlackjackOutcome.PLAYER_WIN, state.result)
        assertTrue(state.rewardCards.isNotEmpty())
        assertEquals(
            listOf(CardId.ADJUST_PLUS_MINUS_ONE, CardId.FLIP_FACE),
            state.rewardCards.map { it.id }
        )
    }

    @Test
    fun `scores update after dice animation completes`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(1, 1, 1, 4, 6, 8)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
            ),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 4L,
            tickMs = 2L,
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        runCurrent()

        val midState = viewModel.uiState.value
        assertEquals(0, midState.playerTotal)
        assertEquals(0, midState.dealerTotal)

        advanceTimeBy(4L)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(10, finalState.playerTotal)
        assertEquals(8, finalState.dealerTotal)
    }

    @Test
    fun `player bust highlights loss before completing defeat state`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(10, 10, 5, 6)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
            ),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            rewardRevealDelayMs = 1_000L
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Hit)
        runCurrent()

        val immediateState = viewModel.uiState.value
        assertEquals(BlackjackOutcome.PLAYER_LOSE, immediateState.result)
        assertFalse(immediateState.isComplete)

        advanceTimeBy(1_000L)
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertEquals(BlackjackOutcome.PLAYER_LOSE, finalState.result)
        assertTrue(finalState.isComplete)
    }

    @Test
    fun `win outcome highlights player mat before revealing rewards`() = runTest {
        val viewModel = BlackjackGameViewModel(
            rollBlackjackDiceUseCase = RollBlackjackDiceUseCase(
                TestDiceRoller(ArrayDeque(listOf(10, 1, 8, 9)))
            ),
            calculateBlackjackScoreUseCase = CalculateBlackjackScoreUseCase(),
            determineBlackjackOutcomeUseCase = DetermineBlackjackOutcomeUseCase(),
            selectMinigameRewardCardsUseCase = SelectMinigameRewardCardsUseCase(
                TestRewardRandomProvider(listOf(0.4f, 0.2f, 0.3f))
            ),
            dispatcher = mainDispatcherRule.dispatcher,
            rollAnimationMs = 0L,
            tickMs = 1L,
            rewardRevealDelayMs = 1_000L
        )

        viewModel.onEvent(BlackjackGameUiEvent.StartGame)
        advanceUntilIdle()

        viewModel.onEvent(BlackjackGameUiEvent.Stand)
        runCurrent()

        val immediateState = viewModel.uiState.value
        assertEquals(BlackjackOutcome.PLAYER_WIN, immediateState.result)
        assertTrue(immediateState.rewardCards.isEmpty())
        assertFalse(immediateState.isComplete)

        advanceTimeBy(1_000L)
        advanceUntilIdle()

        val revealedState = viewModel.uiState.value
        assertTrue(revealedState.rewardCards.isNotEmpty())
        assertTrue(revealedState.isComplete)
    }

    @Test
    fun `clear session delegates to repository`() = runTest {
        val repository = TestGameSessionRepository()
        val viewModel = BlackjackGameViewModel(
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

private class TestDiceRoller(
    private val values: ArrayDeque<Int>
) : DiceRoller {
    override fun roll(range: IntRange): Int = values.removeFirst()
}

private class TestRewardRandomProvider(
    private val values: List<Float>
) : RewardCardsRandomProvider {
    private var index = 0

    override fun nextFloat(): Float {
        val value = values.getOrElse(index) { values.last() }
        index += 1
        return value
    }
}
