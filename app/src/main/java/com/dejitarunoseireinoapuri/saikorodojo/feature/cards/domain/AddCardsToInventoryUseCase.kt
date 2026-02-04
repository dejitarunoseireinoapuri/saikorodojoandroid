package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class AddCardsToInventoryUseCase(
    private val repository: CardInventoryRepository
) {
    fun execute(cardIds: List<CardId>) {
        repository.addCards(cardIds)
    }
}
