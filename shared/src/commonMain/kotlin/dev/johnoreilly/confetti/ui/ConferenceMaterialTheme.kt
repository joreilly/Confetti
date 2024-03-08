package dev.johnoreilly.confetti.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.rememberDynamicColorScheme
import dev.johnoreilly.confetti.AppSettings
import org.koin.compose.koinInject

enum class DarkThemeConfig {
    FOLLOW_SYSTEM, LIGHT, DARK
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun ConferenceMaterialTheme(
    seedColorString: String?,
    content: @Composable () -> Unit
) {
    val appSettings = koinInject<AppSettings>()
    val darkThemeConfigString by appSettings.settings.getStringFlow("darkThemeConfigKey", DarkThemeConfig.FOLLOW_SYSTEM.toString())
        .collectAsState("FOLLOW_SYSTEM")
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
        seedColor,
        shouldUseDarkTheme
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