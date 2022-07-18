package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.api.*
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.LoggingInterceptor
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
import dev.johnoreilly.confetti.pagination.Pagination
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect fun platformModule(): Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(commonModule(), platformModule())
    }

// called by iOS client
fun initKoin() = initKoin() {}

fun commonModule() = module {
    single { ConfettiRepository() }
    single { createApolloClient(get()) }
    single { AppSettings(get()) }
}

@OptIn(ApolloExperimental::class)
fun createApolloClient(sqlNormalizedCacheFactory: NormalizedCacheFactory): ApolloClient {
    val memoryFirstThenSqlCacheFactory = MemoryCacheFactory(10 * 1024 * 1024)
        .chain(sqlNormalizedCacheFactory)

    return ApolloClient.Builder()
//        .serverUrl("https://confetti-349319.uw.r.appspot.com/graphql")
        .serverUrl("http://10.0.2.2:8080/graphql")
        .addHttpInterceptor(LoggingInterceptor())
        .normalizedCache(
            normalizedCacheFactory = memoryFirstThenSqlCacheFactory,
            cacheKeyGenerator = TypePolicyCacheKeyGenerator,
            metadataGenerator = ConnectionMetadataGenerator(Pagination.connectionTypes),
            apolloResolver = FieldPolicyApolloResolver,
            recordMerger = ConnectionRecordMerger,
            writeToCacheAsynchronously = true
        )
        .build()
}
