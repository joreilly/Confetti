@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package dev.johnoreilly.confetti

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.handleDeepLink
import dev.johnoreilly.confetti.account.signIn
import dev.johnoreilly.confetti.auth.Authentication
import dev.johnoreilly.confetti.decompose.DarkThemeConfig
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.decompose.ThemeBrand
import dev.johnoreilly.confetti.decompose.UserEditableSettings
import dev.johnoreilly.confetti.ui.App
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        var userEditableSettings by mutableStateOf<UserEditableSettings?>(null)
        val credentialManager: CredentialManager by inject()
        val authentication: Authentication by inject()


        val appComponent =
            handleDeepLink { uri ->
                val initialConferenceId = uri?.extractConferenceIdOrNull()
                val rootComponentContext = defaultComponentContext(discardSavedState = initialConferenceId != null)

//                val settingsComponent = SettingsComponent(
//                    componentContext = rootComponentContext.childContext("settings"),
//                    appSettings = appSettings,
//                    authentication = authentication,
//                )

                val appComponent = DefaultAppComponent(
                    componentContext = rootComponentContext.childContext("app"),
                    initialConferenceId = initialConferenceId,
                    onSignOut = {
                        lifecycleScope.launch {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        }
                    },
                    onSignIn = {
                        lifecycleScope.launch {
                            signIn(this@MainActivity, authentication)
                        }
                    }
                )
                appComponent
            } ?: return


        lifecycleScope.launch {
            authentication.currentUser
                .collect {
                    appComponent.setUser(it)
                }
        }

//        // Update the theme settings
//        lifecycleScope.launch {
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                appComponent.second.userEditableSettings.collect {
//                    userEditableSettings = it
//                }
//            }
//        }
//

        setContent {
            App(component = appComponent)
        }
    }

    /**
     * From a deep link like `https://confetti-app.dev/conference/devfeststockholm2023` extracts `devfeststockholm2023`.
     */
    private fun Uri.extractConferenceIdOrNull(): String? {
        if (host != "confetti-app.dev") return null
        val path = path ?: return null
        if (path.firstOrNull() != '/') return null
        val parts = path.substring(1).split('/')
        if (parts.size != 2) return null
        if (parts[0] != "conference") return null
        val conferenceId = parts[1]
        if (!conferenceId.all { it.isLetterOrDigit() }) return null
        return conferenceId
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
