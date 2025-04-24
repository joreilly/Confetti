package dev.johnoreilly.confetti.backend.graphql

import com.apollographql.apollo.api.ExecutionContext
import com.apollographql.apollo.ast.GQLStringValue
import com.apollographql.apollo.ast.GQLValue
import com.apollographql.apollo.execution.Coercing
import com.apollographql.apollo.execution.ExternalValue
import com.apollographql.execution.annotation.*
import com.google.firebase.auth.FirebaseAuth
import dev.johnoreilly.confetti.backend.*
import dev.johnoreilly.confetti.backend.datastore.DDirection
import dev.johnoreilly.confetti.backend.datastore.DOrderBy
import dev.johnoreilly.confetti.backend.datastore.DataStore
import kotlinx.datetime.Instant as KotlinxInstant
import kotlinx.datetime.LocalDate as KotlinxLocalDate
import kotlinx.datetime.LocalDateTime as KotlinxLocalDateTime

/**
 * A type representing a formatted kotlinx.datetime.Instant
 */
@GraphQLScalar(InstantCoercing::class)
typealias Instant = KotlinxInstant
/**
 * A type representing a formatted kotlinx.datetime.LocalDate
 */
@GraphQLScalar(LocalDateCoercing::class)
typealias LocalDate = KotlinxLocalDate
/**
 * A type representing a formatted kotlinx.datetime.LocalDateTime
 */
@GraphQLScalar(LocalDateTimeCoercing::class)
typealias LocalDateTime = KotlinxLocalDateTime

object InstantCoercing : Coercing<Instant> {
    override fun serialize(internalValue: Instant): ExternalValue {
        return internalValue.toString()
    }

    override fun deserialize(value: ExternalValue): Instant {
        return Instant.parse(value as String)
    }

    override fun parseLiteral(value: GQLValue): Instant {
        return Instant.parse((value as GQLStringValue).value)
    }
}

object LocalDateCoercing : Coercing<LocalDate> {
    override fun serialize(internalValue: LocalDate): ExternalValue {
        return internalValue.toString()
    }

    override fun deserialize(value: ExternalValue): LocalDate {
        return LocalDate.parse(value as String)
    }

    override fun parseLiteral(value: GQLValue): LocalDate {
        return LocalDate.parse((value as GQLStringValue).value)
    }
}

object LocalDateTimeCoercing : Coercing<LocalDateTime> {
    override fun serialize(internalValue: LocalDateTime): ExternalValue {
        return internalValue.toString()
    }

    override fun deserialize(value: ExternalValue): LocalDateTime {
        return LocalDateTime.parse((value as String))
    }

    override fun parseLiteral(value: GQLValue): LocalDateTime {
        return LocalDateTime.parse((value as GQLStringValue).value)
    }
}

@GraphQLMutation
@GraphQLName("Mutation")
class RootMutation {
    fun addBookmark(dfe: ExecutionContext, sessionId: String): Bookmarks {
        return Bookmarks(dfe.source().addBookmark(sessionId).toList())
    }

    fun removeBookmark(dfe: ExecutionContext, sessionId: String): Bookmarks {
        return Bookmarks(dfe.source().removeBookmark(sessionId).toList())
    }

    /**
     * Deletes the current user account, requires authentication
     */
    fun deleteAccount(dfe: ExecutionContext): Boolean {
        val uid = dfe.uid()
        if (uid == null) {
            return false
        }

        FirebaseAuth.getInstance().deleteUser(uid)
        dfe.source().deleteUserData()
        return true
    }
}

@GraphQLQuery
@GraphQLName("Query")
class RootQuery {
    fun rooms(dfe: ExecutionContext): List<Room> {
        return dfe.source().rooms()
    }

    fun sessions(
        dfe: ExecutionContext,
        @GraphQLDefault("10") first: Int?,
        @GraphQLDefault("null") after: String?,
        @GraphQLDefault("null") filter: SessionFilter?,
        @GraphQLDefault("{field: STARTS_AT, direction: ASCENDING}") orderBy: SessionOrderBy?
    ): SessionConnection {
        return dfe.source().sessions(
            first ?: 10,
            after,
            filter,
            orderBy
        )
    }

