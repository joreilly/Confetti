package dev.johnoreilly.confetti

import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import dev.johnoreilly.confetti.di.initKoin
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainTest {
    class TestComponent : KoinComponent {
        val apolloClientCache = get<ApolloClientCache>()
    }

    @Test
    @Ignore
    fun conferenceDataCanBeCatched() = runBlocking {
        val koin = initKoin()
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
