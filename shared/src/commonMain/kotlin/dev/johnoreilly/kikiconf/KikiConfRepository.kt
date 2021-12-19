package dev.johnoreilly.kikiconf

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.watch
import com.rickclephas.kmp.nativecoroutines.NativeCoroutineScope
import dev.johnoreilly.kikiconf.model.Room
import dev.johnoreilly.kikiconf.model.Session
import dev.johnoreilly.kikiconf.model.Speaker
import dev.johnoreilly.kikiconf.model.mapToModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class KikiConfRepository {
    @NativeCoroutineScope
    private val coroutineScope: CoroutineScope = MainScope()

    // Creates a 10MB MemoryCacheFactory
    val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)

    private val apolloClient = ApolloClient.Builder()
        .serverUrl("https://kiki-conf.ew.r.appspot.com/graphql")
        .normalizedCache(cacheFactory)
        .build()

    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions

    private val _speakers = MutableStateFlow<List<Speaker>>(emptyList())
    val speakers: StateFlow<List<Speaker>> = _speakers

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    init {
        coroutineScope.launch {
            apolloClient.query(GetSessionsQuery())
                .watch()
                .collect { response ->
                    _sessions.value = response.dataAssertNoErrors.sessions.map { it.mapToModel() }
                }
        }

        coroutineScope.launch {
            apolloClient.query(GetSpeakersQuery())
                .watch()
                .collect { response ->
                    _speakers.value = response.dataAssertNoErrors.speakers.map { it.mapToModel() }
                }
        }

        coroutineScope.launch {
            apolloClient.query(GetRoomsQuery())
                .watch()
                .collect { response ->
                    _rooms.value = response.dataAssertNoErrors.rooms.map { it.mapToModel() }
                }
        }
    }

    suspend fun getSession(sessionId: String): Session? {
        val response = apolloClient.query(GetSessionQuery(sessionId)).execute()
        return response.data?.session?.mapToModel()
    }
}