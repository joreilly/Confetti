package dev.johnoreilly.confetti.auto.conferences

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.ConferencesViewModel
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.sessions.SessionsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConferencesScreen(
    carContext: CarContext,
) : Screen(carContext), KoinComponent {

    private val conferenceViewModel: ConferencesViewModel by inject()
    private var uiStateFlow: StateFlow<ConferencesViewModel.UiState> = conferenceViewModel.uiState.onEach {
        invalidate()
    }.stateIn(lifecycleScope, started = SharingStarted.Eagerly, initialValue = ConferencesViewModel.Loading)

    override fun onGetTemplate(): Template {
        val result = uiStateFlow.value

        var listBuilder = ItemList.Builder()
        val loading = when(result) {
            ConferencesViewModel.Loading -> {
                true
            }
            ConferencesViewModel.Error -> {
                return ErrorScreen(carContext, R.string.auto_conferences_failed).onGetTemplate()
            }
            is ConferencesViewModel.Success -> {
                listBuilder = createConferencesList(result.conferences)
                false
            }
        }

        val items = listBuilder.build()
        return ListTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.app_name))
            setHeaderAction(Action.APP_ICON)
            setLoading(loading)
            if (!loading) {
                setSingleList(items)
            }
        }.build()
    }

    private fun createConferencesList(conferences: List<GetConferencesQuery.Conference>): ItemList.Builder {
        val listBuilder = ItemList.Builder()
        for (conference in conferences) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(conference.name)
                    .setOnClickListener {
                        screenManager.push(SessionsScreen(carContext, conference.id))
                    }
                    .build()
            )
        }

        return listBuilder
    }
}