package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.dejitarunoseireinoapuri.saikorodojo.R

class CardItemTest {
    @Test
    fun `default card models contains all card titles`() {
        val models = defaultCardUiModels()

        val titles = models.map { it.titleRes }

        assertEquals(6, models.size)
        assertTrue(titles.contains(R.string.card_adjust_plus_minus_one_title))
        assertTrue(titles.contains(R.string.card_flip_face_title))
        assertTrue(titles.contains(R.string.card_reroll_single_title))
        assertTrue(titles.contains(R.string.card_reroll_all_except_one_title))
        assertTrue(titles.contains(R.string.card_set_value_title))
        assertTrue(titles.contains(R.string.card_repeat_last_title))
    }

    @Test
    fun `card ui model uses apply as default action label`() {
        val model = CardUiModel(
            titleRes = R.string.card_adjust_plus_minus_one_title,
            descriptionRes = R.string.card_adjust_plus_minus_one_description,
            icon = defaultCardUiModels().first().icon
        )

        assertEquals(R.string.apply, model.actionLabelRes)
    }
}
