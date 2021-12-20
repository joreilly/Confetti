package dev.johnoreilly.kikiconf.android

import androidx.lifecycle.ViewModel
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.model.Session


class KikiConfViewModel(private val repository: KikiConfRepository): ViewModel() {
    // TODO - should we maps these to StateFlow (using stateIn())?
    val sessions = repository.sessions
    val speakers = repository.speakers
    val rooms = repository.rooms

    suspend fun getSession(sessionId: String): Session? {
        return repository.getSession(sessionId)
    }
}