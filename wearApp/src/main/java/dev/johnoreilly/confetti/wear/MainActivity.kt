@file:OptIn(KoinInternalApi::class, KoinExperimentalAPI::class)

package dev.johnoreilly.confetti.wear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.google.firebase.FirebaseApp
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.wear.navigation.DefaultWearAppComponent
import dev.johnoreilly.confetti.wear.navigation.NavigationHelper.logNavigationEvent
import dev.johnoreilly.confetti.wear.navigation.WearAppComponent
import dev.johnoreilly.confetti.wear.tile.TileSync
import dev.johnoreilly.confetti.wear.ui.ConfettiApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.KoinAndroidContext
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinInternalApi
import kotlin.time.Duration.Companion.seconds

class MainActivity : ComponentActivity() {
    internal lateinit var appComponent: WearAppComponent
    private val analyticsLogger: AnalyticsLogger by inject()

    private val tileSync: TileSync by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Schedule an update of tiles if user keeps apps open for 5 seconds
        lifecycleScope.launch {
            delay(5.seconds)

            tileSync.trackInstalledTiles(this@MainActivity)
        }

        appComponent =
            DefaultWearAppComponent(
                componentContext = defaultComponentContext(),
                intent = intent
            )

        setContent {
            KoinAndroidContext {
                splashScreen.setKeepOnScreenCondition {
                    appComponent.isWaitingOnThemeOrData
                }

                ConfettiApp(
                    component = appComponent,
                )

                LaunchedEffect(Unit) {
                    logNavigationEvents()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val handled = appComponent.handleDeeplink(intent)

        if (handled) {
            setIntent(intent)
        }
    }

    private fun logNavigationEvents() {
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