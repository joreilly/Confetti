package fr.androidmakers.server.model

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import fr.androidmakers.server.CachedData
import graphql.introspection.Introspection
import kotlinx.datetime.Instant

@GraphQLDirective(
    name = "experimental",
    description = "This field can be changed without warning",
    locations = [Introspection.DirectiveLocation.FIELD_DEFINITION]
)
annotation class Experimental

data class Room(
    val id: String,
    val name: String,
    val capacity: Int?,
)

data class SessionConnection(
    val totalCount: Int,
    val edges: List<SessionEdge>,
    val pageInfo: PageInfo,
)

data class SessionEdge(
    val node: Session,
    val cursor: String,
)

data class PageInfo(
    val hasPreviousPage: Boolean,
    val hasNextPage: Boolean,
    val startCursor: String,
    val endCursor: String,
)

/**
 * @param language an [IETF language code](https://en.wikipedia.org/wiki/IETF_language_tag) like en-US
 */
data class Session(
    val id: String,
    val title: String,
    val description: String,
    val language: String?,
    private val speakerIds: Set<String>,
    val tags: List<String>,
    @Experimental
    val startInstant: Instant,
    @Experimental
    val endInstant: Instant,
    private val roomId: String,
) {

    val speakers: List<Speaker>
        get() {
            return CachedData.speakers().filter {
                speakerIds.contains(it.id)
            }
        }

    // A session might not have a room yet
    val room: Room
        get() {
            return CachedData.rooms().single {
                it.id == roomId
            }
        }
}

data class Speaker(
    val id: String,
    val name: String,
    val bio: String,
    val company: String?,
    val socials: List<Social>,
    val photoUrl: String?,
)

data class Social(
    val icon: String,
    val link: String,
    val name: String,
)

data class PartnerGroup(
    val order: Int,
    val title: String,
    val partners: List<Partner>,
)

data class Partner(
    val order: Int,
    val name: String,
    val logoUrl: String,
    val url: String,
)


data class Venue(
    val name: String,
    val address: String? = null,
    val coordinates: String? = null,
    val description: String,
    val descriptionFr: String,
    val imageUrl: String,
)

data class Configuration(
    val timezone: String,
)
