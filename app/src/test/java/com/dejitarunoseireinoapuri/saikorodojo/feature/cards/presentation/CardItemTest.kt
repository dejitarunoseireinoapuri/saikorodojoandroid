package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import com.dejitarunoseireinoapuri.saikorodojo.R
import org.junit.Assert.assertEquals
import org.junit.Test

class CardItemTest {
    @Test
    fun `defaultCardUiModels uses drawable icons`() {
        val cards = defaultCardUiModels()

        assertEquals(6, cards.size)
        assertEquals(R.drawable.ic_card_adjust, cards[0].iconRes)
        assertEquals(R.drawable.ic_card_flip, cards[1].iconRes)
        assertEquals(R.drawable.ic_card_reroll_single, cards[2].iconRes)
        assertEquals(R.drawable.ic_card_reroll_all_except_one, cards[3].iconRes)
        assertEquals(R.drawable.ic_card_set_value, cards[4].iconRes)
        assertEquals(R.drawable.ic_card_repeat_last, cards[5].iconRes)
    }
}
