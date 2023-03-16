package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class AppViewModel : KMMViewModel(), KoinComponent {
    private val repository: ConfettiRepository = get()

    @NativeCoroutinesState
    val conference: StateFlow<String?> =repository.getConferenceFlow().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            null
        )

    suspend fun setConference(conference: String) {
        repository.setConference(conference)
    }
}
