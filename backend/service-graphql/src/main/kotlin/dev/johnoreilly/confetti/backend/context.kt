package dev.johnoreilly.confetti.backend

import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.execution.GraphQLResponse
import dev.johnoreilly.confetti.backend.graphql.DataSource


class UserId(val uid: String) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<UserId>
        get() = Key

    companion object Key : ExecutionContext.Key<UserId>
}



internal class Source(val source: DataSource) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<Source>
        get() = Key

    companion object Key : ExecutionContext.Key<Source>
}



internal fun GraphQLResponse.canBeCached(): Boolean {
    return errors.orEmpty().none { it.message.lowercase().contains("PersistedQueryNotFound".lowercase()) }
}

val KEY_REQUEST = "request"
val KEY_HEADERS = "headers"