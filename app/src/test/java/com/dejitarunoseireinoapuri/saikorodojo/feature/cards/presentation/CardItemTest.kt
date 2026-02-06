package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import org.junit.Assert.assertEquals
import org.junit.Test

class CardItemTest {
    @Test
    fun `defaultCardUiModels uses drawable icons`() {
        val cards = defaultCardUiModels()

        assertEquals(7, cards.size)
        assertEquals(R.drawable.ic_card_adjust, cards[0].iconRes)
        assertEquals(CardId.ADJUST_PLUS_MINUS_ONE, cards[0].id)
        assertEquals(R.drawable.ic_card_flip, cards[1].iconRes)
        assertEquals(CardId.FLIP_FACE, cards[1].id)
        assertEquals(R.drawable.ic_card_reroll_single, cards[2].iconRes)
        assertEquals(CardId.REROLL_SINGLE, cards[2].id)
        assertEquals(R.drawable.ic_card_reroll_all, cards[3].iconRes)
        assertEquals(CardId.REROLL_ALL, cards[3].id)
        assertEquals(R.drawable.ic_card_set_value, cards[4].iconRes)
        assertEquals(CardId.SET_VALUE, cards[4].id)
        assertEquals(R.drawable.ic_card_repeat_last, cards[5].iconRes)
        assertEquals(CardId.REPEAT_LAST, cards[5].id)
        assertEquals(R.drawable.ic_card_retry, cards[6].iconRes)
        assertEquals(CardId.MINIGAMES, cards[6].id)
    }

    @Test
    fun `resolveCountLayout uses vertical layout for collapsed cards`() {
        val layout = resolveCountLayout(
            showTitle = false,
            showDescription = false,
            showActionButton = false
        )

        assertEquals(CountLayout.Vertical, layout)
    }

    @Test
    fun `resolveCountLayout uses horizontal layout for expanded cards`() {
        val layout = resolveCountLayout(
            showTitle = true,
            showDescription = false,
            showActionButton = false
        )

        assertEquals(CountLayout.Horizontal, layout)
    }
}
