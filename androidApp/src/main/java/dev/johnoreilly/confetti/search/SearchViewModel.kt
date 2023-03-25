package dev.johnoreilly.confetti.search

import androidx.lifecycle.ViewModel
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.MutableStateFlow

class SearchViewModel : ViewModel() {

    val search = MutableStateFlow("")
    val sessions = MutableStateFlow(emptyList<SessionDetails>())
    val speakers = MutableStateFlow(emptyList<SpeakerDetails>())
}
