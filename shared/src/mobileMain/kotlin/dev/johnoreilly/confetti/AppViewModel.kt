package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class AppViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository = get()

    @NativeCoroutinesState
    val conference: StateFlow<String?> = repository
        .getConferenceFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    // Kept for compatibility with iOS
    suspend fun setConference(conference: String) {
        repository.setConference(conference)
    }
}
