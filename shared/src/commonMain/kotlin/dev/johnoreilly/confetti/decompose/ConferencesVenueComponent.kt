package dev.johnoreilly.confetti.decompose

import com.apollographql.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.GetVenueQuery
import dev.johnoreilly.confetti.decompose.ConferencesVenueComponent.Error
import dev.johnoreilly.confetti.decompose.ConferencesVenueComponent.Loading
import dev.johnoreilly.confetti.decompose.ConferencesVenueComponent.Success
import dev.johnoreilly.confetti.decompose.ConferencesVenueComponent.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ConferencesVenueComponent {

    val uiState: Value<UiState>

    fun refresh()
    fun onConferenceClicked(id: String)

    sealed interface UiState
    object Loading : UiState
    object Error : UiState
    class Success(val data: Map<GetConferencesQuery.Conference, GetVenueQuery.Venue?>
    ) : UiState
}

class DefaultConferencesVenueComponent(
    componentContext: ComponentContext,
    private val onConferenceSelected: (id: String) -> Unit,
) : ConferencesVenueComponent, KoinComponent, ComponentContext by componentContext {
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
            val venues = mutableMapOf<GetConferencesQuery.Conference, GetVenueQuery.Venue?>()

            if (initial) {
                repository.conferences(FetchPolicy.CacheFirst).data?.let { data ->
                    hasConferences = true

                    for (conference in data.conferences) {
                        repository.conferenceVenue(conference.id, FetchPolicy.CacheFirst).data?.let {
                            venues[conference] = it.venues.firstOrNull()

                            if (venues.size == data.conferences.size) {
                                channel.send(Success(venues))
                            }

                            repository
                        }
                    }
                }
            }
            repository.conferences(FetchPolicy.NetworkOnly).data?.let { data ->
                hasConferences = true

                for (conference in data.conferences) {
                    repository.conferenceVenue(conference.id, FetchPolicy.CacheFirst).data?.let {
                        venues[conference] = it.venues.firstOrNull()

                        if (venues.size == data.conferences.size) {
                            channel.send(Success(venues))
                        }

                        repository
                    }
                }
            }

            if (!hasConferences) {
                channel.send(Error)
            }
        }
    }

    override fun onConferenceClicked(id: String) {
        onConferenceSelected(id)
    }
}