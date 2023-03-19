package dev.johnoreilly.confetti.backend

import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.KEY_CONFERENCE
import dev.johnoreilly.confetti.backend.DefaultApplication.Companion.KEY_SOURCE
import graphql.ExecutionResult
import graphql.ExecutionResultImpl
import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters
import net.mbonnin.bare.graphql.cast
import java.util.concurrent.CompletableFuture

class CacheControlInstrumentation : Instrumentation {
    class MyState(var maxAge: Int) : InstrumentationState

    override fun createState(parameters: InstrumentationCreateStateParameters?): InstrumentationState? {
        return MyState(1800)
    }

    override fun beginField(
        parameters: InstrumentationFieldParameters,
        state: InstrumentationState
    ): InstrumentationContext<ExecutionResult> {
        val conference = parameters.executionContext.graphQLContext.get<String>(KEY_CONFERENCE)
        when {
             conference == "test" ||
                parameters.field.name == "bookmarks" -> {
                state.cast<MyState>().maxAge = 0
            }
        }
        return super.beginField(parameters, state)!!
    }

    override fun instrumentExecutionResult(
        executionResult: ExecutionResult,
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState
    ): CompletableFuture<ExecutionResult> {

        return CompletableFuture.completedFuture(
            executionResult.cast<ExecutionResultImpl>().transform {
                it.addExtension("maxAge", state.cast<MyState>().maxAge)
            })
    }
}