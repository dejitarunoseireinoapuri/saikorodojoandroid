package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class SetCardInventoryUseCase(
    private val repository: CardInventoryRepository
) {
    fun execute(counts: Map<CardId, Int>) {
        repository.setCounts(counts)
    }
}
