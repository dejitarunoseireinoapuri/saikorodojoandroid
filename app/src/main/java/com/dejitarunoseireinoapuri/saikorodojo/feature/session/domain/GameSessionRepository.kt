package com.dejitarunoseireinoapuri.saikorodojo.feature.session.domain

interface GameSessionRepository {
    fun saveSession(session: SavedSession)
    fun loadSession(): SavedSession?
    fun clearSession()
    fun clearSavedSession()
    fun hasSession(): Boolean
    fun savePendingMainGameSnapshot(snapshot: MainGameSnapshot)
    fun getPendingMainGameSnapshot(): MainGameSnapshot?
}
