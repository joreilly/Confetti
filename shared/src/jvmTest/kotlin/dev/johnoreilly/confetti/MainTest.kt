package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.DefaultFakeResolver
import com.apollographql.apollo3.api.FakeResolverContext
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.testing.MapTestNetworkTransport
import com.benasher44.uuid.uuid4
import dev.johnoreilly.confetti.di.initKoin
import dev.johnoreilly.confetti.schema.__Schema
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.dsl.module

fun testModule() = module {
    factory {
        ApolloClient.Builder()
            .networkTransport(MapTestNetworkTransport().apply {
                register(GetSessionsQuery(), ApolloResponse.Builder(
                    GetSessionsQuery(),
                    uuid4(),
                    GetSessionsQuery.Data(object: DefaultFakeResolver(__Schema.all) {
                        override fun resolveLeaf(context: FakeResolverContext): Any {
                            return when(context.mergedField.type.rawType().name) {
                                "LocalDateTime" -> return kotlinx.datetime.LocalDateTime(1970, 1, 1, 1, 1, 1)
                                else -> super.resolveLeaf(context)
                            }
                        }
                    }) {}
                ).build())
            })
    }
}
class MainTest {
    class TestComponent : KoinComponent {
        val apolloClientCache = get<ApolloClientCache>()
    }

    @Test
    @Ignore
    fun conferenceDataCanBeCatched() = runBlocking {
        val koin = initKoin {
            testModule()
        }

        val testComponent = TestComponent()
        val conference = "fosdem2023"

        val apolloClient = testComponent.apolloClientCache.getClient(conference)

        runBlocking {
            // put in cache
            apolloClient.query(GetConferenceDataQuery()).execute()

            val response = apolloClient.query(GetConferenceDataQuery()).fetchPolicy(FetchPolicy.CacheOnly).execute()
            println(response.data)
        }
    }
}