package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import com.apollographql.apollo3.testing.MapTestNetworkTransport
import com.benasher44.uuid.uuid4
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.schema.__Schema
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
                    GetSessionsQuery.Data(object: DefaultFakeResolver(__Schema.all) {
                        override fun resolveLeaf(context: FakeResolverContext): Any {
                            return when(context.mergedField.type.rawType().name) {
                                "LocalDateTime" -> return LocalDateTime(1970, 1, 1, 1, 1, 1)
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
            val sessions = repo.sessionsQuery("droidconlondon2022").toFlow().first {
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
