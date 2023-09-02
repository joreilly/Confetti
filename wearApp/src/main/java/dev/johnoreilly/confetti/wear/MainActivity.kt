@file:OptIn(KoinInternalApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import com.arkivanov.decompose.defaultComponentContext
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.decompose.DefaultAppComponent
import dev.johnoreilly.confetti.wear.decompose.DefaultWearAppComponent
import dev.johnoreilly.confetti.wear.decompose.WearAppComponent
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.getViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {
    private val analyticsLogger: AnalyticsLogger by inject()

    val appComponent: WearAppComponent =
        DefaultWearAppComponent(
            componentContext = defaultComponentContext(),
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            // TODO https://github.com/InsertKoinIO/koin/issues/1557
            CompositionLocalProvider(
                LocalKoinScope provides KoinPlatformTools.defaultContext()
                    .get().scopeRegistry.rootScope,
                LocalKoinApplication provides KoinPlatformTools.defaultContext().get()
            ) {
                val viewModel: WearAppViewModel = getViewModel()

                splashScreen.setKeepOnScreenCondition {
                    viewModel.waitingOnThemeOrData
                }

                ConfettiApp(
                    component = appComponent,
                    viewModel
                )

                LaunchedEffect(Unit) {
                    logNavigationEvents()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) {
            appComponent.handleDeeplink(intent)
        }

        setIntent(intent)
    }

    private suspend fun logNavigationEvents() {
        if (isFirebaseInstalled) {
//            navController.currentBackStackEntryFlow.collect { navEntry ->
//                analyticsLogger.logNavigationEvent(navEntry)
//            }
        }
    }

    private val isFirebaseInstalled: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (ise: IllegalStateException) {
            false
        }
}