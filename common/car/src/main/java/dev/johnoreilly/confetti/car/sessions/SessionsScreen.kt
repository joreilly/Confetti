package dev.johnoreilly.confetti.car.sessions

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.decompose.DefaultSessionsComponent
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.decompose.SessionsUiState
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.car.sessions.details.SessionDetailsScreen
import dev.johnoreilly.confetti.car.ui.ErrorScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import dev.johnoreilly.confetti.fragment.SessionDetails
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SessionsScreen(
    carContext: CarContext,
    private val conference: String,
) : Screen(carContext), KoinComponent {

    private val authentication: Authentication by inject()

    private val component =
        DefaultSessionsComponent(
            componentContext = defaultComponentContext(),
            conference = conference,
            user = authentication.currentUser.value,
            onSessionSelected = { id ->
                screenManager.push(
                    SessionDetailsScreen(
                        carContext = carContext,
                        conference = conference,
                        user = authentication.currentUser.value,
                        sessionId = id,
                    )
                )
            },
            onSignIn = {},
        )

    init {
        component.uiState.subscribe { invalidate() }
    }

    override fun onGetTemplate(): Template {
        val result = component.uiState.value

        var listBuilder = ListTemplate.Builder()

        val loading = when (result) {
            SessionsUiState.Loading -> {
                true
            }

            SessionsUiState.Error -> {
                return ErrorScreen(carContext, R.string.auto_sessions_failed).onGetTemplate()
            }

            is SessionsUiState.Success -> {
                listBuilder = createSessionsList(result.sessionsByStartTimeList)
                false
            }
        }

        return listBuilder.apply {
            setTitle(carContext.getString(R.string.schedule))
            setHeaderAction(Action.BACK)
            setLoading(loading)
        }.build()
    }

    private fun createSessionsList(sessions: List<Map<String, List<SessionDetails>>>): ListTemplate.Builder {
        var listTemplate = ListTemplate.Builder()
        for (session in sessions) {
            listTemplate = createDailyList(listTemplate, session)
        }

        return listTemplate
    }

    private fun createDailyList(
        listTemplate: ListTemplate.Builder,
        sessions: Map<String, List<SessionDetails>>
    ): ListTemplate.Builder {
        sessions.forEach { (startTime, sessions) ->
            val listBuilder = ItemList.Builder()

            sessions.forEach { session ->
                val speakers = session.speakers.map { it.speakerDetails.name }

                listBuilder.addItem(
                    Row.Builder().apply {
                        setTitle(session.title)

                        if (speakers.isNotEmpty()) {
                            addText(speakers.toString())
                        }

                        setOnClickListener {
                            component.onSessionClicked(id = session.id)
                        }
                    }.build()
                )
            }

            listTemplate.addSectionedList(
                SectionedItemList.create(
                    listBuilder.build(),
                    startTime
                )
            )
        }

        return listTemplate
    }
}