package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.flow.StateFlow

actual interface SettingsComponent {

    val developerSettings: StateFlow<DeveloperSettings?>
    val userEditableSettings: StateFlow<UserEditableSettings?>

    fun updateThemeBrand(themeBrand: ThemeBrand)
    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig)
    fun updateDynamicColorPreference(useDynamicColor: Boolean)
    fun updateUseExperimentalFeatures(value: Boolean)
    fun updateWearTheme(active: Boolean)
    fun installOnWatch(nodeId: String)
    fun enableDeveloperMode()
}

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
    val wearStatus: WearStatus
)

sealed interface WearStatus {
    object Unavailable : WearStatus
    data class NotInstalled(val nodeId: String) : WearStatus
    data class Paired(
        val wearSettings: WearSettings
    ) : WearStatus
}

enum class ThemeBrand {
    DEFAULT, ANDROID
}

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}
