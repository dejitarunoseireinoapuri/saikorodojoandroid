package com.dejitarunoseireinoapuri.saikorodojo.feature.session.data

import android.content.Context
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import java.io.File

object GameSessionRepositoryProvider {
    private const val STORE_FILE_NAME = "game_session.json"

    @Volatile
    private var repository: GameSessionRepository = InMemoryGameSessionRepository.shared

    fun provide(): GameSessionRepository = repository

    fun initialize(context: Context) {
        repository = FileGameSessionRepository(
            file = File(context.filesDir, STORE_FILE_NAME)
        )
    }
}
