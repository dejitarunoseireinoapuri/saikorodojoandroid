package com.dejitarunoseireinoapuri.saikorodojo.feature.session.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.GenerateObjectiveUseCase
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.MinigameType
import com.dejitarunoseireinoapuri.saikorodojo.feature.oddeven.domain.OddEvenChoice
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MinigameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileGameSessionRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun savesAndLoadsMainGameSession() {
        val file = temporaryFolder.newFile("session.json")
        val repository = FileGameSessionRepository(file)
        val snapshot = buildMainGameSnapshot()

        repository.saveSession(SavedSession.MainGame(snapshot))

        val loaded = repository.loadSession() as SavedSession.MainGame
        assertEquals(snapshot.uiSnapshot.diceValues, loaded.snapshot.uiSnapshot.diceValues)
        assertEquals(snapshot.uiSnapshot.levelNumber, loaded.snapshot.uiSnapshot.levelNumber)
        assertEquals(snapshot.baseSeed, loaded.snapshot.baseSeed)
        assertNotNull(loaded.snapshot.currentObjective)
    }

    @Test
    fun savesAndLoadsMinigameSession() {
        val file = temporaryFolder.newFile("session_minigame.json")
        val repository = FileGameSessionRepository(file)
        val mainSnapshot = buildMainGameSnapshot()
        val minigameSnapshot = MinigameSnapshot.OddEven(
            isStarted = true,
            currentRound = 2,
            totalRounds = 3,
            correctCount = 1,
            wrongCount = 0,
            targetCorrect = 2,
            selectedChoice = OddEvenChoice.EVEN,
            diceValue = 4,
            isRolling = false,
            showFireworks = false,
            showFailure = false,
            isComplete = false,
            rewardCardIds = listOf(CardId.MINIGAMES)
        )

        repository.saveSession(
            SavedSession.Minigame(
                minigameType = MinigameType.ODD_EVEN,
                minigameSnapshot = minigameSnapshot,
                mainGameSnapshot = mainSnapshot
            )
        )

        val loaded = repository.loadSession() as SavedSession.Minigame
        assertEquals(MinigameType.ODD_EVEN, loaded.minigameType)
        val loadedSnapshot = loaded.minigameSnapshot as MinigameSnapshot.OddEven
        assertEquals(minigameSnapshot.currentRound, loadedSnapshot.currentRound)
        assertEquals(minigameSnapshot.selectedChoice, loadedSnapshot.selectedChoice)
    }

    @Test
    fun persistsPendingMainGameSnapshot() {
        val file = temporaryFolder.newFile("session_pending.json")
        val repository = FileGameSessionRepository(file)
        val snapshot = buildMainGameSnapshot()

        repository.savePendingMainGameSnapshot(snapshot)

        val loaded = repository.getPendingMainGameSnapshot()
        assertEquals(snapshot.uiSnapshot.levelNumber, loaded?.uiSnapshot?.levelNumber)
        assertEquals(snapshot.baseSeed, loaded?.baseSeed)
    }

    private fun buildMainGameSnapshot(): MainGameSnapshot {
        val uiSnapshot = GameUiSnapshot(
            diceValues = listOf(2, 5),
            diceCount = 2,
            diceType = DiceType.D6,
            diceTypes = listOf(DiceType.D6, DiceType.D8),
            layoutSeed = 123L,
            isRolling = false,
            isAwaitingRerollSingle = false,
            isAwaitingRerollSelected = false,
            isAwaitingFlipFace = false,
            isAwaitingAdjustPlusMinus = false,
            isAwaitingSetValue = false,
            selectedDice = setOf(0),
            selectedRerollDice = setOf(1),
            selectedRerollSingleDieIndex = 0,
            selectedFlipDieIndex = null,
            selectedAdjustmentDieIndex = null,
            selectedSetValueDieIndex = null,
            selectedDiceSum = 7,
            shouldShowSelectedSum = true,
            cardCounts = mapOf(CardId.REROLL_SINGLE to 1),
            selectedCardIndex = 0,
            lastAppliedCardId = CardId.REROLL_SINGLE,
            levelNumber = 2,
            isLevelComplete = false,
            showLevelCompleteMessage = false,
            minigamesAvailable = 1,
            minigamesPlayedSinceInterstitial = 1
        )
        val baseSeed = 52L
        return MainGameSnapshot(
            uiSnapshot = uiSnapshot,
            baseSeed = baseSeed,
            currentObjective = GenerateObjectiveUseCase().execute(
                levelNumber = uiSnapshot.levelNumber,
                diceTypes = uiSnapshot.diceTypes,
                seedBase = baseSeed
            ),
            initialRollSnapshot = GameRollSnapshot(
                diceValues = listOf(2, 5),
                diceTypes = listOf(DiceType.D6, DiceType.D8),
                layoutSeed = 9L
            )
        )
    }
}
