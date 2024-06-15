package dev.johnoreilly.confetti.decompose

import kotlinx.coroutines.flow.StateFlow

data class DeveloperSettings(
    val token: String?
)

/**
 * Represents the settings which the user can edit within the app.
 */
data class UserEditableSettings(
    val brand: ThemeBrand,
    val useDynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
    val useExperimentalFeatures: Boolean,
)

enum class ThemeBrand {
    DEFAULT, ANDROID
}

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}


interface SettingsComponent {

    val developerSettings: StateFlow<DeveloperSettings?>
    val userEditableSettings: StateFlow<UserEditableSettings?>

    fun updateThemeBrand(themeBrand: ThemeBrand)
    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    fun updateDynamicColorPreference(useDynamicColor: Boolean)
    fun updateUseExperimentalFeatures(value: Boolean)
    fun enableDeveloperMode()
}