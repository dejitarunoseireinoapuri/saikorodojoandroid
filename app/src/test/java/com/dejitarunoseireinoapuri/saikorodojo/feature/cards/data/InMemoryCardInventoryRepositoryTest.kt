package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryCardInventoryRepositoryTest {
    @Test
    fun `add and consume cards updates counts`() {
        val repository = InMemoryCardInventoryRepository()

        repository.addCards(listOf(CardId.REROLL_ALL, CardId.REROLL_ALL, CardId.MINIGAMES))

        assertEquals(2, repository.getCounts()[CardId.REROLL_ALL])
        assertEquals(1, repository.getCounts()[CardId.MINIGAMES])

        repository.consumeCard(CardId.REROLL_ALL)
        repository.consumeCard(CardId.MINIGAMES)

        assertEquals(1, repository.getCounts()[CardId.REROLL_ALL])
        assertEquals(null, repository.getCounts()[CardId.MINIGAMES])
    }

    @Test
    fun `set counts replaces inventory`() {
        val repository = InMemoryCardInventoryRepository()

        repository.addCards(listOf(CardId.REROLL_ALL, CardId.MINIGAMES))
        repository.setCounts(mapOf(CardId.SET_VALUE to 2))

        assertEquals(2, repository.getCounts()[CardId.SET_VALUE])
        assertEquals(null, repository.getCounts()[CardId.MINIGAMES])
        assertEquals(null, repository.getCounts()[CardId.REROLL_ALL])
    }
}
