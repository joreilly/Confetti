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
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.handleDeepLink
import dev.johnoreilly.confetti.account.SignInProcess
import dev.johnoreilly.confetti.decompose.AppComponent
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
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        var userEditableSettings by mutableStateOf<UserEditableSettings?>(null)
        val signInProcess: SignInProcess by inject()

        val appComponent =
            handleDeepLink { uri ->
                val deepLinkInfo = uri?.extractDeepLinkInfoOrNull()
                val rootComponentContext = defaultComponentContext(discardSavedState = deepLinkInfo != null)

                val appComponent = DefaultAppComponent(
                    componentContext = rootComponentContext.childContext("app"),
                    initialConferenceId = deepLinkInfo?.conferenceId,
                    initialSessionId = deepLinkInfo?.sessionId,
                    onSignOut = {
                        lifecycleScope.launch {
                            signInProcess.signOut()
                        }
                    },
                    onSignIn = {
                        lifecycleScope.launch {
                            signInProcess.signIn(this@MainActivity)
                        }
                    }
                )
                appComponent
            } ?: return

        splashScreen.setKeepOnScreenCondition {
            appComponent.stack.value.active.instance is AppComponent.Child.Loading
        }

        setContent {
            App(component = appComponent)
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
    ThemeBrand.DEFAULT, null -> false
    ThemeBrand.ANDROID -> true
}

@Composable
private fun shouldDisableDynamicTheming(
    useDynamicColor: Boolean?
): Boolean = useDynamicColor?.not() ?: true
