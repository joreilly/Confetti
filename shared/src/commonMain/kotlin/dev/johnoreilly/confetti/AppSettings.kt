package dev.johnoreilly.confetti

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSettingsApi::class)
class AppSettings(val settings: ObservableSettings) {

    val enabledLanguages: Flow<Set<String>> =
        settings.getStringOrNullFlow(ENABLED_LANGUAGES_SETTING).map { getEnabledLanguagesSetFromString(it) }

    init {
        if (settings.getString(ENABLED_LANGUAGES_SETTING, "").isEmpty()) {
            settings.putString(ENABLED_LANGUAGES_SETTING, "en-US")
        }
    }


    fun updateEnableLanguageSetting(language: String, checked: Boolean) {
        val currentEnabledLanguagesString = settings.getStringOrNull(ENABLED_LANGUAGES_SETTING)
        val currentEnabledLanguagesSet = getEnabledLanguagesSetFromString(currentEnabledLanguagesString)

        val newEnabledLanguagesString = if (checked) {
            currentEnabledLanguagesSet.plus(language)
        } else {
            currentEnabledLanguagesSet.minus(language)
        }
        settings.putString(ENABLED_LANGUAGES_SETTING, newEnabledLanguagesString.joinToString(separator = ","))
    }

    fun getConference(): String {
        return settings.getString(CONFERENCE_SETTING, "")
    }

    fun setConference(conference: String) {
        settings.putString(CONFERENCE_SETTING, conference)
    }

    private fun getEnabledLanguagesSetFromString(settingsString: String?) =
        settingsString?.split(",")?.toSet() ?: emptySet()

    companion object {
        const val ENABLED_LANGUAGES_SETTING = "enabled_languages_2"
        const val CONFERENCE_SETTING = "conference"
    }
}