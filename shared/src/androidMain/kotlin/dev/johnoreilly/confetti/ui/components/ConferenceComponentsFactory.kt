package dev.johnoreilly.confetti.ui.components

import android.content.Context
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import okhttp3.OkHttpClient
import java.util.ServiceLoader

/**
 * Builds the [ConferenceComponents] an app variant draws with, for a module the variant includes
 * at build time: `:livery`, in sduiDebug only, registers one in
 * `META-INF/services/dev.johnoreilly.confetti.ui.components.ConferenceComponentsFactory`. The
 * app's own code never refers to that module, so every other variant builds without it and draws
 * [NativeComponents].
 */
interface ConferenceComponentsFactory {
    /**
     * [options] are the variant's settings, as the app's BuildConfig has them (for Livery:
     * `forcedArm`, `templatesUrl`).
     */
    fun create(
        context: Context,
        okHttp: OkHttpClient,
        analytics: AnalyticsLogger,
        options: Map<String, String>,
    ): ConferenceComponents

    companion object {
        /** The factory the variant includes, or null (draw [NativeComponents]). */
        fun load(): ConferenceComponentsFactory? =
            ServiceLoader.load(ConferenceComponentsFactory::class.java, ConferenceComponentsFactory::class.java.classLoader)
                .firstOrNull()
    }
}
