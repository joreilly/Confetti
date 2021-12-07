package dev.johnoreilly.kikiconf

import com.apollographql.apollo3.ApolloClient

class KikiConfRepository {
    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://kiki-conf.ew.r.appspot.com/graphql")
        .build()


    suspend fun getSessions(): List<GetSessionsQuery.Session> {
        val response = apolloClient.query(GetSessionsQuery()).execute()
        return response.dataOrThrow.sessions

    }
}