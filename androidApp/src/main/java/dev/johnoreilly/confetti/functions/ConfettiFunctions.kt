package dev.johnoreilly.confetti.functions

import androidx.appfunctions.service.AppFunction
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.fragment.SessionDetails
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * AppFunctions for Confetti conference app.
 * Provides capabilities to list sessions and manage bookmarks.
 */
class ConfettiFunctions : KoinComponent {
    private val repository: ConfettiRepository by inject()
    private val appSettings: AppSettings by inject()
    private val authentication: Authentication by inject()

    /**
     * Adds a bookmark to a session.
     *
     * @param sessionId The unique identifier of the session to bookmark.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun addBookmark(
        context: AppFunctionContext,
        sessionId: String
    ): Boolean {
        val conference = appSettings.getConference()
        val user = authentication.currentUser.value
        return repository.addBookmark(conference, user?.uid, user, sessionId)
    }

    /**
     * Removes a bookmark from a session.
     *
     * @param sessionId The unique identifier of the session to remove the bookmark from.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun removeBookmark(
        context: AppFunctionContext,
        sessionId: String
    ): Boolean {
        val conference = appSettings.getConference()
        val user = authentication.currentUser.value
        return repository.removeBookmark(conference, user?.uid, user, sessionId)
    }

    /**
     * Lists all sessions for the currently selected conference.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun listSessions(
        context: AppFunctionContext
    ): List<SessionResponse> {
        val conference = appSettings.getConference()
        val user = authentication.currentUser.value
        val response = repository.sessions(conference, user?.uid, user, FetchPolicy.CacheFirst)
        return response.data?.sessions?.nodes?.map {
            it.sessionDetails.toResponse()
        } ?: emptyList()
    }

    /**
     * Gets the name of the currently selected conference.
     */
    @AppFunction(isDescribedByKDoc = true)
    suspend fun getConference(
        context: AppFunctionContext
    ): String {
        return appSettings.getConference()
    }

    private fun SessionDetails.toResponse(): SessionResponse {
        return SessionResponse(
            id = id,
            title = title,
            description = sessionDescription,
            startsAt = startsAt.toString(),
            endsAt = endsAt.toString()
        )
    }
}

/**
 * Represents a session in the conference.
 */
@AppFunctionSerializable
data class SessionResponse(
    /**
     * The unique identifier of the session.
     */
    val id: String,
    /**
     * The title of the session.
     */
    val title: String,
    /**
     * A brief description of the session content.
     */
    val description: String?,
    /**
     * The start time of the session in ISO-8601 format.
     */
    val startsAt: String,
    /**
     * The end time of the session in ISO-8601 format.
     */
    val endsAt: String
)
