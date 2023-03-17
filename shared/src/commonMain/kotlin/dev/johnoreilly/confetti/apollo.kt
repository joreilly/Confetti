
package dev.johnoreilly.confetti

import com.apollographql.apollo3.ApolloCall
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.ExecutionContext
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.exception.ApolloException
import com.benasher44.uuid.uuid4

val ApolloResponse<*>.exception: ApolloException?
    get() = executionContext[ExceptionElement]?.exception

private class ExceptionElement(val exception: ApolloException) : ExecutionContext.Element {
    override val key: ExecutionContext.Key<*> = Key

    companion object Key : ExecutionContext.Key<ExceptionElement>
}

private fun <D : Operation.Data> ApolloErrorResponse(operation: Operation<D>, exception: ApolloException) =
    ApolloResponse.Builder(operation = operation, requestUuid = uuid4(), data = null)
        .addExecutionContext(ExceptionElement(exception))
        .build()

suspend fun <D : Operation.Data> ApolloCall<D>.tryExecute(): ApolloResponse<D> = try {
    execute()
} catch (e: ApolloException) {
    ApolloErrorResponse(operation = operation, exception = e)
}