    @Deprecated("Use speakersPage instead")
    fun speakers(dfe: ExecutionContext): List<Speaker> {
        return dfe.source().speakers(first = 100, after = null).nodes
    }

    fun speakersPage(
        dfe: ExecutionContext,
        @GraphQLDefault("null") first: Int?,
        @GraphQLDefault("null") after: String?,
    ): SpeakerConnection {
        return dfe.source().speakers(first ?: 10, after)
    }

    fun speaker(dfe: ExecutionContext, id: String): Speaker {
        return dfe.source().speaker(id)
    }

    fun venue(dfe: ExecutionContext, id: String): Venue {
        return dfe.source().venues().first { it.id == id }
    }

    fun venues(dfe: ExecutionContext): List<Venue> {
        return dfe.source().venues()
    }

    fun partnerGroups(dfe: ExecutionContext): List<PartnerGroup> {
        return dfe.source().partnerGroups()
    }

    fun session(dfe: ExecutionContext, id: String): Session {
        return dfe.source().sessions(listOf(id)).single()
    }

    fun config(dfe: ExecutionContext): Conference {
        return dfe.source().conference()
    }

    @Deprecated("Use bookmarkConnection instead")
    fun bookmarks(dfe: ExecutionContext): Bookmarks? {
        if (dfe.uid() == null) {
            return null
        }

        dfe.disableCaching()
        return Bookmarks(dfe.source().bookmarks().toList())
    }

    fun bookmarkConnection(dfe: ExecutionContext): BookmarkConnection? {
        if (dfe.uid() == null) {
            return null
        }

        dfe.disableCaching()
        return BookmarkConnection(
            nodes = dfe.source().sessions(dfe.source().bookmarks().toList())
        )
    }

    fun conferences(@GraphQLDefault("null") orderBy: ConferenceOrderBy?): List<Conference> {
        val orderBy1 =
            orderBy ?: ConferenceOrderBy(ConferenceField.DAYS, OrderByDirection.DESCENDING)
        return DataStore().readConfigs(
            DOrderBy(orderBy1.field.value, orderBy1.direction.toDDirection())
        ).map {
            it.toConference()
        }
    }
}

private fun ExecutionContext.disableCaching() {
    get(MaxAgeContext)!!.maxAge = 0
}

class BookmarkConnection(
    val nodes: List<Session>
)

class Bookmarks(val sessionIds: List<String>) {
    val id = "Bookmarks"
}

internal fun OrderByDirection.toDDirection(): DDirection {
    return when (this) {
        OrderByDirection.ASCENDING -> DDirection.ASCENDING
        OrderByDirection.DESCENDING -> DDirection.DESCENDING
    }
}

@GraphQLName("LocalDateTimeFilterInput")
class LocalDateTimeFilter(
    @GraphQLDefault("null") val before: LocalDateTime?,
    @GraphQLDefault("null") val after: LocalDateTime?,
)

@GraphQLName("SessionFilterInput")
class SessionFilter(
    @GraphQLDefault("null") val startsAt: LocalDateTimeFilter?,
    @GraphQLDefault("null") val endsAt: LocalDateTimeFilter?,
)

@GraphQLName("SessionOrderByInput")
class SessionOrderBy(
    val field: SessionField,
    val direction: OrderByDirection
)

enum class SessionField(val value: String) {
    STARTS_AT("start"),
}

@GraphQLName("ConferenceOrderByInput")
class ConferenceOrderBy(
    val field: ConferenceField,
    val direction: OrderByDirection
)

enum class OrderByDirection {
    ASCENDING,
    DESCENDING
}

enum class ConferenceField(val value: String) {
    DAYS("days"),
}

fun ExecutionContext.uid(): String? {
    return get(UidContext)?.uid
}

private fun ExecutionContext.source(): DataSource {
    return get(SourceContext)?.source ?: error("No SourceContext")
}

