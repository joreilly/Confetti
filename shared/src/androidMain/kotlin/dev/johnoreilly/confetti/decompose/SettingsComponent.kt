package dev.johnoreilly.confetti.decompose

import dev.johnoreilly.confetti.DarkThemeConfig
import dev.johnoreilly.confetti.DeveloperSettings
import dev.johnoreilly.confetti.ThemeBrand
import dev.johnoreilly.confetti.UserEditableSettings
import kotlinx.coroutines.flow.StateFlow

actual interface SettingsComponent {

    val developerSettings: StateFlow<DeveloperSettings?>
    val userEditableSettings: StateFlow<UserEditableSettings?>

    fun updateThemeBrand(themeBrand: ThemeBrand)
    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    fun updateDynamicColorPreference(useDynamicColor: Boolean)
    fun updateUseExperimentalFeatures(value: Boolean)
    fun enableDeveloperMode()
}