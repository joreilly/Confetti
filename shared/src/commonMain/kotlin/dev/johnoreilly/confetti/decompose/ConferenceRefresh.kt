package dev.johnoreilly.confetti.decompose

interface ConferenceRefresh {
    fun refresh(conference: String, fetchImages: Boolean = false)
}