package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.debugserver.ApolloDebugServer

actual fun ApolloClient.registerApolloDebugServer(conference: String) {
    ApolloDebugServer.registerApolloClient(this, conference)
}

actual fun ApolloClient.unregisterApolloDebugServer() {
    ApolloDebugServer.unregisterApolloClient(this)
}
