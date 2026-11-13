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
import dev.johnoreilly.confetti.GetConferencesQuery
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
        repository.addBookmark(conference, user.uid, user, sessionId)
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
        repository.removeBookmark(conference, user.uid, user, sessionId)
    }

    /**
     * Lists every session in the currently selected conference.
     *
     * @return All sessions with their ID, title, description, times, room, speakers and tags.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listSessions(): List<SessionResponse> = withContext(Dispatchers.IO) {
        val conference = requireConference()
        val user = authentication.currentUser.value
        val data = repository.sessions(conference, user?.uid, user, FetchPolicy.CacheFirst).data
        val timeZone = data?.config?.timezone
        data?.sessions?.nodes?.map { it.sessionDetails.toResponse(timeZone) } ?: emptyList()
    }

    /**
     * Lists the sessions the user has bookmarked in the currently selected conference.
     * Requires the user to be signed in to Confetti.
     *
     * @return The bookmarked sessions, in the same form as [listSessions].
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listBookmarks(): List<SessionResponse> = withContext(Dispatchers.IO) {
        val conference = requireConference()
        val user = requireSignedInUser()
        // Network first: bookmarks may have just been changed by addBookmark/removeBookmark
        // or on another device, so a cached list could be stale.
        val data = repository.bookmarkedSessionsQuery(conference, user.uid, user, FetchPolicy.NetworkFirst)
            .execute()
            .data
        val timeZone = data?.config?.timezone
        data?.bookmarkConnection?.nodes?.map { it.sessionDetails.toResponse(timeZone) } ?: emptyList()
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

    /**
     * Lists the speakers of the currently selected conference.
     *
     * @return All speakers with their ID, name, company, bio, links and the sessions they present.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listSpeakers(): List<SpeakerProfileResponse> = withContext(Dispatchers.IO) {
        val conference = requireConference()
        repository.conferenceData(conference, FetchPolicy.CacheFirst).data?.speakers?.nodes?.map {
            val speaker = it.speakerDetails
            SpeakerProfileResponse(
                id = speaker.id,
                name = speaker.name,
                company = speaker.company,
                tagline = speaker.tagline,
                city = speaker.city,
                bio = speaker.bio,
                sessionIds = speaker.sessions.map { session -> session.id },
                links = speaker.socials.map { social -> LinkResponse(name = social.name, url = social.url) },
            )
        } ?: emptyList()
    }

    /**
     * Gets the venue of the currently selected conference.
     *
     * @return The conference's venues (usually one), with address and coordinates where known.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getVenue(): List<VenueResponse> = withContext(Dispatchers.IO) {
        val conference = requireConference()
        repository.conferenceVenue(conference, FetchPolicy.CacheFirst).data?.venues?.map {
            VenueResponse(
                name = it.name,
                address = it.address,
                description = it.description.ifBlank { null },
                latitude = it.latitude,
                longitude = it.longitude,
            )
        } ?: emptyList()
    }

    /**
     * Lists every conference available in Confetti.
     *
     * @return All conferences with their ID, name, days and time zone.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listConferences(): List<ConferenceResponse> = withContext(Dispatchers.IO) {
        conferences().map { it.toResponse() }
    }

    /**
     * Selects the conference that the other functions act on, replacing the user's current
     * selection in the app. The app shows the new conference the next time it is opened.
     *
     * @param conferenceId ID of the conference to select, as returned in [ConferenceResponse.id].
     * @return The selected conference.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun selectConference(conferenceId: String): ConferenceResponse = withContext(Dispatchers.IO) {
        val conference = conferences().firstOrNull { it.id == conferenceId }
            ?: throw AppFunctionElementNotFoundException(
                "No conference with ID '$conferenceId'. Use listConferences to find valid IDs."
            )
        repository.setConference(conference.id, conference.themeColor)
        conference.toResponse()
    }

    private suspend fun conferences(): List<GetConferencesQuery.Conference> =
        repository.conferences(FetchPolicy.CacheFirst).data?.conferences.orEmpty()

    private suspend fun requireConference(): String =
        appSettings.getConference().ifEmpty {
            throw AppFunctionElementNotFoundException(
                "No conference is selected. Use selectConference, or open Confetti and choose a conference."
            )
        }

    // Bookmarks are stored per signed-in user; like the app UI, refuse rather than report a
    // bookmark that was never saved.
    private fun requireSignedInUser(): User =
        authentication.currentUser.value ?: throw AppFunctionPermissionRequiredException(
            "Bookmarks need a Confetti account. Open Confetti and sign in first."
        )

    private fun SessionDetails.toResponse(timeZone: String?) = SessionResponse(
        id = id,
        title = title,
        description = sessionDescription,
        startsAt = startsAt.toJavaLocalDateTime(),
        endsAt = endsAt.toJavaLocalDateTime(),
        timeZone = timeZone,
        type = type,
        room = room?.name,
        speakers = speakers.map {
            SpeakerResponse(
                id = it.sessionSpeakerDetails.id,
                name = it.sessionSpeakerDetails.name,
                company = it.sessionSpeakerDetails.company,
                tagline = it.sessionSpeakerDetails.tagline,
            )
        },
        tags = tags,
        language = language,
        recordingUrl = recordingUrl,
    )

    private fun GetConferencesQuery.Conference.toResponse() = ConferenceResponse(
        id = id,
        name = name,
        days = days.map { it.toString() },
        timeZone = timezone,
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
    /** Start time, local to the conference: interpret it in [timeZone]. */
    val startsAt: LocalDateTime,
    /** End time, local to the conference: interpret it in [timeZone]. */
    val endsAt: LocalDateTime,
    /** IANA time zone of the conference (for example "Europe/London"), if known. */
    val timeZone: String?,
    /** Kind of session, for example "talk", "workshop" or "keynote". */
    val type: String,
    /** Name of the room the session is in, if known. */
    val room: String?,
    /** People presenting the session. */
    val speakers: List<SpeakerResponse>,
    /** Topic tags, for example "Kotlin" or "AI/ML". */
    val tags: List<String>,
    /** Language the session is given in, if specified. */
    val language: String?,
    /** Link to a recording of the session, once one is published. */
    val recordingUrl: String?,
)

