package dev.johnoreilly.confetti.car.sessions.details

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.LongMessageTemplate
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.DefaultSessionDetailsComponent
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.SessionDetailsUiState
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.car.speakers.details.SpeakerDetailsScreen
import dev.johnoreilly.confetti.car.ui.ErrorScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class SessionDetailsScreen(
    carContext: CarContext,
    conference: String,
    user: User?,
    sessionId: String,
) : Screen(carContext), KoinComponent {

    private val component =
        DefaultSessionDetailsComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            user = user,
            sessionId = sessionId,
            onFinished = { screenManager.pop() },
            onSignIn = { /* Unused */ },
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

    init {
        component.uiState.subscribe { invalidate() }
    }

    override fun onGetTemplate(): Template {
        var isBookmarked: Boolean? = null
        lifecycleScope.launch {
            component.isBookmarked.collect {
                isBookmarked = it
                invalidate()
            }
        }

        return when (val state = component.uiState.value) {
            is SessionDetailsUiState.Loading ->
                PaneTemplate.Builder(Pane.Builder().setLoading(true).build()).build()

            is SessionDetailsUiState.Error ->
                ErrorScreen(carContext, R.string.auto_session_failed).onGetTemplate()

            is SessionDetailsUiState.Success -> {
                val session = state.sessionDetails
                LongMessageTemplate.Builder(session.sessionDescription ?: carContext.getString(R.string.auto_no_information)).apply {
                    setTitle(session.title)
                    setHeaderAction(Action.BACK)

                    if (isBookmarked == true) {
                        addAction(getRemoveFromBookmarksAction().build())
                    } else {
                        setActionStrip(
                            ActionStrip.Builder()
                                .addAction(getAddToBookmarksAction().build())
                                .build()
                        )
                    }
                }.build()
            }
        }
    }

    private fun getAddToBookmarksAction(): Action.Builder {
        return Action.Builder()
            .setOnClickListener(
                ParkedOnlyOnClickListener.create {
                    component.addBookmark()
                    CarToast.makeText(
                        carContext,
                        carContext.getString(R.string.auto_session_bookmark_success),
                        CarToast.LENGTH_SHORT
                    ).show()

                    invalidate()
                })
            .setTitle(carContext.getString(R.string.auto_session_bookmark))
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, R.drawable.ic_outlined_bookmarks)
                ).build()
            )
    }

    private fun getRemoveFromBookmarksAction(): Action.Builder {
        return Action.Builder()
            .setOnClickListener(
                ParkedOnlyOnClickListener.create {
                    component.removeBookmark()
                    CarToast.makeText(
                        carContext,
                        carContext.getString(R.string.auto_session_remove_bookmark_success),
                        CarToast.LENGTH_SHORT
                    ).show()

                    invalidate()
                })
            .setTitle(carContext.getString(R.string.auto_session_remove_bookmark))
    }
}