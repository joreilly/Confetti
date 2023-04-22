@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.auto.speakers

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.GridItem
import androidx.car.app.model.GridTemplate
import androidx.car.app.model.ItemList
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SpeakersUiState
import dev.johnoreilly.confetti.SpeakersViewModel
import dev.johnoreilly.confetti.auto.speakers.details.SpeakerDetailsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.auto.utils.fetchImage
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class SpeakersScreen(
    carContext: CarContext,
    conference: String,
    speakersViewModel: SpeakersViewModel = SpeakersViewModel()
) : Screen(carContext) {

    val fallbackImage = CarIcon.Builder(
        IconCompat.createWithResource(carContext, R.drawable.ic_filled_person)
    ).build()

    init {
        speakersViewModel.configure(conference)
    }

    data class StateAndImages(
        val state: SpeakersUiState,
        val images: Map<String, CarIcon> = mapOf()
    )

    val speakersFlow = speakersViewModel.speakers
    val imagesFlow: Flow<Map<String, CarIcon>> = speakersViewModel.speakers.flatMapLatest {
        fetchImages(it)
    }

    private var uiState: StateFlow<StateAndImages> = combine(
        speakersFlow,
        imagesFlow
    ) { speakers, images ->
        StateAndImages(speakers, images)
    }.onEach {
        invalidate()
    }
        .stateIn(lifecycleScope, SharingStarted.Eagerly, StateAndImages(SpeakersUiState.Loading))

    private fun fetchImages(uiState: SpeakersUiState): Flow<Map<String, CarIcon>> {
        return if (uiState is SpeakersUiState.Success) {
            combine(uiState.speakers.map { imageFlow(it) }) {
                it.toMap()
            }
        } else {
            flowOf(mapOf())
        }
    }

    private fun imageFlow(speakerDetails: SpeakerDetails): Flow<Pair<String, CarIcon>> = flow {
            emit(speakerDetails.id to fallbackImage)

            val photoUrl = speakerDetails.photoUrl
            if (photoUrl != null) {
                val bitmap = fetchImage(carContext, photoUrl)

                if (bitmap != null) {
                    val icon = IconCompat.createWithBitmap(bitmap)
                    emit(speakerDetails.id to CarIcon.Builder(icon).build())
                }
            }
        }

    override fun onGetTemplate(): Template {
        val result = uiState.value

        if (result.state is SpeakersUiState.Error) {
            return ErrorScreen(carContext, R.string.auto_speakers_failed).onGetTemplate()
        }

        return GridTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.speakers))
            setHeaderAction(Action.BACK)

            if (result.state == SpeakersUiState.Loading) {
                setLoading(true)
            }

            if (result.state is SpeakersUiState.Success) {
                setSingleList(createSpeakersList(result.state, result.images))
            }
        }.build()
    }

    private fun createSpeakersList(
        state: SpeakersUiState.Success,
        images: Map<String, CarIcon>
    ): ItemList {
        return ItemList.Builder().apply {
            for (speaker in state.speakers) {
                val image = images.getOrDefault(speaker.id, fallbackImage)

                addItem(
                    GridItem.Builder()
                        .setTitle(speaker.name)
                        .setText(speaker.company ?: "")
                        .setOnClickListener {
                            screenManager.push(SpeakerDetailsScreen(carContext, speaker, image))
                        }
                        .setImage(image)
                        .build()
                )
            }
        }.build()
    }
}
