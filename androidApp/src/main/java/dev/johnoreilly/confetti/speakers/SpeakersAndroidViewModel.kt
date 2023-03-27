package dev.johnoreilly.confetti.speakers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.compose.paging.Pager
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.GetSpeakersQuery
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.Flow

class SpeakersAndroidViewModel(private val repository: ConfettiRepository) : ViewModel() {
    private lateinit var conference: String

    fun configure(conference: String) {
        this.conference = conference
    }

    val speakers: Flow<PagingData<SpeakerDetails>> = createPager()
        .flow
        .cachedIn(viewModelScope)

    @OptIn(ApolloExperimental::class)
    private fun createPager(): Pager<ApolloCall<GetSpeakersQuery.Data>, SpeakerDetails> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            appendCall = { response, loadSize ->
                if (response != null && response.data?.speakers?.pageInfo?.endCursor == null) {
                    // Reached the end of the list
                    return@Pager null
                }
                repository.speakersCall(
                    conference = conference,
                    first = loadSize,
                    after = response?.data?.speakers?.pageInfo?.endCursor
                )
            },
            getItems = { response ->
                if (response.hasErrors()) {
                    Result.failure(Exception("Could not fetch page of speakers: ${response.errors!!.joinToString { it.message }}"))
                } else {
                    Result.success(response.data!!.speakers.nodes.map { it.speakerDetails })
                }
            },
        )

    }
}
