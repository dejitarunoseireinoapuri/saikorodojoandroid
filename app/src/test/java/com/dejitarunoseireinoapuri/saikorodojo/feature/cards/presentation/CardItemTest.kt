package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.dejitarunoseireinoapuri.saikorodojo.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    @Test
    fun `default card size is taller than wide`() {
        assertEquals(200.dp, DefaultCardSize.width)
        assertEquals(280.dp, DefaultCardSize.height)
        assertTrue(DefaultCardSize.height > DefaultCardSize.width)
    }

    @Test
    fun `title font sizes are ordered from largest to smallest`() {
        val sizes = titleFontSizes()

        assertEquals(listOf(18.sp, 17.sp, 16.sp, 15.sp, 14.sp, 13.sp, 12.sp), sizes)
    }
}
