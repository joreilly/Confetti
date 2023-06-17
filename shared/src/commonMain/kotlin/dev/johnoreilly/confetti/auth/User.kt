package dev.johnoreilly.confetti.auth

import dev.johnoreilly.confetti.TokenProvider

interface User: TokenProvider {
    val name: String
    val email: String?
    val photoUrl: String?
    val uid: String
}