package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class ConferencesViewModel : KMMViewModel(), KoinComponent {
    val repository: ConfettiRepository = get()

    sealed interface UiState

    object Loading : UiState
    object Error : UiState
    class Success(val conferences: List<GetConferencesQuery.Conference>) : UiState

    var job: Job? = null
    init {
        refresh(true)
    }

    @NativeCoroutinesState
    val uiStates: MutableStateFlow<UiState> = MutableStateFlow(Loading)

    fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = viewModelScope.coroutineScope.launch {
            var hasConferences = false
            if (initial) {
                repository.conferenceList(FetchPolicy.CacheFirst).data?.conferences?.let {
                    hasConferences = true
                    uiStates.emit(Success(it))
                }
            }
            repository.conferenceList(FetchPolicy.NetworkOnly).data?.conferences?.let {
                hasConferences = true
                uiStates.emit(Success(it))
            }

            if (!hasConferences) {
                uiStates.emit(Error)
            }
        }
    }
}