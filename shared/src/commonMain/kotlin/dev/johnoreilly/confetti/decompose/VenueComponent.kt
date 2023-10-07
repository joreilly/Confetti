package dev.johnoreilly.confetti.decompose

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetVenueQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface VenueComponent {

    val uiState: Value<UiState>

    fun refresh()

    sealed interface UiState
    data object Loading : UiState
    data object Error : UiState
    class Success(val data: GetVenueQuery.Venue
    ) : UiState
}

class DefaultVenueComponent(
    componentContext: ComponentContext,
    private val conference: String
) : VenueComponent, KoinComponent, ComponentContext by componentContext {
    private val coroutineScope = coroutineScope()
    val repository: ConfettiRepository = get()

    private var job: Job? = null

    private val channel = Channel<VenueComponent.UiState>()

    init {
        refresh(true)
    }

    override val uiState: Value<VenueComponent.UiState> = flow {
        for (uiState in channel) {
            emit(uiState)
        }
    }.asValue(initialValue = VenueComponent.Loading, lifecycle = lifecycle)

    override fun refresh() = refresh(false)

    private fun refresh(initial: Boolean) {
        job?.cancel()
        job = coroutineScope.launch {
            if (initial) {
                repository.conferenceVenue(conference, FetchPolicy.CacheFirst).data?.let {
                    it.venues.firstOrNull()?.let { venue ->
                        channel.send(VenueComponent.Success(venue))
                    }
                }
            }
            repository.conferenceVenue(conference, FetchPolicy.CacheFirst).data?.let {
                it.venues.firstOrNull()?.let { venue ->
                    channel.send(VenueComponent.Success(venue))
                }
            }
        }
    }
}