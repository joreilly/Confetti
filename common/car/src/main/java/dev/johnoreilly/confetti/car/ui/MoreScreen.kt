package dev.johnoreilly.confetti.car.ui

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import dev.johnoreilly.confetti.GetConferencesQuery
import dev.johnoreilly.confetti.car.R
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.car.bookmarks.BookmarksScreen
import dev.johnoreilly.confetti.car.search.SearchScreen
import dev.johnoreilly.confetti.car.sessions.SessionsScreen
import dev.johnoreilly.confetti.car.signin.SignInScreen
import dev.johnoreilly.confetti.car.speakers.SpeakersScreen
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class MoreScreen(
    carContext: CarContext,
    private val conference: GetConferencesQuery.Conference
) : Screen(carContext), KoinComponent {

    private val authentication: Authentication by inject()

    override fun onGetTemplate(): Template {
        val user = authentication.currentUser.value

        val listBuilder = ItemList.Builder()
        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.sessions))
                .setOnClickListener { screenManager.push(SessionsScreen(carContext, conference.id)) }
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.speakers))
                .setOnClickListener { screenManager.push(SpeakersScreen(carContext, conference.id, user)) }
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.bookmarks))
                .setOnClickListener { screenManager.push(
                    BookmarksScreen(
                        carContext,
                        user,
                        conference.id
                    )) }
                .build()
        )

        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.search))
                .setOnClickListener { screenManager.push(SearchScreen(carContext, conference.id, user)) }
                .build()
        )

        val isAuthenticated = user != null
        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(
                    if (isAuthenticated) {
                        R.string.auto_sign_out
                    } else {
                        R.string.auto_sign_in
                    }
                ))
                .setOnClickListener { screenManager.push(
                    SignInScreen(
                        carContext,
                        isAuthenticated
                    )
                ) }
                .build()
        )

        return ListTemplate.Builder().apply {
            setTitle(conference.name)
            setHeaderAction(Action.BACK)
            setLoading(false)
            setSingleList(listBuilder.build())
        }.build()
    }
}