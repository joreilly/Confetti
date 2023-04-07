@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.horologist.data.apphelper.AppHelperNodeStatus
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.ui.colorScheme
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    val appSettings: AppSettings,
    val wearSettingsSync: WearSettingsSync,
    val applicationContext: Context
) : ViewModel() {

    private val settings = appSettings.settings

    private val wearStatusFlow =
        combine(
            wearSettingsSync.wearNodes,
            wearSettingsSync.settingsFlow,
        ) { wearNodes, wearSettings ->
            buildWearStatus(wearNodes, wearSettings)
        }

    val userEditableSettings: StateFlow<UserEditableSettings?> =
        combine(
            settings.getStringFlow(brandKey, ThemeBrand.DEFAULT.toString()),
            settings.getStringFlow(darkThemeConfigKey, DarkThemeConfig.FOLLOW_SYSTEM.toString()),
            settings.getBooleanFlow(useDynamicColorKey, false),
            appSettings.experimentalFeaturesEnabledFlow,
            wearStatusFlow,
            ) { themeBrand, darkThemeConfig, useDynamicColor, useExperimentalFeatures, wearStatus ->
            UserEditableSettings(
                brand = ThemeBrand.valueOf(themeBrand),
                useExperimentalFeatures = useExperimentalFeatures,
                useDynamicColor = useDynamicColor,
                darkThemeConfig = DarkThemeConfig.valueOf(darkThemeConfig),
                wearStatus = wearStatus,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    private fun buildWearStatus(
        wearNodes: List<AppHelperNodeStatus>,
        wearSettings: WearSettings
    ): WearStatus {
        return if (wearNodes.isEmpty()) {
            WearStatus.Unavailable
        } else if (wearNodes.find { it.isAppInstalled } == null) {
            WearStatus.NotInstalled(wearNodes.first().id)
        } else {
            WearStatus.Paired(wearSettings)
        }
    }

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

    fun updateUseExperimentalFeatures(value: Boolean) {
        viewModelScope.launch {
            appSettings.setExperimentalFeaturesEnabled(value)
        }
    }

    fun updateWearTheme(active: Boolean) {
        viewModelScope.launch {
            val settings = userEditableSettings.first()

            val theme = colorScheme(
                androidTheme = settings?.brand == ThemeBrand.ANDROID,
                darkTheme = true,
                disableDynamicTheming = settings?.useDynamicColor ?: false,
                context = applicationContext
            )

            if (active) {
                wearSettingsSync.updateWearTheme(theme)
            } else {
                wearSettingsSync.clearWearTheme()
            }
        }
    }

    fun installOnWatch(nodeId: String) {
        viewModelScope.launch {
            wearSettingsSync.installOnWearNode(nodeId)
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
