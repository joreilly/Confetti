package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat


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

    fun getSessionTime(session: SessionDetails): String {
        // TODO cleaner way of doing this?
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ")
        val date = df.parse(session.startDate)

        val timeFormatter = SimpleDateFormat("HH:mm")
        return timeFormatter.format(date)
    }

    fun getSessionSpeakerLocation(session: SessionDetails): String {
        var text = session.speakers.joinToString(", ") { it.name }
        text += " / ${session.room.name} / ${getLanguageInEmoji(session.language)}"
        return text
    }

    fun getLanguageInEmoji(language: String?): String {
        return when (language?.toLowerCase()) {
            "english" -> "\uD83C\uDDEC\uD83C\uDDE7"
            "french" -> "\uD83C\uDDEB\uD83C\uDDF7"
            else -> "\uD83C\uDDEC\uD83C\uDDE7 \uD83C\uDDEB\uD83C\uDDF7"
        }
    }
}