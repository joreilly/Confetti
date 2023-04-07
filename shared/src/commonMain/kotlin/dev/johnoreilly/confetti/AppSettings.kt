package dev.johnoreilly.confetti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class AppSettings(val settings: FlowSettings) {

    suspend fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        val currentEnabledLanguagesString = settings.getStringOrNull(ENABLED_LANGUAGES_SETTING)
        val currentEnabledLanguagesSet =
            getEnabledLanguagesSetFromString(currentEnabledLanguagesString)

        val newEnabledLanguagesString = if (checked) {
            currentEnabledLanguagesSet.plus(language)
        } else {
            currentEnabledLanguagesSet.minus(language)
        }
        settings.putString(
            ENABLED_LANGUAGES_SETTING,
            newEnabledLanguagesString.joinToString(separator = ",")
        )
    }

    val experimentalFeaturesEnabledFlow = settings
        .getBooleanFlow(EXPERIMENTAL_FEATURES_ENABLED, false)

    suspend fun isExperimentalFeaturesEnabled() =
        settings.getBoolean(EXPERIMENTAL_FEATURES_ENABLED, false)

    suspend fun setExperimentalFeaturesEnabled(value: Boolean) {
        settings.putBoolean(EXPERIMENTAL_FEATURES_ENABLED, value)
    }

    suspend fun getConference(): String {
        return settings.getString(CONFERENCE_SETTING, CONFERENCE_NOT_SET)
    }

    fun getConferenceFlow(): Flow<String> {
        return settings.getStringFlow(CONFERENCE_SETTING, CONFERENCE_NOT_SET)
    }

    suspend fun setConference(conference: String) {
        settings.putString(CONFERENCE_SETTING, conference)
    }

    private fun getEnabledLanguagesSetFromString(settingsString: String?) =
        settingsString?.split(",")?.toSet() ?: emptySet()

    suspend fun updateGuestMode(guestMode: Boolean) {
        settings.putBoolean(GUEST_MODE, guestMode)
    }

    companion object {
        const val EXPERIMENTAL_FEATURES_ENABLED = "experimental_features_enabled"
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages_2"
        const val CONFERENCE_SETTING = "conference"
        const val GUEST_MODE = "guest_mode"
        const val CONFERENCE_NOT_SET = ""
    }
}
