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
        .onStart {
            delay(1000)
        }
        .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        null
    )
}
