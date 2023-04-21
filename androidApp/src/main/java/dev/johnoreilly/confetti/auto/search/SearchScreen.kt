package dev.johnoreilly.confetti.auto.search

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.SearchTemplate.SearchCallback
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SearchViewModel
import dev.johnoreilly.confetti.auto.utils.formatDateTime
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class SearchScreen (
    carContext: CarContext,
    conference: String
) : Screen(carContext) {

    private val searchViewModel: SearchViewModel by KoinJavaComponent.inject(SearchViewModel::class.java)

    init {
        searchViewModel.configure(conference, null, null)
    }

    override fun onGetTemplate(): Template {
        var sessions: List<SessionDetails>? = null
        lifecycleScope.launch {
            searchViewModel.sessions.collect {
                sessions = it
                invalidate()
            }
        }

        val listBuilder = createSessionsList(sessions)

        return SearchTemplate.Builder(
            object : SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    super.onSearchTextChanged(searchText)
                    searchViewModel.onSearchChange(searchText)
                }

                override fun onSearchSubmitted(searchText: String) {
                    super.onSearchSubmitted(searchText)
                    searchViewModel.onSearchChange(searchText)
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