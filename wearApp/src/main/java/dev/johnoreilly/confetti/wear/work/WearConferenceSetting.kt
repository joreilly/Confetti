package dev.johnoreilly.confetti.wear.work

import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.wear.settings.PhoneSettingsSync
import dev.johnoreilly.confetti.work.ConferenceSetting
import kotlinx.coroutines.flow.Flow

class WearConferenceSetting(
    val phoneSettingsSync: PhoneSettingsSync,
    repository: ConfettiRepository,
) : ConferenceSetting(repository) {
    override fun selectedConference(): Flow<String> {
        return phoneSettingsSync.conferenceFlow
    }
}