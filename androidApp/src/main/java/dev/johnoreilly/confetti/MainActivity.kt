@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package dev.johnoreilly.confetti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.adaptive.calculateDisplayFeatures
import dev.johnoreilly.confetti.settings.DarkThemeConfig
import dev.johnoreilly.confetti.settings.SettingsViewModel
import dev.johnoreilly.confetti.settings.ThemeBrand
import dev.johnoreilly.confetti.settings.UserEditableSettings
import dev.johnoreilly.confetti.ui.ConfettiApp
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingsViewModel: SettingsViewModel by viewModel()
        var userEditableSettings by mutableStateOf<UserEditableSettings?>(null)

        // Update the theme settings
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.userEditableSettings.collect {
                    userEditableSettings = it
                }
            }
        }


        // Turn off the decor fitting system windows, which allows us to handle insets,
        // including IME animations
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()

            val windowSizeClass = calculateWindowSizeClass(this)
            val displayFeatures = calculateDisplayFeatures(this)

            ConfettiTheme(
                darkTheme = shouldUseDarkTheme(userEditableSettings?.darkThemeConfig),
                androidTheme = shouldUseAndroidTheme(userEditableSettings?.brand),
                disableDynamicTheming = shouldDisableDynamicTheming(userEditableSettings?.useDynamicColor)
            ) {
                ConfettiBackground {
                    ConfettiApp(
                        navController = navController,
                        windowSizeClass = windowSizeClass,
                        displayFeatures = displayFeatures,
                    )
                }
            }
        }
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

@Composable
private fun shouldUseAndroidTheme(
    themeBrand: ThemeBrand?,
): Boolean = when (themeBrand) {
        ThemeBrand.DEFAULT,null -> false
        ThemeBrand.ANDROID -> true
}

@Composable
private fun shouldDisableDynamicTheming(
    useDynamicColor: Boolean?
): Boolean = useDynamicColor?.not() ?: true

