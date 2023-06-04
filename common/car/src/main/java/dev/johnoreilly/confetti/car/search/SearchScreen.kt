package dev.johnoreilly.confetti.car.search

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.SearchTemplate.SearchCallback
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.DefaultSearchComponent
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.car.sessions.details.SessionDetailsScreen
import dev.johnoreilly.confetti.car.speakers.details.SpeakerDetailsScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import dev.johnoreilly.confetti.car.utils.formatDateTime
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent

class SearchScreen(
    carContext: CarContext,
    conference: String,
    user: User?,
) : Screen(carContext), KoinComponent {

    private val component =
        DefaultSearchComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            user = user,
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
            onSignIn = { /* Unused */ },
        )
    private val sessionsState = component.sessions.onEach {
        invalidate()
    }.stateIn(lifecycleScope, started = SharingStarted.Eagerly, initialValue = null)

    override fun onGetTemplate(): Template {
        val sessions = sessionsState.value

        val listBuilder = createSessionsList(sessions)

        return SearchTemplate.Builder(
            object : SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    super.onSearchTextChanged(searchText)
                    component.onSearchChange(searchText)
                }

                override fun onSearchSubmitted(searchText: String) {
                    super.onSearchSubmitted(searchText)
                    component.onSearchChange(searchText)
                }
            }
        ).apply {
            setHeaderAction(Action.BACK)
            setShowKeyboardByDefault(false)
            setItemList(listBuilder.build())
        }.build()
    }

    private fun createSessionsList(sessions: List<SessionDetails>?): ItemList.Builder {
        if (sessions == null) {
            return ItemList.Builder()
        }

        val listBuilder = ItemList.Builder()
        for (session in sessions) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(session.title)
                    .addText(
                        carContext.getString(
                            R.string.auto_session_text,
                            session.room?.name ?: carContext.getString(R.string.auto_placeholder),
                            formatDateTime(session.startsAt)
                        )
                    ).build()
            )
        }

        return listBuilder
    }
}