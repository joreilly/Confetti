package dev.johnoreilly.confetti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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

    val notificationsEnabledFlow: Flow<Boolean> = settings
    .getBooleanFlow(NOTIFICATIONS_ENABLED, false)

    val experimentalFeaturesEnabledFlow = settings
        .getBooleanFlow(EXPERIMENTAL_FEATURES_ENABLED, false)

    suspend fun isExperimentalFeaturesEnabled() =
        settings.getBoolean(EXPERIMENTAL_FEATURES_ENABLED, false)

    suspend fun setExperimentalFeaturesEnabled(value: Boolean) {
        settings.putBoolean(EXPERIMENTAL_FEATURES_ENABLED, value)
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        settings.putBoolean(NOTIFICATIONS_ENABLED, value)
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

    val forceEnableAssistantFlow = settings
        .getBooleanFlow(FORCE_ENABLE_ASSISTANT, false)

    suspend fun setForceEnableAssistant(value: Boolean) {
        settings.putBoolean(FORCE_ENABLE_ASSISTANT, value)
    }

    val onboardingCompletedFlow: Flow<Boolean> = settings
        .getBooleanFlow(ONBOARDING_COMPLETED, false)

    suspend fun isOnboardingCompleted(): Boolean =
        settings.getBoolean(ONBOARDING_COMPLETED, false)

    suspend fun setOnboardingCompleted(value: Boolean) {
        settings.putBoolean(ONBOARDING_COMPLETED, value)
    }

    // Stored per conference since track names differ between conferences. Newline-separated
    // since track names are free text (e.g. "agentic codingCon") but never span lines.
    fun selectedTracksFlow(conference: String): Flow<Set<String>> = settings
        .getStringFlow(selectedTracksKey(conference), "")
        .map { stored -> stored.split(SELECTED_TRACKS_SEPARATOR).filter { it.isNotEmpty() }.toSet() }

    suspend fun setSelectedTracks(conference: String, tracks: Set<String>) {
        settings.putString(selectedTracksKey(conference), tracks.joinToString(SELECTED_TRACKS_SEPARATOR))
    }

    private fun selectedTracksKey(conference: String) = "$SELECTED_TRACKS_PREFIX$conference"

    companion object {
        const val DEVELOPER_MODE = "developer_mode"
        const val EXPERIMENTAL_FEATURES_ENABLED = "experimental_features_enabled"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages_2"
        const val CONFERENCE_SETTING = "conference"
        const val CONFERENCE_THEME_COLOR_SETTING = "conferenceThemeColor"
        const val CONFERENCE_NOT_SET = ""
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val FORCE_ENABLE_ASSISTANT = "force_enable_assistant"
        const val SELECTED_TRACKS_PREFIX = "selected_tracks_"
        private const val SELECTED_TRACKS_SEPARATOR = "\n"
    }
}
