package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryCardInventoryRepositoryTest {
    @Test
    fun `add and consume cards updates counts`() {
        val repository = InMemoryCardInventoryRepository()

        repository.addCards(listOf(CardId.REROLL_ALL, CardId.REROLL_ALL, CardId.RETRY))

        assertEquals(2, repository.getCounts()[CardId.REROLL_ALL])
        assertEquals(1, repository.getCounts()[CardId.RETRY])

        repository.consumeCard(CardId.REROLL_ALL)
        repository.consumeCard(CardId.RETRY)

        assertEquals(1, repository.getCounts()[CardId.REROLL_ALL])
        assertEquals(null, repository.getCounts()[CardId.RETRY])
    }
}
