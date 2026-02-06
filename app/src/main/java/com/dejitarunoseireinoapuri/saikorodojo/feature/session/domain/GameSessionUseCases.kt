package com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain

class SaveGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    fun execute(session: SavedSession) {
        repository.saveSession(session)
    }
}

class LoadGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    fun execute(): SavedSession? {
        return repository.loadSession()
    }
}

class ClearGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    fun execute() {
        repository.clearSession()
    }
}

class HasSavedGameSessionUseCase(
    private val repository: GameSessionRepository
) {
    fun execute(): Boolean {
        return repository.hasSession()
    }
}

class SavePendingMainGameSnapshotUseCase(
    private val repository: GameSessionRepository
) {
    fun execute(snapshot: MainGameSnapshot) {
        repository.savePendingMainGameSnapshot(snapshot)
    }
}

class GetPendingMainGameSnapshotUseCase(
    private val repository: GameSessionRepository
) {
    fun execute(): MainGameSnapshot? {
        return repository.getPendingMainGameSnapshot()
    }
}
