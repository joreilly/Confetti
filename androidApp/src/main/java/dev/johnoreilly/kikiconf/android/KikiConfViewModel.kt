package dev.johnoreilly.kikiconf.android

import androidx.lifecycle.ViewModel
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.model.Room
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker


class KikiConfViewModel(private val repository: KikiConfRepository): ViewModel() {

    suspend fun getSessions(): List<Session> {
        return repository.getSessions()
    }

    suspend fun getSpeakers(): List<Speaker> {
        return repository.getSpeakers()
    }

    suspend fun getRooms(): List<Room> {
        return repository.getRooms()
    }

    suspend fun getSession(sessionId: String): Session? {
        return repository.getSession(sessionId)
    }

}