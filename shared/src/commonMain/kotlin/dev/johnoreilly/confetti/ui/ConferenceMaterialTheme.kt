package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import com.russhwolf.settings.ExperimentalSettingsApi
import dev.johnoreilly.confetti.AppSettings
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import org.koin.compose.koinInject

@OptIn(ExperimentalStdlibApi::class, ExperimentalSettingsApi::class)
@Composable
fun ConferenceMaterialTheme(
    seedColorString: String?,
    content: @Composable () -> Unit
) {
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
            isAmoled = false
        )

        MaterialTheme(colorScheme = colorScheme) {
            content()
        }
    } else {
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