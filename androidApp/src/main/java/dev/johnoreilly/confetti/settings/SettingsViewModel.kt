@file:OptIn(ExperimentalCoroutinesApi::class)

package dev.johnoreilly.confetti.settings

import com.arkivanov.decompose.ComponentContext
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.DarkThemeConfig
import dev.johnoreilly.confetti.DeveloperSettings
import dev.johnoreilly.confetti.ThemeBrand
import dev.johnoreilly.confetti.UserEditableSettings
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.decompose.coroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val appSettings: AppSettings,
    private val authentication: Authentication,
) : SettingsComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private val settings = appSettings.settings

    override val developerSettings: StateFlow<DeveloperSettings?> = appSettings.developerModeFlow().flatMapLatest {
        if (!it) {
            flowOf(null)
        } else {
            authentication.currentUser.map { user ->
                DeveloperSettings(token = user?.token(false))
            }
        }
    }.stateIn(
        scope = coroutineScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    override val userEditableSettings: StateFlow<UserEditableSettings?> =
        combine(
            settings.getStringFlow(brandKey, ThemeBrand.DEFAULT.toString()),
            settings.getStringFlow(darkThemeConfigKey, DarkThemeConfig.FOLLOW_SYSTEM.toString()),
            settings.getBooleanFlow(useDynamicColorKey, false),
            appSettings.experimentalFeaturesEnabledFlow,
        ) { themeBrand, darkThemeConfig, useDynamicColor, useExperimentalFeatures ->
            UserEditableSettings(
                brand = ThemeBrand.valueOf(themeBrand),
                useExperimentalFeatures = useExperimentalFeatures,
                useDynamicColor = useDynamicColor,
                darkThemeConfig = DarkThemeConfig.valueOf(darkThemeConfig),
            )
        }.stateIn(
            scope = coroutineScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    override fun updateThemeBrand(themeBrand: ThemeBrand) {
        coroutineScope.launch {
            settings.putString(brandKey, themeBrand.toString())
        }
    }

    override fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        coroutineScope.launch {
            settings.putString(darkThemeConfigKey, darkThemeConfig.toString())
        }
    }

    override fun updateDynamicColorPreference(useDynamicColor: Boolean) {
        coroutineScope.launch {
            settings.putBoolean(useDynamicColorKey, useDynamicColor)
        }
    }

    override fun updateUseExperimentalFeatures(value: Boolean) {
        coroutineScope.launch {
            appSettings.setExperimentalFeaturesEnabled(value)
        }
    }


    override fun enableDeveloperMode() {
        coroutineScope.launch {
            appSettings.setDeveloperMode(true)
        }
    }

    companion object {
        const val brandKey = "brandKey"
        const val useDynamicColorKey = "useDynamicColorKey"
        const val darkThemeConfigKey = "darkThemeConfigKey"
    }
}
