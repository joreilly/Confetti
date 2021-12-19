package dev.johnoreilly.kikiconf.android

import androidx.lifecycle.ViewModel
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.model.Room
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker
import kotlinx.coroutines.flow.StateFlow


class KikiConfViewModel(private val repository: KikiConfRepository): ViewModel() {
    val sessions: StateFlow<List<Session>> = repository.sessions
    val speakers: StateFlow<List<Speaker>> = repository.speakers
    val rooms: StateFlow<List<Room>> = repository.rooms

    suspend fun getSession(sessionId: String): Session? {
        return repository.getSession(sessionId)
    }
}