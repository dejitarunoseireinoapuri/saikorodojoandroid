package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

interface CardInventoryRepository {
    fun getCounts(): Map<CardId, Int>
    fun addCards(cardIds: List<CardId>)
    fun consumeCard(cardId: CardId)
    fun setCounts(counts: Map<CardId, Int>)
}