/** A person presenting a session. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SpeakerResponse(
    /** Unique ID of the speaker, matching [SpeakerProfileResponse.id]. */
    val id: String,
    /** Full name of the speaker. */
    val name: String,
    /** Company or organisation the speaker works for, if known. */
    val company: String?,
    /** Short description of the speaker, if provided. */
    val tagline: String?,
)

/** A conference venue. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class VenueResponse(
    /** Name of the venue. */
    val name: String,
    /** Street address of the venue, if known. */
    val address: String?,
    /** Description of the venue, if provided. */
    val description: String?,
    /** Latitude of the venue, if known. */
    val latitude: Double?,
    /** Longitude of the venue, if known. */
    val longitude: Double?,
)

/** A conference available in Confetti. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ConferenceResponse(
    /** Unique ID of the conference, used to select it. */
    val id: String,
    /** Name of the conference. */
    val name: String,
    /** Dates the conference runs on, as ISO-8601 dates (for example "2026-12-10"). */
    val days: List<String>,
    /** IANA time zone of the conference (for example "Europe/London"). */
    val timeZone: String,
)

/** A conference speaker. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SpeakerProfileResponse(
    /** Unique ID of the speaker. */
    val id: String,
    /** Full name of the speaker. */
    val name: String,
    /** Company or organisation the speaker works for, if known. */
    val company: String?,
    /** Short description of the speaker, if provided. */
    val tagline: String?,
    /** City the speaker is based in, if known. */
    val city: String?,
    /** Biography of the speaker, if provided. */
    val bio: String?,
    /** IDs of the sessions the speaker presents, matching [SessionResponse.id]. */
    val sessionIds: List<String>,
    /** The speaker's social and web links. */
    val links: List<LinkResponse>,
)

/** A named web link. */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class LinkResponse(
    /** Name of the link, for example "Bluesky" or "GitHub". */
    val name: String,
    /** URL of the link. */
    val url: String,
)
