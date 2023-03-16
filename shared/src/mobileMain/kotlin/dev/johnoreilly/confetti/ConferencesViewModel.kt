package dev.johnoreilly.confetti

import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.SharingStarted
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class ConferencesViewModel : KMMViewModel(), KoinComponent {
    val repository: ConfettiRepository = get()

    @NativeCoroutinesState
    val conferenceList = repository
        .conferenceList
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            emptyList()
        )
}