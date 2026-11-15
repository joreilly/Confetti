package dev.johnoreilly.confetti.wear.sdui

import android.content.Context
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.livery.BuildConfig
import dev.johnoreilly.confetti.livery.FirebaseSduiArms
import dev.johnoreilly.confetti.livery.confettiLiveryStyling
import dev.johnoreilly.confetti.livery.liveryHttpClient
import dev.johnoreilly.confetti.ui.livery.LiveryStyling
import dev.johnoreilly.confetti.wear.components.NativeWearComponents
import dev.johnoreilly.confetti.wear.components.WearComponents
import dev.johnoreilly.confetti.wear.sdui.widget.BookmarkedNextSession
import dev.johnoreilly.confetti.wear.sdui.widget.NextSessionSource
import dev.johnoreilly.confetti.wear.sdui.widget.NextSessionWidget
import dev.johnoreilly.confetti.wear.tile.TileUpdater
import ee.schimke.livery.protocol.PlayerCapabilities
import ee.schimke.livery.protocol.Surfaces
import ee.schimke.livery.protocol.Target
import ee.schimke.livery.widgets.wear.LiveryWearWidget
import ee.schimke.livery.widgets.wear.WearWidgetSlot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * The Wear sdui build's Koin module, added after the app's own: it overrides the app's
 * `WearComponents` (Confetti's own composables) with the sdui experiment's, per conference series
 * as on the phone (Firebase's `sdui_<series>` arm), and the app's `TileUpdater` with one that also
 * redraws the next-session widgets. Livery is built on first use, by whichever needs it first: the
 * slots or a widget.
 */
val wearSduiModule = module {
    single<LiveryStyling> {
        confettiLiveryStyling(
            androidContext(), liveryHttpClient(get<OkHttpClient>()), get<AnalyticsLogger>(),
            templatesUrl = BuildConfig.TEMPLATES_URL,
            // A refreshed catalog downloads the widgets' templates too, for both containers.
            extraTargets = listOf(wearWidget(WearWidgetSlot.CONTAINER_SMALL), wearWidget(WearWidgetSlot.CONTAINER_LARGE)),
        )
    }
    single<WearComponents> {
        WearSduiComponents(
            arms = FirebaseSduiArms(forced = BuildConfig.FORCED_ARM.ifEmpty { null }),
            native = NativeWearComponents,
        ) {
            WearLiveryComponents(get(), native = NativeWearComponents)
        }
    }
    single<TileUpdater> { WidgetsAndTileUpdater(androidContext()) }
    single<NextSessionSource> { BookmarkedNextSession(get(), get(), get()) }
}

/** What a Glance Wear widget plays, as LiveryWearWidget asks for it: its profile, a baked dark theme, its container. */
private fun wearWidget(container: String) = PlayerCapabilities(
    Surfaces.WEAR_WIDGET, maxRcApiLevel = 7, profiles = setOf(LiveryWearWidget.PROFILE), livePush = false,
    themes = setOf(Target.baked(dark = true)), container = container,
)

/** The tile, and the next-session widgets, after a bookmark changes. */
private class WidgetsAndTileUpdater(private val context: Context) : TileUpdater() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun updateAll() {
        super.updateAll()
        scope.launch { NextSessionWidget.updateAll(context) }
    }
}
