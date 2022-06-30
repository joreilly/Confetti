package dev.johnoreilly.confetti.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.cache.normalized.api.*
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.network.http.LoggingInterceptor
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ConfettiRepository
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
        .serverUrl("https://confetti-349319.uw.r.appspot.com/graphql")
//        .serverUrl("http://10.0.2.2:8080/graphql")
        .addHttpInterceptor(LoggingInterceptor())
        .normalizedCache(
            normalizedCacheFactory = memoryFirstThenSqlCacheFactory,
            cacheKeyGenerator = TypePolicyCacheKeyGenerator,
            metadataGenerator = CursorPaginationMetadataGenerator(setOf("SessionConnection")),
            apolloResolver = FieldPolicyApolloResolver,
            recordMerger = FieldRecordMerger(CursorPaginationFieldMerger()),
            writeToCacheAsynchronously = true
        )
        .build()
}

@OptIn(ApolloExperimental::class)
@Suppress("UNCHECKED_CAST")
private class CursorPaginationMetadataGenerator(private val connectionTypes: Set<String>) :
    MetadataGenerator {
    override fun metadataForObject(
        obj: Any?,
        context: MetadataGeneratorContext
    ): Map<String, Any?> {
        if (context.field.type.leafType().name in connectionTypes) {
            obj as Map<String, Any?>
            val edges = obj["edges"] as List<Map<String, Any?>>
            val startCursor = edges.firstOrNull()?.get("cursor") as String?
            val endCursor = edges.lastOrNull()?.get("cursor") as String?
            return mapOf(
                "startCursor" to startCursor,
                "endCursor" to endCursor,
                "before" to context.argumentValue("before"),
                "after" to context.argumentValue("after"),
            )
        }
        return emptyMap()
    }
}

@OptIn(ApolloExperimental::class)
private class CursorPaginationFieldMerger : FieldRecordMerger.FieldMerger {
    @Suppress("UNCHECKED_CAST")
    override fun mergeFields(
        existing: FieldRecordMerger.FieldInfo,
        incoming: FieldRecordMerger.FieldInfo
    ): FieldRecordMerger.FieldInfo {
        val existingStartCursor = existing.metadata["startCursor"] as? String
        val existingEndCursor = existing.metadata["endCursor"] as? String
        val incomingStartCursor = incoming.metadata["startCursor"] as? String
        val incomingEndCursor = incoming.metadata["endCursor"] as? String
        val incomingBeforeArgument = incoming.metadata["before"] as? String
        val incomingAfterArgument = incoming.metadata["after"] as? String

        return if (incomingBeforeArgument == null && incomingAfterArgument == null) {
            // Not a pagination query
            incoming
        } else if (existingStartCursor == null || existingEndCursor == null) {
            // Existing is empty
            incoming
        } else if (incomingStartCursor == null || incomingEndCursor == null) {
            // Incoming is empty
            existing
        } else {
            val existingValue = existing.value as Map<String, Any?>
            val existingList = existingValue["edges"] as List<*>
            val incomingList = (incoming.value as Map<String, Any?>)["edges"] as List<*>

            val mergedList: List<*>
            val newStartCursor: String
            val newEndCursor: String
            if (incomingAfterArgument == existingEndCursor) {
                mergedList = existingList + incomingList
                newStartCursor = existingStartCursor
                newEndCursor = incomingEndCursor
            } else if (incomingBeforeArgument == existingStartCursor) {
                mergedList = incomingList + existingList
                newStartCursor = incomingStartCursor
                newEndCursor = existingEndCursor
            } else {
                // We received a list which is neither the previous nor the next page.
                // Handle this case by resetting the cache with this page
                mergedList = incomingList
                newStartCursor = incomingStartCursor
                newEndCursor = incomingEndCursor
            }

            val mergedFieldValue = existingValue.toMutableMap()
            mergedFieldValue["edges"] = mergedList
            FieldRecordMerger.FieldInfo(
                value = mergedFieldValue,
                metadata = mapOf("startCursor" to newStartCursor, "endCursor" to newEndCursor)
            )
        }
    }
}
