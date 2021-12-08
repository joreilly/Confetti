package dev.johnoreilly.kikiconf

import com.apollographql.apollo3.ApolloClient
import dev.johnoreilly.kikiconf.model.Room
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker
import dev.johnoreilly.kikiconf.model.mapToModel

class KikiConfRepository {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://kiki-conf.ew.r.appspot.com/graphql")
        .build()

    suspend fun getSessions(): List<Session> {
        val response = apolloClient.query(GetSessionsQuery()).execute()
        return response.dataOrThrow.sessions.map { it.mapToModel() }
    }

    suspend fun getSpeakers(): List<Speaker> {
        val response = apolloClient.query(GetSpeakersQuery()).execute()
        return response.dataOrThrow.speakers.map { it.mapToModel() }
    }

    suspend fun getRooms(): List<Room> {
        val response = apolloClient.query(GetRoomsQuery()).execute()
        return response.dataOrThrow.rooms.map { it.mapToModel() }
    }

}