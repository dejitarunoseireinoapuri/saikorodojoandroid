package com.dejitarunoseireinoapuri.saikorodojo.feature.session.data

import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.GameSessionRepository
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.MainGameSnapshot
import com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain.SavedSession

class InMemoryGameSessionRepository : GameSessionRepository {
    private var savedSession: SavedSession? = null
    private var pendingMainGameSnapshot: MainGameSnapshot? = null

    override fun saveSession(session: SavedSession) {
        savedSession = session
    }

    override fun loadSession(): SavedSession? {
        return savedSession
    }

    override fun clearSession() {
        savedSession = null
        pendingMainGameSnapshot = null
    }

    override fun clearSavedSession() {
        savedSession = null
    }

    override fun hasSession(): Boolean {
        return savedSession != null
    }

    override fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot) {
        pendingMainGameSnapshot = snapshot
    }

    override fun getPendingMainGameSnapshot(): MainGameSnapshot? {
        return pendingMainGameSnapshot
    }

    companion object {
        val shared = InMemoryGameSessionRepository()
    }
}
