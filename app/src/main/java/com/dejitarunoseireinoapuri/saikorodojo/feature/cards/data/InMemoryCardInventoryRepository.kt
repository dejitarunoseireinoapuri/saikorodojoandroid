package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardId
import com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain.CardInventoryRepository

class InMemoryCardInventoryRepository(
    initialCounts: Map<CardId, Int> = emptyMap()
) : CardInventoryRepository {
    private val counts: MutableMap<CardId, Int> = initialCounts.toMutableMap()

    override fun getCounts(): Map<CardId, Int> {
        return counts.toMap()
    }

    override fun addCards(cardIds: List<CardId>) {
        cardIds.forEach { cardId ->
            counts[cardId] = (counts[cardId] ?: 0) + 1
        }
    }

    override fun consumeCard(cardId: CardId) {
        val current = counts[cardId] ?: return
        if (current <= 1) {
            counts.remove(cardId)
        } else {
            counts[cardId] = current - 1
        }
    }

    override fun setCounts(counts: Map<CardId, Int>) {
        this.counts.clear()
        this.counts.putAll(counts)
    }

    companion object {
        val shared = InMemoryCardInventoryRepository()
    }
}
