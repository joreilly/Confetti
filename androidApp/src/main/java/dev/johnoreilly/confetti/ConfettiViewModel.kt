package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*


class ConfettiViewModel(private val repository: ConfettiRepository): ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    val sessions = repository.sessions
    val speakers = repository.speakers
    val rooms = repository.rooms

    suspend fun getSession(sessionId: String): SessionDetails? {
        return repository.getSession(sessionId)
    }

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.updateEnableLanguageSetting(language, checked)
    }

    fun getSessionSpeakerLocation(session: SessionDetails): String {
        var text = session.speakers.joinToString(", ") { it.name }
        text += " / ${session.room.name} / ${getLanguageInEmoji(session.language)}"
        return text
    }

    fun getSessionTime(session: SessionDetails): String {
        return repository.getSessionTime(session)
    }

    fun getLanguageInEmoji(language: String?): String {
        return when (language?.toLowerCase()) {
            "en-us" -> "\uD83C\uDDEC\uD83C\uDDE7"
            "fr-fr" -> "\uD83C\uDDEB\uD83C\uDDF7"
            "english" -> "\uD83C\uDDEC\uD83C\uDDE7"
            "french" -> "\uD83C\uDDEB\uD83C\uDDF7"
            else -> "\uD83C\uDDEC\uD83C\uDDE7 \uD83C\uDDEB\uD83C\uDDF7"
        }
    }
}