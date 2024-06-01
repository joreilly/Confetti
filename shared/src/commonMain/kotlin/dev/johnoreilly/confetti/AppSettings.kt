package dev.johnoreilly.confetti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
        return settings.getStringFlow(CONFERENCE_SETTING, CONFERENCE_NOT_SET).first()
    }

    suspend fun getConferenceThemeColor(): String {
        return settings.getStringFlow(CONFERENCE_THEME_COLOR_SETTING, "0xFF800000").first()
    }

    fun getConferenceFlow(): Flow<String> {
        return settings.getStringFlow(CONFERENCE_SETTING, CONFERENCE_NOT_SET)
    }

    suspend fun setConference(conference: String) {
        settings.putString(CONFERENCE_SETTING, conference)
    }

    suspend fun setConferenceThemeColor(themeColor: String) {
        settings.putString(CONFERENCE_THEME_COLOR_SETTING, themeColor)
    }

    private fun getEnabledLanguagesSetFromString(settingsString: String?) =
        settingsString?.split(",")?.toSet() ?: emptySet()

    fun developerModeFlow() =
        settings.getBooleanFlow(DEVELOPER_MODE, false)

    suspend fun setDeveloperMode(b: Boolean) {
        settings.putBoolean(DEVELOPER_MODE, b)
    }

    companion object {
        const val DEVELOPER_MODE = "developer_mode"
        const val EXPERIMENTAL_FEATURES_ENABLED = "experimental_features_enabled"
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages_2"
        const val CONFERENCE_SETTING = "conference"
        const val CONFERENCE_THEME_COLOR_SETTING = "conferenceThemeColor"
        const val CONFERENCE_NOT_SET = ""
    }
}
