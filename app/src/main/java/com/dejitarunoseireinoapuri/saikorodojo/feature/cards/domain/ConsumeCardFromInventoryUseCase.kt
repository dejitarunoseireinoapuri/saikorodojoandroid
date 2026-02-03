package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class ConsumeCardFromInventoryUseCase(
    private val repository: CardInventoryRepository
) {
    fun execute(cardId: CardId) {
        repository.consumeCard(cardId)
    }
}
