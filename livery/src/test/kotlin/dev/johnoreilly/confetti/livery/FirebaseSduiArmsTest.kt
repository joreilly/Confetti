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

    @Test fun theArmComesFromRemoteConfig() {
        val arms = FirebaseSduiArms(config = { mapOf("sdui_droidcon" to "native")[it] })
        assertEquals(SduiExperiment.NATIVE, arms.arm("droidcon"))
        assertEquals(SduiExperiment.LIVERY, arms.arm("kotlinconf"))
    }

    /** The kill switch draws every series native, whatever its arm. */
    @Test fun theKillSwitchTurnsLiveryOffEverywhere() {
        val values = mapOf(FirebaseSduiArms.KILL_SWITCH to "false", "sdui_droidcon" to "livery")
        val arms = FirebaseSduiArms(config = { values[it] })
        assertEquals(SduiExperiment.NATIVE, arms.arm("droidcon"))
        assertEquals(SduiExperiment.NATIVE, arms.arm("kotlinconf"))
    }

    /** An unreadable switch value keeps Livery on: only an explicit `false` turns it off. */
    @Test fun onlyAnExplicitFalseTurnsLiveryOff() {
        val arms = FirebaseSduiArms(config = { mapOf(FirebaseSduiArms.KILL_SWITCH to "maybe")[it] })
        assertEquals(SduiExperiment.LIVERY, arms.arm("droidcon"))
    }

    @Test fun aForcedArmWins() {
        assertEquals(SduiExperiment.NATIVE, FirebaseSduiArms(forced = SduiExperiment.NATIVE).arm("kotlinconf"))
    }

    /** Shaders on Android 13+ unless Remote Config turns them off; nothing turns them on below 13. */
    @Test fun shadersNeedAndroid13AndTheSwitch() {
        val none = FirebaseSduiArms.RemoteValues { null }
        val off = FirebaseSduiArms.RemoteValues { mapOf(FirebaseSduiArms.SHADERS_SWITCH to "false")[it] }
        val on = FirebaseSduiArms.RemoteValues { mapOf(FirebaseSduiArms.SHADERS_SWITCH to "true")[it] }
        assertEquals(true, shadersSupported(sdkInt = 33, config = none))
        assertEquals(false, shadersSupported(sdkInt = 33, config = off))
        assertEquals(false, shadersSupported(sdkInt = 32, config = on))
    }
}
