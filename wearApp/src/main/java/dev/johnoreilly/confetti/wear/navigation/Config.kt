package dev.johnoreilly.confetti.wear.navigation


import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize
import com.arkivanov.essenty.parcelable.TypeParceler
import kotlinx.datetime.LocalDate

@Parcelize
sealed class Config : Parcelable {
    val loggingName: String
        get() = this::class.java.simpleName

    open val loggingArguments: Map<String, String>
        get() = mapOf()

    object Loading : Config()
    object Conferences : Config()

    data class ConferenceSessions(
        val uid: String?, // Unused, but needed to recreated the component when the user changes
        val conference: String,
        @TypeParceler<LocalDate?, LocalDateParceler>() val date: LocalDate? = null
    ) : Config() {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)
    }

    data class SessionDetails(
        val uid: String?, // Unused, but needed to recreated the component when the user changes
        val conference: String,
        val session: String,
    ) : Config() {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference, "session" to session)
    }

    data class SpeakerDetails(
        val uid: String?, // Unused, but needed to recreated the component when the user changes
        val conference: String,
        val speaker: String,
    ) : Config() {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference, "speaker" to speaker)
    }

    object Settings : Config()

    object GoogleSignIn : Config()

    object GoogleSignOut : Config()

    data class Bookmarks(
        val uid: String?, // Unused, but needed to recreated the component when the user changes
        val conference: String,
    ) : Config() {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)
    }

    data class Home(
        val uid: String?, // Unused, but needed to recreated the component when the user changes
        val conference: String,
    ) : Config() {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)
    }
}