@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package dev.johnoreilly.confetti

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import dev.johnoreilly.confetti.splash.SplashReadyStatus
import dev.johnoreilly.confetti.ui.ConfettiApp
import dev.johnoreilly.confetti.ui.ConfettiTheme
import dev.johnoreilly.confetti.ui.component.ConfettiBackground
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    private val confettiRepository: ConfettiRepository by inject()
    private val splashReadyStatus: SplashReadyStatus by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (intent.data == null) { // If we're deep linking anywhere, don't show splash
            // Keep splash screen on until we get the first info on which conference we've chosen
            splashScreen.setKeepOnScreenCondition {
                !splashReadyStatus.isAppReadyToShow
            }
        }

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

        val defaultConferenceParameter = intent.data?.findConferenceParameter()
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
                        confettiRepository = confettiRepository,
                        defaultConferenceParameter = defaultConferenceParameter,
                    )
                }
            }
        }
    }

    /**
     * This relies on our deep link looking either like
     * `confetti://confetti/sessions/{conference}`
     * or
     * `confetti://confetti/speakers/{conference}/{speakerId}`
     * or more generally
     * `whatever/[sessions|speakers]/{conference}/whatever
     *
     * Should be doing some more general checks here or find a way to find what the original
     * URI template was like, so we can find what is in the position of {conference} for example.
     * Now as I see it we only get the already decoded Uri so I don't know how to get the ID in a
     * better way.
     */
    private fun Uri.findConferenceParameter(): String? {
        return pathSegments.also { Log.d("Stelios", "segmn$it") }
            .dropWhile<String?> { segment -> segment != "sessions" && segment != "speakers" }
            .drop(1) // Also drop the "sessions" or the "speakers" segment
            .firstOrNull()
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
    ThemeBrand.DEFAULT, null -> false
    ThemeBrand.ANDROID -> true
}

@Composable
private fun shouldDisableDynamicTheming(
    useDynamicColor: Boolean?
): Boolean = useDynamicColor?.not() ?: true

