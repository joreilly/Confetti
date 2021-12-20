package dev.johnoreilly.kikiconf.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.normalizedCache
import dev.johnoreilly.kikiconf.KikiConfRepository
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule())
    }

// called by iOS client
fun initKoin() = initKoin() {}

fun commonModule() = module {
    single { KikiConfRepository() }
    single { createApolloClient() }
}

fun createApolloClient(): ApolloClient {
    // Creates a 10MB MemoryCacheFactory
    val cacheFactory = MemoryCacheFactory(maxSizeBytes = 10 * 1024 * 1024)

    return ApolloClient.Builder()
        .serverUrl("https://kiki-conf.ew.r.appspot.com/graphql")
        .normalizedCache(cacheFactory)
        .build()
}
