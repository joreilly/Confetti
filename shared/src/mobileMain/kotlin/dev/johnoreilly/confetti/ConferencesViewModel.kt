package dev.johnoreilly.confetti

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConferencesComponent.Error
import dev.johnoreilly.confetti.ConferencesComponent.Loading
import dev.johnoreilly.confetti.ConferencesComponent.Success
import dev.johnoreilly.confetti.ConferencesComponent.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConferencesComponent {

    val uiState: Value<UiState>

    fun refresh()
    fun onConferenceClicked(conference: String)

    sealed interface UiState
    object Loading : UiState
    object Error : UiState
    class Success(val conferences: List<GetConferencesQuery.Conference>) : UiState
}

class DefaultConferencesComponent(
    componentContext: ComponentContext,
    private val onConferenceSelected: (conference: String) -> Unit,
) : ConferencesComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()
    val repository: ConfettiRepository = get()

    private var job: Job? = null

    private val channel = Channel<UiState>()

    init {
        refresh(true)
    }

    override val uiState: Value<UiState> = flow {
        for (uiState in channel) {
            emit(uiState)
        }
    }.asValue(initialValue = Loading, lifecycle = lifecycle)

    override fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
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

    override fun onConferenceClicked(conference: String) {
        onConferenceSelected(conference)
    }
}