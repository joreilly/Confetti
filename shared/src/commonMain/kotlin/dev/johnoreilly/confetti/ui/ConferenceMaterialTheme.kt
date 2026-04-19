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

@OptIn(ExperimentalStdlibApi::class, ExperimentalSettingsApi::class)
@Composable
fun ConferenceMaterialTheme(
    seedColorString: String?,
    content: @Composable () -> Unit,
) {
    // Previews (LocalInspectionMode = true) can't reach Koin — the compose-
    // ai-tools renderer installs a stub Application by default and skips the
    // consumer's onCreate, so Koin is never started. Short-circuit with the
    // settings defaults (dynamic color off, follow-system dark theme) so
    // previews render without an Application-level DI container.
    if (LocalInspectionMode.current) {
        ConferenceMaterialThemeWithDefaults(seedColorString, content)
        return
    }

    val appSettings = koinInject<AppSettings>()
    val useDynamicColor by appSettings.settings.getBooleanFlow("useDynamicColorKey", false)
        .collectAsState(false)

    if (!useDynamicColor) {
        val darkThemeConfigString by appSettings.settings.getStringFlow(
            "darkThemeConfigKey",
            DarkThemeConfig.FOLLOW_SYSTEM.toString()
        )
            .collectAsState(DarkThemeConfig.FOLLOW_SYSTEM.toString())
        val darkThemeConfig = DarkThemeConfig.valueOf(darkThemeConfigString)
        ApplyMaterialTheme(seedColorString, darkThemeConfig, content)
    } else {
        content()
    }
}

/**
 * Theme path used when there's no Koin container available (previews, tests).
 * Mirrors the `!useDynamicColor` branch of [ConferenceMaterialTheme] with
 * settings pinned to their defaults: dynamic color off, dark mode follows the
 * system.
 */
@Composable
private fun ConferenceMaterialThemeWithDefaults(
    seedColorString: String?,
    content: @Composable () -> Unit,
) {
    ApplyMaterialTheme(seedColorString, DarkThemeConfig.FOLLOW_SYSTEM, content)
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun ApplyMaterialTheme(
    seedColorString: String?,
    darkThemeConfig: DarkThemeConfig,
    content: @Composable () -> Unit,
) {
    val shouldUseDarkTheme = shouldUseDarkTheme(darkThemeConfig)

    var seedColor = Color(0xFF008000) // default if none set
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

@Composable
private fun shouldUseDarkTheme(
    darkThemeConfig: DarkThemeConfig?,
): Boolean = when (darkThemeConfig) {
    DarkThemeConfig.FOLLOW_SYSTEM, null -> isSystemInDarkTheme()
    DarkThemeConfig.LIGHT -> false
    DarkThemeConfig.DARK -> true
}