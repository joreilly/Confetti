package dev.johnoreilly.confetti.car.speakers.details

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.decompose.DefaultSpeakerDetailsComponent
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.decompose.SpeakerDetailsUiState
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.car.sessions.details.SessionDetailsScreen
import dev.johnoreilly.confetti.car.ui.ErrorScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import dev.johnoreilly.confetti.car.utils.fetchImage
import dev.johnoreilly.confetti.fragment.SpeakerDetails
import kotlinx.coroutines.launch

class SpeakerDetailsScreen(
    carContext: CarContext,
    conference: String,
    user: User?,
    speakerId: String,
) : Screen(carContext) {

    private val component =
        DefaultSpeakerDetailsComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            speakerId = speakerId,
            onSessionSelected = { id ->
                screenManager.push(
                    SessionDetailsScreen(
                        carContext = carContext,
                        conference = conference,
                        user = user,
                        sessionId = id,
                    )
                )
            },
            onFinished = { screenManager.pop() },
        )

    private var image = CarIcon.Builder(IconCompat.createWithResource(carContext, R.drawable.ic_filled_person)).build()

    init {
        component.uiState.subscribe {
            invalidate()
            fetchImage()
        }
    }

    private fun fetchImage() {
        val state = component.uiState.value as? SpeakerDetailsUiState.Success ?: return
        val photoUrl = state.details.photoUrl ?: return

        lifecycleScope.launch {
            val bitmap = fetchImage(carContext, photoUrl)
            if (bitmap != null) {
                image = CarIcon.Builder(IconCompat.createWithBitmap(bitmap)).build()
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template =
        when (val state = component.uiState.value) {
            is SpeakerDetailsUiState.Loading ->
                PaneTemplate.Builder(Pane.Builder().setLoading(true).build()).build()

            is SpeakerDetailsUiState.Error ->
                ErrorScreen(carContext, R.string.auto_speaker_failed).onGetTemplate()

            is SpeakerDetailsUiState.Success -> {
                val speaker = state.details
                val paneBuilder = Pane.Builder()
                paneBuilder.addRow(
                    Row.Builder().apply {
                        setTitle(carContext.getString(R.string.auto_speakers_sessions))
                        for (session in speaker.sessions) {
                            addText(session.title)
                        }
                    }.build()
                ).setImage(image)

                paneBuilder.addAction(createAction(speaker).build())

                PaneTemplate.Builder(paneBuilder.build())
                    .setHeaderAction(Action.BACK)
                    .setTitle(speaker.name)
                    .build()
            }
        }

    private fun createAction(speaker: SpeakerDetails): Action.Builder {
        return Action.Builder()
            .setTitle(carContext.getString(R.string.auto_speakers_biography))
            .setOnClickListener {
                screenManager.push(SpeakerBiographyScreen(carContext, speaker))
            }
    }
}