package dev.johnoreilly.confetti.wear.navigation

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed class Config {
    val loggingName: String
        get() = this::class.java.simpleName

    open val loggingArguments: Map<String, String>
        get() = mapOf()

    @Serializable
    data object Loading : Config()

    @Serializable
    data object Conferences : Config()

    interface UserAware {
        val uid: String?
        fun onUserChanged(uid: String?): Config
    }

    interface ConferenceAware {
        val conference: String
    }

    @Serializable
    data class ConferenceSessions(
        override val uid: String?, // Unused, but needed to recreated the component when the user changes
        override val conference: String,
        val date: LocalDate? = null
    ) : Config(), UserAware, ConferenceAware {
        override val loggingArguments: Map<String, String>
            get() = mapOf("conference" to conference)

        override fun onUserChanged(uid: String?): Config {
            return this.copy(uid = uid)
        }
    }

    @Serializable
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

    @Serializable
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

    @Serializable
    data object Settings : Config()

    @Serializable
    data object GoogleSignIn : Config()

    @Serializable
    data object GoogleSignOut : Config()

    @Serializable
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

    @Serializable
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