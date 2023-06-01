package dev.johnoreilly.confetti.auto.conferences

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.ConferencesComponent
import dev.johnoreilly.confetti.DefaultConferencesComponent
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.auto.sessions.SessionsScreen
import dev.johnoreilly.confetti.auto.ui.ErrorScreen
import dev.johnoreilly.confetti.auto.utils.defaultComponentContext
import org.koin.core.component.KoinComponent

class ConferencesScreen(
    carContext: CarContext,
) : Screen(carContext), KoinComponent {

    private val component: ConferencesComponent =
        DefaultConferencesComponent(
            componentContext = defaultComponentContext(),
            onConferenceSelected = { id -> screenManager.push(SessionsScreen(carContext, id)) },
        )

    init {
        component.uiState.subscribe { invalidate() }
    }

    override fun onGetTemplate(): Template {

        val result = component.uiState.value

        var listBuilder = ItemList.Builder()
        val loading = when (result) {
            ConferencesComponent.Loading -> {
                true
            }

            ConferencesComponent.Error -> {
                return ErrorScreen(carContext, R.string.auto_conferences_failed).onGetTemplate()
            }

            is ConferencesComponent.Success -> {
                listBuilder = createConferencesList(result.conferences)
                false
            }
        }

        val items = listBuilder.build()
        return ListTemplate.Builder().apply {
            setTitle(carContext.getString(R.string.app_name))
            setHeaderAction(Action.BACK)
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
                        component.onConferenceClicked(conference = conference.id)
                    }
                    .build()
            )
        }

        return listBuilder
    }
}