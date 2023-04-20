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
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class ConferencesScreen(
    carContext: CarContext,
) : Screen(carContext) {

    private val conferenceViewModel: ConferencesViewModel by KoinJavaComponent.inject(ConferencesViewModel::class.java)
    private var uiState: ConferencesViewModel.UiState = ConferencesViewModel.Loading

    override fun onGetTemplate(): Template {
        lifecycleScope.launch {
            conferenceViewModel.uiState.collect {
                uiState = it
                invalidate()
            }
        }

        var listBuilder = ItemList.Builder()
        val loading = when(val result = uiState) {
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