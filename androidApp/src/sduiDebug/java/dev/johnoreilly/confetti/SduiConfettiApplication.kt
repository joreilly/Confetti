package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.sdui.sduiModule

/** The sdui build's application: Confetti, with the sdui module's bindings over the app's own. */
class SduiConfettiApplication : ConfettiApplication() {
    override val variantModules get() = listOf(sduiModule)
}
