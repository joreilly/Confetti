package dev.johnoreilly.confetti.backend

import graphql.ExecutionResult
import graphql.execution.ExecutionContext
import graphql.execution.instrumentation.Instrumentation
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters
import java.util.concurrent.CompletableFuture

class ApolloUsageReportingImplementation: Instrumentation {
    class ApolloState(val stack: MutableList<Trace.Node>): InstrumentationState


    override fun instrumentExecutionContext(
        executionContext: ExecutionContext?,
        parameters: InstrumentationExecutionParameters?,
        state: InstrumentationState?
    ): ExecutionContext {
        return super.instrumentExecutionContext(executionContext, parameters, state)
    }

    override fun beginExecution(
        parameters: InstrumentationExecutionParameters?,
        state: InstrumentationState?
    ): InstrumentationContext<ExecutionResult>? {
        return object: InstrumentationContext<ExecutionResult> {
            override fun onDispatched(result: CompletableFuture<ExecutionResult>?) {
            }

            override fun onCompleted(result: ExecutionResult?, t: Throwable?) {
            }

        }
    }

    override fun beginField(
        parameters: InstrumentationFieldParameters?,
        state: InstrumentationState?
    ): InstrumentationContext<ExecutionResult>? {
        return super.beginField(parameters, state)
    }
}