package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import com.rickclephas.kmm.viewmodel.stateIn
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

open class ConferencesViewModel(
    private val onConferenceSet: suspend (String) -> Unit = {}
) : KMMViewModel(), KoinComponent {
    val repository: ConfettiRepository = get()

    sealed interface UiState

    object Loading : UiState
    object Error : UiState
    class Success(val conferences: List<GetConferencesQuery.Conference>) : UiState

    private var job: Job? = null

    private val channel = Channel<UiState>()

    init {
        refresh(true)
    }

    @NativeCoroutinesState
    val uiState: StateFlow<UiState> = flow {
        for (uiState in channel) {
            emit(uiState)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)


    fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = viewModelScope.coroutineScope.launch {
            var hasConferences = false
            if (initial) {
                repository.conferences(FetchPolicy.CacheFirst).data?.conferences?.let {
                    hasConferences = true
                    channel.send(Success(it))
                }
            }
            repository.conferences(FetchPolicy.NetworkOnly).data?.conferences?.let {
                hasConferences = true
                channel.send(Success(it))
            }

            if (!hasConferences) {
                channel.send(Error)
            }
        }
    }

    open suspend fun setConference(conference: String) {
        repository.setConference(conference)
        onConferenceSet(conference)
    }
}