data class Room(
    val id: String,
    val name: String,
    val capacity: Int?,
)

data class SessionConnection(
    val nodes: List<Session>,
    val pageInfo: PageInfo,
)


data class PageInfo(
    val endCursor: String?,
)

enum class LinkType {
    YouTube,
    Audio,
    AudioUncompressed,
    Other
}

data class Link(
    val type: LinkType,
    val url: String,
)

/**
 */
data class Session(
    val id: String,
    val title: String,
    val description: String?,
    /**
     * A shorter version of description for use when real estate is scarce like watches for an example.
     * This field might have the same value as description if a shortDescription is not available.
     */
    val shortDescription: String?,
    /**
     * An [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US.
     */
    val language: String?,
    private val speakerIds: Set<String>,
    val tags: List<String>,
    @Deprecated("use startsAt instead")
    val startInstant: Instant,
    @Deprecated("use endsAt instead")
    val endInstant: Instant,
    val startsAt: LocalDateTime,
    val endsAt: LocalDateTime,
    private val roomIds: Set<String>,
    val complexity: String?,
    val feedbackId: String?,
    /**
     * One of "break", "lunch", "party", "keynote", "talk" or any other conference-specific format.
     */
    val type: String,
    val links: List<Link>
) {
    fun speakers(dfe: ExecutionContext): List<Speaker> {
        return dfe.source().speakers(speakerIds.toList())
    }

    fun room(dfe: ExecutionContext): Room? {
        val roomId = roomIds.firstOrNull()
        if (roomId == null) {
            return null
        }
        return dfe.source().rooms().firstOrNull {
            it.id == roomId
        }
    }

    fun rooms(dfe: ExecutionContext): List<Room> {
        return dfe.source().rooms().filter {
            roomIds.contains(it.id)
        }
    }
}

data class SpeakerConnection(
    val nodes: List<Speaker>,
    val pageInfo: PageInfo,
)

data class Speaker(
    val id: String,
    val name: String,
    val bio: String?,
    val tagline: String?,
    val company: String?,
    val companyLogoUrl: String?,
    val city: String?,
    val socials: List<Social>,
    val photoUrl: String?,
    val photoUrlThumbnail: String?,
    private val sessionIds: List<String>,
) {
    fun sessions(
        dfe: ExecutionContext,
    ): List<Session> {
        return dfe.source().sessions(
            sessionIds
        )
    }
}


data class Social(
    val icon: String?,
    @Deprecated("use url instead", ReplaceWith("url"))
    val link: String,
    val name: String,
) {
    @Suppress("DEPRECATION")
    val url: String
        get() = link
}

data class PartnerGroup(
    val title: String,
    val partners: List<Partner>,
)

data class Partner(
    val name: String,
    private val logoUrl: String,
    private val logoUrlDark: String?,
    val url: String,
) {
    /**
     * @param dark returns the logo for use on a dark background or fallbacks to the light mode if none exist
     */
    fun logoUrl(@GraphQLDefault("false") dark: Boolean?): String {
        return if (dark == true) {
            logoUrlDark ?: logoUrl
        } else {
            logoUrl
        }
    }
}

data class Venue(
    val id: String,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String? = null,
    val imageUrl: String?,
    /**
     * the url to an image containing the floor plan
     */
    val floorPlanUrl: String?,
    private val descriptions: Map<String, String>
) {
    @Deprecated(
        "use latitude and " +
            "longitude instead"
    )
    val coordinates: String?
        get() {
            return if (latitude != null && longitude != null) {
                "$latitude,$longitude"
            } else {
                null
            }
        }

    @Deprecated("use description(language: \"fr\") instead")
    val descriptionFr: String
        get() {
            return descriptions.get("fr") ?: descriptions.get("en") ?: ""
        }

    fun description(@GraphQLDefault("\"en\"") language: String?): String {
        return descriptions.get(language) ?: descriptions.get("en") ?: ""
    }
}

data class Conference(
    val id: String,
    val name: String,
    val timezone: String,
    val days: List<LocalDate>,
    val themeColor: String? = null
)
