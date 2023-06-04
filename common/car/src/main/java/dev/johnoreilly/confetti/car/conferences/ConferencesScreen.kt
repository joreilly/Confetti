package dev.johnoreilly.confetti.car.conferences

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
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.car.ui.ErrorScreen
import dev.johnoreilly.confetti.car.ui.MoreScreen
import dev.johnoreilly.confetti.car.utils.defaultComponentContext
import org.koin.core.component.KoinComponent

class ConferencesScreen(
    carContext: CarContext,
) : Screen(carContext), KoinComponent {

    private val component: ConferencesComponent =
        DefaultConferencesComponent(
            componentContext = defaultComponentContext(),
            onConferenceSelected = { conference -> screenManager.push(MoreScreen(carContext, conference)) },
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
                        component.onConferenceClicked(conference = conference)
                    }
                    .build()
            )
        }

        return listBuilder
    }
}