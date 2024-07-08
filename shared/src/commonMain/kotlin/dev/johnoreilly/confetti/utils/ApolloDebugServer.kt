package dev.johnoreilly.confetti.utils

import com.apollographql.apollo.ApolloClient

expect fun ApolloClient.registerApolloDebugServer(conference: String)

expect fun ApolloClient.unregisterApolloDebugServer()
