package dev.johnoreilly.confetti.functions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionElementNotFoundException
import androidx.appfunctions.AppFunctionPermissionRequiredException
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.auth.User
import dev.johnoreilly.confetti.fragment.SessionDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDateTime

/**
 * AppFunctions for the Confetti conference app. KSP generates the concrete
 * [ConfettiAppFunctionService] registered in the manifest.
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "ConfettiAppFunctionService",
    appFunctionXmlFileName = "confetti_app_function_service",
)
abstract class BaseConfettiAppFunctionService : AppFunctionService(), KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val appSettings: AppSettings by inject()
    private val authentication: Authentication by inject()

    /**
     * Bookmarks a session in the currently selected conference. Requires the user to be
     * signed in to Confetti.
     *
     * @param sessionId ID of the session to bookmark, as returned in [SessionResponse.id].
     * @return true if the bookmark was saved.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun addBookmark(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        val conference = requireConference()
        val user = requireSignedInUser()
        repository.addBookmark(conference, user?.uid, user, sessionId)
    }

    /**
     * Removes the bookmark from a session in the currently selected conference. Requires the
     * user to be signed in to Confetti.
     *
     * @param sessionId ID of the session to un-bookmark, as returned in [SessionResponse.id].
     * @return true if the bookmark was removed.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun removeBookmark(sessionId: String): Boolean = withContext(Dispatchers.IO) {
        val conference = requireConference()
        val user = requireSignedInUser()
        repository.removeBookmark(conference, user?.uid, user, sessionId)
    }

    /**
     * Lists every session in the currently selected conference.
     *
     * @return All sessions with their ID, title, description and start/end times.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listSessions(): List<SessionResponse> = withContext(Dispatchers.IO) {
        val conference = requireConference()
        val user = authentication.currentUser.value
        val response = repository.sessions(conference, user?.uid, user, FetchPolicy.CacheFirst)
        response.data?.sessions?.nodes?.map { it.sessionDetails.toResponse() } ?: emptyList()
    }

    /**
     * Gets the ID of the conference currently selected in the app.
     *
     * @return The conference ID.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getConference(): String = withContext(Dispatchers.IO) {
        requireConference()
    }

    private suspend fun requireConference(): String =
        appSettings.getConference().ifEmpty {
            throw AppFunctionElementNotFoundException(
                "No conference is selected. Open Confetti and choose a conference first."
            )
        }

    // Bookmarks are stored per signed-in user; like the app UI, refuse rather than report a
    // bookmark that was never saved.
    private fun requireSignedInUser(): User =
        authentication.currentUser.value ?: throw AppFunctionPermissionRequiredException(
            "Bookmarks need a Confetti account. Open Confetti and sign in first."
        )

    private fun SessionDetails.toResponse() = SessionResponse(
        id = id,
        title = title,
        description = sessionDescription,
        startsAt = startsAt.toJavaLocalDateTime(),
        endsAt = endsAt.toJavaLocalDateTime(),
    )
}

/** A session (talk or workshop) in a conference. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SessionResponse(
    /** Unique ID of the session, used to add or remove a bookmark. */
    val id: String,
    /** Title of the session. */
    val title: String,
    /** Abstract describing the session content, if provided. */
    val description: String?,
    /** Start time in the conference's local time zone. */
    val startsAt: LocalDateTime,
    /** End time in the conference's local time zone. */
    val endsAt: LocalDateTime,
)
