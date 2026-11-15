package dev.johnoreilly.confetti.wear.sdui

import dev.johnoreilly.confetti.wear.ConfettiApplication
import org.koin.core.module.Module

/** The Wear sdui build's application: Confetti, with the Wear sdui module's bindings over the app's own. */
class SduiConfettiApplication : ConfettiApplication() {
    override val variantModules: List<Module> get() = listOf(wearSduiModule)
}
