@file:OptIn(ExperimentalSettingsApi::class)

package dev.johnoreilly.confetti.settings

import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.google.android.horologist.data.apphelper.AppHelperNodeStatus
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.DarkThemeConfig
import dev.johnoreilly.confetti.DeveloperSettings
import dev.johnoreilly.confetti.decompose.SettingsComponent
import dev.johnoreilly.confetti.ThemeBrand
import dev.johnoreilly.confetti.UserEditableSettings
import dev.johnoreilly.confetti.WearStatus
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.coroutineScope
import dev.johnoreilly.confetti.ui.colorScheme
import dev.johnoreilly.confetti.wear.WearSettingsSync
import dev.johnoreilly.confetti.wear.proto.WearSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val appSettings: AppSettings,
    private val wearSettingsSync: WearSettingsSync,
    private val applicationContext: Context,
    private val authentication: Authentication,
) : SettingsComponent, ComponentContext by componentContext {

    private val coroutineScope = coroutineScope()
    private val settings = appSettings.settings

    private val wearStatusFlow =
        combine(
            wearSettingsSync.wearNodes,
            wearSettingsSync.settingsFlow,
        ) { wearNodes, wearSettings ->
            buildWearStatus(wearNodes, wearSettings)
        }

    override val developerSettings: StateFlow<DeveloperSettings?> = appSettings.developerModeFlow().flatMapLatest {
        if (!it) {
            flowOf(null)
        } else {
            authentication.currentUser.map {
                DeveloperSettings(token = it?.token(false))
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
            scope = coroutineScope,
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

    override fun updateWearTheme(active: Boolean) {
        coroutineScope.launch {
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

    override fun installOnWatch(nodeId: String) {
        coroutineScope.launch {
            wearSettingsSync.installOnWearNode(nodeId)
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
