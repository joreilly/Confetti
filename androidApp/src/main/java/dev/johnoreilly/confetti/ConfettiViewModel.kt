package dev.johnoreilly.confetti

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.*


class ConfettiViewModel(private val repository: ConfettiRepository): ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    val sessions = repository.sessions
    val speakers = repository.speakers
    val rooms = repository.rooms

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.updateEnableLanguageSetting(language, checked)
    }

    fun getSessionTime(session: SessionDetails): String {
        return repository.getSessionTime(session)
    }
}