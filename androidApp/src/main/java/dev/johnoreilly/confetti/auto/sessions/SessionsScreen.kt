package dev.johnoreilly.confetti.auto.sessions

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.SectionedItemList
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SessionsUiState
import dev.johnoreilly.confetti.SessionsViewModel
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.auto.sessions.details.SessionDetailsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.auto.ui.MoreScreen
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class SessionsScreen(
    carContext: CarContext,
    private val conference: String,
) : Screen(carContext) {

    private var user: User? = null
    private val authentication: Authentication by KoinJavaComponent.inject(Authentication::class.java)

    private val sessionsViewModel: SessionsViewModel by KoinJavaComponent.inject(SessionsViewModel::class.java)
    private var uiState: SessionsUiState = SessionsUiState.Loading

    init {
        sessionsViewModel.configure(conference, null, null)
    }

    override fun onGetTemplate(): Template {
        lifecycleScope.launch {
            authentication.currentUser.collect {
                user = it
                invalidate()
            }
        }

        lifecycleScope.launch {
            sessionsViewModel.uiState.collect {
                uiState = it
                invalidate()
            }
        }

        var listBuilder = ListTemplate.Builder()

        var venueLat: Double? = null
        var venueLon: Double? = null
        val loading = when(val result = uiState) {
            SessionsUiState.Loading -> {
                true
            }
            SessionsUiState.Error -> {
                return ErrorScreen(carContext, R.string.auto_sessions_failed).onGetTemplate()
            }
            is SessionsUiState.Success -> {
                venueLat = result.venueLat
                venueLon = result.venueLon
                listBuilder = createSessionsList(result.sessionsByStartTimeList)
                false
            }
        }

        return listBuilder.apply {
            setTitle(carContext.getString(R.string.schedule))
            setHeaderAction(Action.BACK)
            setLoading(loading)
            setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        Action.Builder()
                            .setTitle(carContext.getString(R.string.auto_more))
                            .setOnClickListener {
                                screenManager.push(
                                    MoreScreen(
                                        carContext,
                                        conference,
                                        user,
                                        venueLat,
                                        venueLon
                                    )
                                )
                            }
                            .build())
                    .build())
        }.build()
    }

    private fun createSessionsList(sessions: List<Map<String, List<SessionDetails>>>): ListTemplate.Builder {
        var listTemplate = ListTemplate.Builder()
        for (session in sessions) {
            listTemplate = createDailyList(listTemplate, session)
        }

        return listTemplate
    }

    private fun createDailyList(listTemplate: ListTemplate.Builder, sessions: Map<String, List<SessionDetails>>): ListTemplate.Builder {
        sessions.forEach { (startTime, sessions) ->
            val listBuilder = ItemList.Builder()

            sessions.forEach { session ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(session.title)
                        .addText(session.speakers.map { it.speakerDetails.name }.toString())
                        .setOnClickListener { screenManager.push(
                            SessionDetailsScreen(
                                carContext,
                                conference,
                                user,
                                session
                            )
                        ) }
                        .build()
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