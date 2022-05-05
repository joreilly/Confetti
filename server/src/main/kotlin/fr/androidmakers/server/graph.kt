package fr.androidmakers.server

import com.expediagroup.graphql.server.operations.Query
import fr.androidmakers.server.model.*
import org.springframework.stereotype.Component

@Component
class RootQuery : Query {
  fun rooms(): List<Room> {
    return CachedData.rooms()
  }
  fun sessions(): List<Session> {
    return CachedData.sessions()
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
    return CachedData.sessions().first { it.id == id }
  }

  fun config(): Configuration {
    return Configuration(
        timezone = "Europe/Paris"
    )
  }
}
