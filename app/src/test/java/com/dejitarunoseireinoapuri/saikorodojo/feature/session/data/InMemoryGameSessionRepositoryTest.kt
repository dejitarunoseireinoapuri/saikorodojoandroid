package com.dejitarunoseireinoapuri.saikorodojo.feature.session.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.LevelObjective
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameRollSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameUiSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InMemoryGameSessionRepositoryTest {
    @Test
    fun `stores and clears sessions`() {
        val repository = InMemoryGameSessionRepository()
        val snapshot = MainGameSnapshot(
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
                showLevelCompleteMessage = false,
                minigamesAvailable = 3,
                minigamesPlayedSinceInterstitial = 0
            ),
            baseSeed = 7L,
            currentObjective = LevelObjective(emptyList()),
            initialRollSnapshot = GameRollSnapshot(
                diceValues = listOf(1, 1, 1),
                diceTypes = listOf(DiceType.D6, DiceType.D6, DiceType.D6),
                layoutSeed = 3L
            )
        )

        repository.saveSession(SavedSession.MainGame(snapshot))

        assertEquals(true, repository.hasSession())
        assertEquals(snapshot, (repository.loadSession() as SavedSession.MainGame).snapshot)

        repository.clearSession()

        assertEquals(false, repository.hasSession())
        assertNull(repository.loadSession())
    }

    @Test
    fun `stores pending main game snapshot`() {
        val repository = InMemoryGameSessionRepository()
        val snapshot = MainGameSnapshot(
            uiSnapshot = GameUiSnapshot(
                diceValues = listOf(4, 4),
                diceCount = 2,
                diceType = DiceType.D6,
                diceTypes = listOf(DiceType.D6, DiceType.D6),
                layoutSeed = 9L,
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
                levelNumber = 2,
                isLevelComplete = false,
                showLevelCompleteMessage = false,
                minigamesAvailable = 1,
                minigamesPlayedSinceInterstitial = 2
            ),
            baseSeed = 11L,
            currentObjective = LevelObjective(emptyList()),
            initialRollSnapshot = null
        )

        repository.savePendingMainGameSnapshot(snapshot)

        assertEquals(snapshot, repository.getPendingMainGameSnapshot())
    }
}
