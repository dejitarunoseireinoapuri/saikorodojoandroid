package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class GetCardInventoryUseCase(
    private val repository: CardInventoryRepository
) {
    fun execute(): Map<CardId, Int> {
        return repository.getCounts()
    }
}
