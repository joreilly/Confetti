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
import dev.johnoreilly.confetti.SessionsViewModelParams
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

//class SessionsScreen(
//    carContext: CarContext,
//    private val conference: String,
//) : Screen(carContext), KoinComponent {
//
//    private val authentication: Authentication by inject()
//
//    private val sessionsViewModel: SessionsViewModel by inject(
//        parameters = { parametersOf(SessionsViewModelParams(conference, null, null)) }
//    )
//
//    private var uiState: StateFlow<SessionsUiState> = sessionsViewModel.uiState.onEach {
//        invalidate()
//    }.stateIn(lifecycleScope, started = SharingStarted.Eagerly, initialValue = SessionsUiState.Loading)
//
//    override fun onGetTemplate(): Template {
//        val result = uiState.value
//
//        var listBuilder = ListTemplate.Builder()
//
//        var venueLat: Double? = null
//        var venueLon: Double? = null
//        val loading = when(result) {
//            SessionsUiState.Loading -> {
//                true
//            }
//            SessionsUiState.Error -> {
//                return ErrorScreen(carContext, R.string.auto_sessions_failed).onGetTemplate()
//            }
//            is SessionsUiState.Success -> {
//                venueLat = result.venueLat
//                venueLon = result.venueLon
//                listBuilder = createSessionsList(result.sessionsByStartTimeList)
//                false
//            }
//        }
//
//        return listBuilder.apply {
//            setTitle(carContext.getString(R.string.schedule))
//            setHeaderAction(Action.BACK)
//            setLoading(loading)
//            setActionStrip(
//                ActionStrip.Builder()
//                    .addAction(
//                        Action.Builder()
//                            .setTitle(carContext.getString(R.string.auto_more))
//                            .setOnClickListener {
//                                screenManager.push(
//                                    MoreScreen(
//                                        carContext,
//                                        conference,
//                                        authentication.currentUser.value,
//                                        venueLat,
//                                        venueLon
//                                    )
//                                )
//                            }
//                            .build())
//                    .build())
//        }.build()
//    }
//
//    private fun createSessionsList(sessions: List<Map<String, List<SessionDetails>>>): ListTemplate.Builder {
//        var listTemplate = ListTemplate.Builder()
//        for (session in sessions) {
//            listTemplate = createDailyList(listTemplate, session)
//        }
//
//        return listTemplate
//    }
//
//    private fun createDailyList(listTemplate: ListTemplate.Builder, sessions: Map<String, List<SessionDetails>>): ListTemplate.Builder {
//        sessions.forEach { (startTime, sessions) ->
//            val listBuilder = ItemList.Builder()
//
//            sessions.forEach { session ->
//                listBuilder.addItem(
//                    Row.Builder()
//                        .setTitle(session.title)
//                        .addText(session.speakers.map { it.speakerDetails.name }.toString())
//                        .setOnClickListener { screenManager.push(
//                            SessionDetailsScreen(
//                                carContext,
//                                conference,
//                                authentication.currentUser.value,
//                                session
//                            )
//                        ) }
//                        .build()
//                )
//            }
//
//            listTemplate.addSectionedList(
//                SectionedItemList.create(
//                    listBuilder.build(),
//                    startTime
//                )
//            )
//        }
//
//        return listTemplate
//    }
//}