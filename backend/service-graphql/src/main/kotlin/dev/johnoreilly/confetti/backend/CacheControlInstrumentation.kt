package dev.johnoreilly.confetti.backend

import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.execution.Instrumentation
import com.apollographql.apollo3.execution.ResolveInfo

class CacheControl(var maxAge: Long) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<CacheControl>
        get() = Key

    companion object Key : ExecutionContext.Key<CacheControl>
}

class Conference(val conference: String) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<Conference>
        get() = Key

    companion object Key : ExecutionContext.Key<Conference>
}

class CacheControlInstrumentation : Instrumentation {
    override fun beforeResolve(resolveInfo: ResolveInfo) {
        val cacheControl = resolveInfo.executionContext[CacheControl] ?: error("")

        val conference = resolveInfo.executionContext[Conference]?.conference
        when {
            conference == "test" ||
                resolveInfo.fieldName == "bookmarks" ||
            resolveInfo.fieldName == "bookmarkConnection" -> {
                cacheControl.maxAge = 0
            }
        }
    }
}