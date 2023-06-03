package dev.johnoreilly.confetti.car.speakers

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
import dev.johnoreilly.confetti.DefaultSpeakersComponent
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.SpeakersUiState
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.car.speakers.details.SpeakerDetailsScreen
import dev.johnoreilly.confetti.car.ui.ErrorScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import dev.johnoreilly.confetti.car.utils.fetchImage
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class SpeakersScreen(
    carContext: CarContext,
    private val conference: String,
    user: User?,
) : Screen(carContext) {

    private val component =
        DefaultSpeakersComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            onSpeakerSelected = { id ->
                screenManager.push(
                    SpeakerDetailsScreen(
                        carContext = carContext,
                        conference = conference,
                        user = user,
                        speakerId = id,
                    )
                )
            },
        )

    private var images: Map<String, CarIcon> = emptyMap()

    private val fallbackImage = CarIcon.Builder(
        IconCompat.createWithResource(carContext, R.drawable.ic_filled_person)
    ).build()

    init {
        component.uiState.subscribe { state ->
            invalidate()

            lifecycleScope.launch {
                fetchImages(state).collect { newImages ->
                    images = newImages
                    invalidate()
                }
            }
        }
    }

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
        val state = component.uiState.value

        if (state is SpeakersUiState.Error) {
            return ErrorScreen(carContext, R.string.auto_speakers_failed).onGetTemplate()
        }

        return GridTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.speakers))
            setHeaderAction(Action.BACK)

            if (state == SpeakersUiState.Loading) {
                setLoading(true)
            }

            if (state is SpeakersUiState.Success) {
                setSingleList(createSpeakersList(state, images))
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
                            component.onSpeakerClicked(id = speaker.id)
                        }
                        .setImage(image)
                        .build()
                )
            }
        }.build()
    }
}
