@file:OptIn(KoinInternalApi::class)

package dev.johnoreilly.confetti.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.firebase.FirebaseApp
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.analytics.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.getViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {
    lateinit var navController: NavHostController
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            navController = rememberSwipeDismissableNavController()

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

                ConfettiApp(navController, viewModel)

                LaunchedEffect(Unit) {
                    logNavigationEvents()
                }
            }
        }
    }

    private suspend fun logNavigationEvents() {
        if (isFirebaseInstalled) {
            navController.currentBackStackEntryFlow.collect { navEntry ->
                analyticsLogger.logNavigationEvent(navEntry)
            }
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