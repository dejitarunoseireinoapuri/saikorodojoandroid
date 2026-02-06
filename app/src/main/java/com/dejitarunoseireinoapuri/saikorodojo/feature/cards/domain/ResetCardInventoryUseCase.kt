package com.dejitarunoseireinoapuri.saikorodojo.feature.cards.domain

class ResetCardInventoryUseCase(
    private val repository: CardInventoryRepository
) {
    fun execute() {
        repository.setCounts(emptyMap())
    }
}
