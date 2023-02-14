package dev.johnoreilly.confetti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class AppSettings(val settings: FlowSettings) {

    suspend fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        val currentEnabledLanguagesString = settings.getStringOrNull(ENABLED_LANGUAGES_SETTING)
        val currentEnabledLanguagesSet = getEnabledLanguagesSetFromString(currentEnabledLanguagesString)

        val newEnabledLanguagesString = if (checked) {
            currentEnabledLanguagesSet.plus(language)
        } else {
            currentEnabledLanguagesSet.minus(language)
        }
        settings.putString(ENABLED_LANGUAGES_SETTING, newEnabledLanguagesString.joinToString(separator = ","))
    }

    suspend fun getConference(): String? {
        return settings.getStringOrNull(CONFERENCE_SETTING)
    }

    fun getConferenceFlow(): Flow<String?> {
        return settings.getStringOrNullFlow(CONFERENCE_SETTING)
    }

    suspend fun setConference(conference: String?) {
        if (conference != null) {
            settings.putString(CONFERENCE_SETTING, conference)
        } else {
            settings.remove(CONFERENCE_SETTING)
        }
    }

    private fun getEnabledLanguagesSetFromString(settingsString: String?) =
        settingsString?.split(",")?.toSet() ?: emptySet()

    companion object {
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages_2"
        const val CONFERENCE_SETTING = "conference"
    }
}