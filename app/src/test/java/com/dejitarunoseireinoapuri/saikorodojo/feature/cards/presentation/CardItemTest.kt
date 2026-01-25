package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CardItemTest {
    @Test
    fun `default card models contains all card titles`() {
        val models = defaultCardUiModels()

        val titles = models.map { it.title }

        assertEquals(6, models.size)
        assertTrue(titles.contains("Ajustar ±1"))
        assertTrue(titles.contains("Voltear cara"))
        assertTrue(titles.contains("Relanzar un dado"))
        assertTrue(titles.contains("Relanzar todos menos uno"))
        assertTrue(titles.contains("Fijar valor"))
        assertTrue(titles.contains("Repetir última carta"))
    }

    @Test
    fun `card ui model uses apply as default action label`() {
        val model = CardUiModel(
            title = "Ajustar ±1",
            description = "Aumenta o reduce en 1 el valor de un dado, sin salir del rango del dado",
            icon = defaultCardUiModels().first().icon
        )

        assertEquals("Aplicar", model.actionLabel)
    }
}
