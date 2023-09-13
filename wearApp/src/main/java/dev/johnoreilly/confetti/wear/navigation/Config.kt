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

    interface UserAware {
        val uid: String?
        fun onUserChanged(uid: String?): Config
    }

    interface ConferenceAware {
        val conference: String
    }

    data class ConferenceSessions(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
        @TypeParceler<LocalDate?, LocalDateParceler>() val date: LocalDate? = null
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }

    data class SessionDetails(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
        val session: String,
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference, "session" to session)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }

    data class SpeakerDetails(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
        val speaker: String,
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference, "speaker" to speaker)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }

    object Settings : Config()

    object GoogleSignIn : Config()

    object GoogleSignOut : Config()

    data class Bookmarks(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }

    data class Home(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }
}