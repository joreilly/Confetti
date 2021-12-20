package dev.johnoreilly.kikiconf.android

import androidx.lifecycle.ViewModel
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.fragment.SessionDetails


class KikiConfViewModel(private val repository: KikiConfRepository): ViewModel() {
    // TODO - should we maps these to StateFlow (using stateIn())?
    val sessions = repository.sessions
    val speakers = repository.speakers
    val rooms = repository.rooms

    suspend fun getSession(sessionId: String): SessionDetails? {
        return repository.getSession(sessionId)
    }
}