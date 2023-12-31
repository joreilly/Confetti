package dev.johnoreilly.confetti.decompose

import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import dev.johnoreilly.confetti.ConfettiRepository
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
    class Success(val data: Venue) : UiState
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
                        val data = Venue(
                            id = venue.id,
                            name = venue.name,
                            address = venue.address,
                            description = venue.description,
                            latitude = venue.latitude,
                            longitude = venue.longitude,
                            imageUrl = venue.imageUrl,
                            floorPlanUrl = venue.floorPlanUrl,
                            mapLink = buildMapLink(venue.address)
                        )
                        channel.send(VenueComponent.Success(data))
                    }
                }
            }
        }
    }

    private fun buildMapLink(address: String?): String? {
        var query: String? = null
        if (address != null) {
            query = address.replace(",", "%2C").replace(" ", "+")
        }
        if (query.isNullOrEmpty()) return null

        return "https://www.google.com/maps/search/?api=1&query=$query"
    }
}

data class Venue(
    val id: String,
    val name: String,
    val address: String?,
    val description: String,
    val latitude: Double?,
    val longitude: Double?,
    val imageUrl: String?,
    val floorPlanUrl: String?,
    val mapLink: String?,
)