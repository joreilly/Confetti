package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.Flow
import java.util.*


class ConfettiViewModel(private val repository: ConfettiRepository) : ViewModel() {
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
        // TODO need to figure out how we want to generally handle languages
        return when (language?.lowercase(Locale.ROOT)) {
            "english" -> "\uD83C\uDDEC\uD83C\uDDE7"
            "french" -> "\uD83C\uDDEB\uD83C\uDDF7"
            else -> ""
        }
    }

    fun fetchMoreSessions() {
        repository.fetchMoreSessions()
    }
}
