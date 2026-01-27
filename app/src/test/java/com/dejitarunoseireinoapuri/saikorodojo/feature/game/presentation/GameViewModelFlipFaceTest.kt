package com.dejitarunoseireinoapuri.saikorodojo.feature.game.presentation

import com.dejitarunoseireinoapuri.saikorodojo.MainDispatcherRule
import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation.CardUiModel
import com.dejitarunoseireinoapuri.saikorodojo.feature.game.domain.DiceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelFlipFaceTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `flipping a die replaces it with its opposite value`() {
        val viewModel = GameViewModel(
            dispatcher = mainDispatcherRule.dispatcher,
            diceCount = 1,
            diceType = DiceType.D6,
            diceTypeProvider = { _, count -> List(count) { DiceType.D6 } },
            cardUiModels = listOf(flipFaceCard(count = 2))
        )

        viewModel.onEvent(GameUiEvent.ApplyCard(0))

        val afterApply = viewModel.uiState.value
        assertTrue(afterApply.isAwaitingFlipFace)
        assertEquals(1, afterApply.cardUiModels.single().count)

        viewModel.onEvent(GameUiEvent.DiceClicked(0))

        val afterFlip = viewModel.uiState.value
        assertEquals(6, afterFlip.diceValues.first())
        assertTrue(!afterFlip.isAwaitingFlipFace)
    }

    private fun flipFaceCard(count: Int = 1): CardUiModel {
        return CardUiModel(
            id = CardId.FLIP_FACE,
            titleRes = R.string.card_flip_face_title,
            descriptionRes = R.string.card_flip_face_description,
            iconRes = R.drawable.ic_card_flip,
            count = count
        )
    }
}
