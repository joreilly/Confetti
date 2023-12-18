package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.debugserver.ApolloDebugServer

actual fun ApolloClient.registerApolloDebugServer(conference: String) {
    if (isInUnitTests) {
        // No-op in unit tests, as it's called multiple times without calling unregister
        return
    }
    ApolloDebugServer.registerApolloClient(this, conference)
}

actual fun ApolloClient.unregisterApolloDebugServer() {
    ApolloDebugServer.unregisterApolloClient(this)
}
