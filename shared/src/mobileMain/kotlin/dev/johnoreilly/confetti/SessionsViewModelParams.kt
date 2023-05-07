package dev.johnoreilly.confetti

import com.arkivanov.decompose.ComponentContext

class SessionsViewModelParams(
    val componentContext: ComponentContext,
    val conference: String,
    val uid: String?,
    val tokenProvider: TokenProvider?,
)
