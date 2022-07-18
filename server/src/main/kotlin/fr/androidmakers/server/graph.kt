package fr.androidmakers.server

import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import fr.androidmakers.server.model.*
import org.springframework.stereotype.Component

@Component
class RootQuery : Query {
  fun rooms(): List<Room> {
    return CachedData.rooms()
  }
  fun sessions(first: Int? = 10, after: String? = null): SessionConnection {
    return CachedData.sessions(first = first ?: 10, after = after)
  }
  fun speakers(): List<Speaker> {
    return CachedData.speakers()
  }

  fun venue(id: String): Venue {
    return CachedData.venue(id)
  }

  fun partnerGroups(): List<PartnerGroup> {
    return CachedData.partners()
  }

  fun session(id: String): Session {
    return CachedData.allSessions().first { it.id == id }
  }

  fun config(): Configuration {
    return Configuration(
        timezone = "Europe/Paris"
    )
  }
}

@Component
class RootMutation : Mutation {
  fun setSessionFavorite(sessionId: String, isFavorite: Boolean): Session {
    CachedData.setSessionFavorite(sessionId = sessionId, isFavorite = isFavorite)
    return CachedData.allSessions().first { it.id == sessionId }
  }
}
