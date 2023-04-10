package dev.johnoreilly.confetti.work

import dev.johnoreilly.confetti.ConfettiRepository
import kotlinx.coroutines.flow.Flow

open class ConferenceSetting(
    val repository: ConfettiRepository
) {
    open fun selectedConference(): Flow<String> {
        return repository.getConferenceFlow()
    }
}