package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.ui.components.ConferenceComponentsFactory
import org.junit.Assert.assertEquals
import org.junit.Test

/** Only the sduiDebug build type includes :livery, so only it finds a components factory. */
class ConferenceComponentsVariantTest {
    @Test fun onlySduiDebugIncludesLivery() {
        assertEquals(BuildConfig.BUILD_TYPE == "sduiDebug", ConferenceComponentsFactory.load() != null)
    }
}
