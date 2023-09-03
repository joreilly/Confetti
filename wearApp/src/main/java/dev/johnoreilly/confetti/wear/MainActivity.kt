@file:OptIn(KoinInternalApi::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.arkivanov.decompose.defaultComponentContext
import com.google.firebase.FirebaseApp
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.wear.navigation.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.wear.navigation.DefaultWearAppComponent
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.getViewModel
import org.koin.compose.LocalKoinApplication
import org.koin.compose.LocalKoinScope
import org.koin.core.annotation.KoinInternalApi
import org.koin.mp.KoinPlatformTools

class MainActivity : ComponentActivity() {
    private lateinit var appComponent: WearAppComponent
    private val analyticsLogger: AnalyticsLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        appComponent =
            DefaultWearAppComponent(
                componentContext = defaultComponentContext(),
            )

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
            appComponent.stack.subscribe {
                analyticsLogger.logNavigationEvent(it.active.configuration)
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