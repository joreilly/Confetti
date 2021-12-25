package dev.johnoreilly.kikiconf.android

import androidx.lifecycle.ViewModel
import dev.johnoreilly.kikiconf.KikiConfRepository
import dev.johnoreilly.kikiconf.fragment.SessionDetails
import kotlinx.coroutines.flow.*


class KikiConfViewModel(private val repository: KikiConfRepository): ViewModel() {
    val enabledLanguages: Flow<Set<String>> = repository.enabledLanguages

    val sessions = repository.sessions
    val speakers = repository.speakers
    val rooms = repository.rooms

    suspend fun getSession(sessionId: String): SessionDetails? {
        return repository.getSession(sessionId)
    }

    fun onLanguageChecked(language: String, checked: Boolean) {
        repository.onLanguageChecked(language, checked)
    }
}