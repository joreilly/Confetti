package dev.johnoreilly.confetti.auto.sessions.details

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.car.app.model.LongMessageTemplate
import androidx.car.app.model.ParkedOnlyOnClickListener
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.lifecycleScope
import dev.johnoreilly.confetti.BookmarksViewModel
import dev.johnoreilly.confetti.R
import dev.johnoreilly.confetti.SessionDetailsViewModel
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

class SessionDetailsScreen(
    carContext: CarContext,
    conference: String,
    user: User?,
    private val session: SessionDetails
) : Screen(carContext) {

    private val bookmarksViewModel: BookmarksViewModel by KoinJavaComponent.inject(BookmarksViewModel::class.java)
    private val sessionDetailsViewModel: SessionDetailsViewModel by KoinJavaComponent.inject(SessionDetailsViewModel::class.java)

    init {
        bookmarksViewModel.configure(conference, user?.uid, user)
        sessionDetailsViewModel.configure(conference, session.id, user?.uid, user)
    }

    override fun onGetTemplate(): Template {
        var isBookmarked: Boolean? = null
        lifecycleScope.launch {
            sessionDetailsViewModel.isBookmarked.collect {
                isBookmarked = it
                invalidate()
            }
        }

        return LongMessageTemplate.Builder(session.sessionDescription ?: "").apply {
            setTitle(session.title)
            setHeaderAction(Action.BACK)

            if (isBookmarked == true) {
                addAction(getRemoveFromBookmarksAction().build())
            } else {
                setActionStrip(ActionStrip.Builder()
                    .addAction(getAddToBookmarksAction().build())
                    .build()
                )
            }
        }.build()
    }

    private fun getAddToBookmarksAction(): Action.Builder {
        return Action.Builder()
            .setOnClickListener(
                ParkedOnlyOnClickListener.create {
                    bookmarksViewModel.addBookmark(session.id)
                    CarToast.makeText(
                        carContext,
                        carContext.getString(R.string.auto_session_bookmark_success),
                        CarToast.LENGTH_SHORT
                    ).show()

                    invalidate()
                })
            .setTitle(carContext.getString(R.string.auto_session_bookmark))
            .setIcon(
                CarIcon.Builder(
                    IconCompat.createWithResource(carContext, R.drawable.ic_outlined_bookmarks)
                ).build()
            )
    }

    private fun getRemoveFromBookmarksAction(): Action.Builder {
        return Action.Builder()
            .setOnClickListener(
                ParkedOnlyOnClickListener.create {
                    bookmarksViewModel.removeBookmark(session.id)
                    CarToast.makeText(
                        carContext,
                        carContext.getString(R.string.auto_session_remove_bookmark_success),
                        CarToast.LENGTH_SHORT
                    ).show()

                    invalidate()
                })
            .setTitle(carContext.getString(R.string.auto_session_remove_bookmark))
    }
}