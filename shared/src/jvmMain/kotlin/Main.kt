package dev.johnoreilly.confetti

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.FakeResolverContext
import com.apollographql.apollo.testing.MapTestNetworkTransport
import com.apollographql.cache.normalized.FetchPolicy
import com.benasher44.uuid.uuid4
import dev.johnoreilly.confetti.builder.Data
import dev.johnoreilly.confetti.builder.resolver.DefaultFakeResolver
import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDateTime
import org.koin.dsl.module


private fun testModule() = module {
    factory {
        ApolloClient.Builder()
            .networkTransport(MapTestNetworkTransport().apply {
                register(GetSessionsQuery(), ApolloResponse.Builder(
                    GetSessionsQuery(),
                    uuid4(),
                ).data(
                    GetSessionsQuery.Data(object: DefaultFakeResolver() {
                        override fun resolveLeaf(context: FakeResolverContext): Any {
                            return when(context.mergedField.type.rawType().name) {
                                // Apollo 5 data builders serialize leaf values through the
                                // response adapter, so return the scalar's wire form (String)
                                "LocalDateTime" -> return LocalDateTime(1970, 1, 1, 1, 1, 1).toString()
                                else -> super.resolveLeaf(context)
                            }
                        }
                    }) {}
                ).build())
            })
    }
}

@Suppress("UNUSED_PARAMETER")
suspend fun main(args: Array<String>) {
    val koin = initKoin {
        modules(testModule())
    }.koin
    val repo = koin.get<ConfettiRepository>()
    val clientCache = koin.get<ApolloClientCache>()

    try {
        withTimeout(60000L) {
            println("Sessions")
            val sessions = repo.sessionsQuery("droidconberlin2023", fetchPolicy = FetchPolicy.CacheFirst).toFlow().first {
                // First emission is a cache miss, ignore it
                it.exception == null
            }
            sessions.data!!.sessions.nodes.map { it.sessionDetails }.forEach { session ->
                println("${session.startsAt} ${session.title}")
            }
        }
    } finally {
        println("done")
        clientCache.close()
    }
}
