package dev.johnoreilly.confetti

data class SessionsViewModelParams(val conference: String, val uid: String?, val tokenProvider: TokenProvider?)
