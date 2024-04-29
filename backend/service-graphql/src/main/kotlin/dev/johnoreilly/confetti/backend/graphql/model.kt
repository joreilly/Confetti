package dev.johnoreilly.confetti.backend.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDeprecated
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.google.firebase.auth.FirebaseAuth
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.KEY_SOURCE
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.KEY_UID
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DDirection
import dev.johnoreilly.confetti.backend.datastore.DOrderBy
import dev.johnoreilly.confetti.backend.datastore.DataStore
import graphql.introspection.Introspection
import graphql.schema.DataFetchingEnvironment
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.springframework.stereotype.Component

@GraphQLDirective(
    name = "requiresOptIn",
    description = "This field can be changed without warning",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class RequiresOptIn(val feature: String)

@Component
class RootMutation : Mutation {
    fun addBookmark(dfe: DataFetchingEnvironment, sessionId: String): Bookmarks {
        return Bookmarks(dfe.source().addBookmark(sessionId).toList())
    }

    fun removeBookmark(dfe: DataFetchingEnvironment, sessionId: String): Bookmarks {
        return Bookmarks(dfe.source().removeBookmark(sessionId).toList())
    }

    /**
     * Deletes the current user account, requires authentication
     */
    fun deleteAccount(dfe: DataFetchingEnvironment): Boolean {
        val uid = dfe.uid()
        if (uid == null) {
            return false
        }

        FirebaseAuth.getInstance().deleteUser(uid)
        dfe.source().deleteUserData()
        return true
    }
}

@Component
class RootQuery : Query {
    fun rooms(dfe: DataFetchingEnvironment): List<Room> {
        return dfe.source().rooms()
    }

    fun sessions(
        dfe: DataFetchingEnvironment,
        first: Int? = 10,
        after: String? = null,
        filter: SessionFilter? = null,
        orderBy: SessionOrderBy? = SessionOrderBy(
            field = SessionField.STARTS_AT,
            direction = OrderByDirection.ASCENDING
        )
    ): SessionConnection {
        return dfe.source().sessions(
            first ?: 10,
            after,
            filter,
            orderBy
        )
    }

    @Deprecated("Use speakersPage instead")
    fun speakers(dfe: DataFetchingEnvironment): List<Speaker> {
        return dfe.source().speakers(first = 100, after = null).nodes
    }

    fun speakersPage(
        dfe: DataFetchingEnvironment,
        first: Int? = 10,
        after: String? = null,
    ): SpeakerConnection {
        return dfe.source().speakers(first ?: 10, after)
    }

    fun speaker(dfe: DataFetchingEnvironment, id: String): Speaker {
        return dfe.source().speaker(id)
    }

    fun venue(dfe: DataFetchingEnvironment, id: String): Venue {
        return dfe.source().venues().first { it.id == id }
    }

    fun venues(dfe: DataFetchingEnvironment): List<Venue> {
        return dfe.source().venues()
    }

    fun partnerGroups(dfe: DataFetchingEnvironment): List<PartnerGroup> {
        return dfe.source().partnerGroups()
    }

    fun session(dfe: DataFetchingEnvironment, id: String): Session {
        return dfe.source().sessions(listOf(id)).single()
    }

    fun config(dfe: DataFetchingEnvironment): Conference {
        return dfe.source().conference()
    }

    @GraphQLDeprecated("Use bookmarkConnection instead")
    fun bookmarks(dfe: DataFetchingEnvironment): Bookmarks? {
        if (dfe.uid() == null) {
            return null
        }
        return Bookmarks(dfe.source().bookmarks().toList())
    }

    fun bookmarkConnection(dfe: DataFetchingEnvironment): BookmarkConnection? {
        if (dfe.uid() == null) {
            return null
        }
        return BookmarkConnection(
            nodes = dfe.source().sessions(dfe.source().bookmarks().toList())
        )
    }

    fun conferences(orderBy: ConferenceOrderBy? = null): List<Conference> {
        val orderBy1 =
            orderBy ?: ConferenceOrderBy(ConferenceField.DAYS, OrderByDirection.DESCENDING)
        return DataStore().readConfigs(
            DOrderBy(orderBy1.field.value, orderBy1.direction.toDDirection())
        ).map {
            it.toConference()
        }
    }
}

class  BookmarkConnection(
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

class LocalDateTimeFilter(
    val before: LocalDateTime? = null,
    val after: LocalDateTime? = null,
)

class SessionFilter(
    val startsAt: LocalDateTimeFilter? = null,
    val endsAt: LocalDateTimeFilter? = null,
)

class SessionOrderBy(
    val field: SessionField,
    val direction: OrderByDirection
)

enum class SessionField(val value: String) {
    STARTS_AT("start"),
}

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

fun DataFetchingEnvironment.uid(): String? {
    return graphQlContext.get(KEY_UID)
}
private fun DataFetchingEnvironment.source(): DataSource {
    return graphQlContext.get(KEY_SOURCE)
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

enum class  LinkType {
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
    @GraphQLDescription("""A shorter version of description for use when real estate is scarce like watches for an example.
This field might have the same value as description if a shortDescription is not available""")
    val shortDescription: String?,
    @GraphQLDescription("""An [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US""")
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
    @GraphQLDescription("""One of "break", "lunch", "party", "keynote", "talk" or any other conference-specific format""")
    val type: String,
    val links: List<Link>
) {
    fun speakers(dfe: DataFetchingEnvironment): List<Speaker> {
        return dfe.source().speakers(speakerIds.toList())
    }

    fun room(dfe: DataFetchingEnvironment): Room? {
        val roomId = roomIds.firstOrNull()
        if (roomId == null) {
            return null
        }
        return dfe.source().rooms().firstOrNull {
            it.id == roomId
        }
    }

    fun rooms(dfe: DataFetchingEnvironment): List<Room> {
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
        dfe: DataFetchingEnvironment,
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
    fun logoUrl(dark: Boolean? = false): String {
        return if (dark == true) {
            logoUrlDark ?: logoUrl
        } else {
            logoUrl
        }
    }
}

/**
 * @property floorPlanUrl the url to an image containing the floor plan
 */
data class Venue(
    val id: String,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String? = null,
    val imageUrl: String?,
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

    fun description(language: String? = "en"): String {
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
