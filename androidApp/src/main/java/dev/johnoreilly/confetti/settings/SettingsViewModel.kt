@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.wear.WearSettingsSync
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(val appSettings: AppSettings, val wearSettingsSync: WearSettingsSync) : ViewModel() {

    private val settings = appSettings.settings

    val userEditableSettings: StateFlow<UserEditableSettings> =
        combine(settings.getStringFlow(brandKey, ThemeBrand.DEFAULT.toString()),
            settings.getStringFlow(darkThemeConfigKey, DarkThemeConfig.FOLLOW_SYSTEM.toString()),
            settings.getBooleanFlow(useDynamicColorKey, false),
        ) { themeBrand, darkThemeConfig, useDynamicColor ->
            UserEditableSettings(
                    brand = ThemeBrand.valueOf(themeBrand),
                    useDynamicColor = useDynamicColor,
                    darkThemeConfig = DarkThemeConfig.valueOf(darkThemeConfig),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserEditableSettings(ThemeBrand.DEFAULT, false, DarkThemeConfig.FOLLOW_SYSTEM),
        )


    fun updateThemeBrand(themeBrand: ThemeBrand) {
        viewModelScope.launch {
            settings.putString(brandKey, themeBrand.toString())
        }
    }

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            settings.putString(darkThemeConfigKey, darkThemeConfig.toString())
        }
    }

    fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        viewModelScope.launch {
            settings.putBoolean(useDynamicColorKey, useDynamicColor)
        }
    }

    fun updateWearTheme() {
        viewModelScope.launch {
            wearSettingsSync.updateWearTheme()
        }
    }
    companion object {
        const val brandKey = "brandKey"
        const val useDynamicColorKey = "useDynamicColorKey"
        const val darkThemeConfigKey = "darkThemeConfigKey"
    }
}

/**
 * Represents the settings which the user can edit within the app.
 */
data class UserEditableSettings(
    val brand: ThemeBrand,
    val useDynamicColor: Boolean,
    val darkThemeConfig: DarkThemeConfig,
)

enum class ThemeBrand {
    DEFAULT, ANDROID
}

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}


