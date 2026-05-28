package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import com.materialkolor.rememberDynamicColorScheme
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import org.koin.compose.koinInject

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ConferenceMaterialTheme(
    seedColorString: String?,
    useDynamicColor: Boolean = false,
    darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    content: @Composable () -> Unit,
) {
    if (useDynamicColor) {
        content()
        return
    }

    val shouldUseDarkTheme = shouldUseDarkTheme(darkThemeConfig)

    var seedColor = Color(0xFF008000)
    seedColorString?.let {
        try {
            seedColor = Color(seedColorString.hexToLong(HexFormat { number.prefix = "0x" }))
        } catch (e: Exception) {
            println(e)
        }
    }
    val colorScheme = rememberDynamicColorScheme(
        primary = seedColor,
        isDark = shouldUseDarkTheme,
        isAmoled = false,
    )

    MaterialTheme(colorScheme = colorScheme) {
        content()
    }
}

/**
 * Settings-aware wrapper around [ConferenceMaterialTheme]. Reads the user's
 * theme preferences from Koin-provided [AppSettings] and forwards them as
 * plain parameters, so the theme function itself stays free of DI.
 *
 * Falls back to defaults under [LocalInspectionMode] (previews, screenshot
 * tests) where Koin may not be initialised.
 */
@OptIn(ExperimentalSettingsApi::class)
@Composable
fun ConferenceMaterialThemeFromSettings(
    seedColorString: String?,
    content: @Composable () -> Unit,
) {
    if (LocalInspectionMode.current) {
        ConferenceMaterialTheme(seedColorString, content = content)
        return
    }

    val appSettings = koinInject<AppSettings>()
    val useDynamicColor by appSettings.settings.getBooleanFlow("useDynamicColorKey", false)
        .collectAsState(false)
    val darkThemeConfigString by appSettings.settings.getStringFlow(
        "darkThemeConfigKey",
        DarkThemeConfig.FOLLOW_SYSTEM.toString()
    ).collectAsState(DarkThemeConfig.FOLLOW_SYSTEM.toString())

    ConferenceMaterialTheme(
        seedColorString = seedColorString,
        useDynamicColor = useDynamicColor,
        darkThemeConfig = DarkThemeConfig.valueOf(darkThemeConfigString),
        content = content,
    )
}

@Composable
private fun shouldUseDarkTheme(
    darkThemeConfig: DarkThemeConfig?,
): Boolean = when (darkThemeConfig) {
    DarkThemeConfig.FOLLOW_SYSTEM, null -> isSystemInDarkTheme()
    DarkThemeConfig.LIGHT -> false
    DarkThemeConfig.DARK -> true
}
