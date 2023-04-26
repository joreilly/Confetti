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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SearchScreen (
    carContext: CarContext,
    conference: String
) : Screen(carContext), KoinComponent {

    private val searchViewModel: SearchViewModel by inject()

    private val sessionsState = searchViewModel.sessions.onEach {
        invalidate()
    }.stateIn(lifecycleScope, started = SharingStarted.Eagerly, initialValue = null)

    init {
        searchViewModel.configure(conference, null, null)
    }

    override fun onGetTemplate(): Template {
        val sessions = sessionsState.value

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