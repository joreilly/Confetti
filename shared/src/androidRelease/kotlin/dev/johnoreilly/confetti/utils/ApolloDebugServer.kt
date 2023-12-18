package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloClient

actual fun ApolloClient.registerApolloDebugServer(conference: String) {
    // no-op
}

actual fun ApolloClient.unregisterApolloDebugServer() {
    // no-op
}
