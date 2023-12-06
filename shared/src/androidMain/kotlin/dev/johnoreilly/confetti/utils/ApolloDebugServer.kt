package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.debugserver.ApolloDebugServer
import dev.johnoreilly.confetti.shared.BuildConfig

actual fun ApolloClient.registerApolloDebugServer(conference: String) {
    if (BuildConfig.DEBUG) ApolloDebugServer.registerApolloClient(this, conference)
}

actual fun ApolloClient.unregisterApolloDebugServer() {
    if (BuildConfig.DEBUG) ApolloDebugServer.unregisterApolloClient(this)
}
