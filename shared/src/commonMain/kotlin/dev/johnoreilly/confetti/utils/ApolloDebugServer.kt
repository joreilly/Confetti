package dev.johnoreilly.confetti.utils

import com.apollographql.apollo3.ApolloClient

expect fun ApolloClient.registerApolloDebugServer(conference: String)

expect fun ApolloClient.unregisterApolloDebugServer()
