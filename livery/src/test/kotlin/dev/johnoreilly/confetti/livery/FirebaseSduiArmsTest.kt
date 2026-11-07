package dev.johnoreilly.confetti.livery

import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseSduiArmsTest {
    /** Without Firebase (as before a fetch), each series draws livery, and keeps it. */
    @Test fun defaultArmIsLiveryAndIsKept() {
        val arms = FirebaseSduiArms()
        assertEquals(SduiExperiment.LIVERY, arms.arm("droidcon"))
        assertEquals(SduiExperiment.LIVERY, arms.arm("droidcon"))
    }

    @Test fun aForcedArmWins() {
        assertEquals(SduiExperiment.NATIVE, FirebaseSduiArms(forced = SduiExperiment.NATIVE).arm("kotlinconf"))
    }
}
