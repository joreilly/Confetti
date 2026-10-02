package dev.johnoreilly.confetti

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.observable.makeObservable
import com.russhwolf.settings.coroutines.toFlowSettings
import dev.johnoreilly.confetti.ui.sessions.toggleTrack
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Properties
import org.junit.Test
import kotlin.test.assertEquals

class TrackSelectionTest {
    private val tracks = listOf("droidCon", "swiftCon", "agentic codingCon")

    @Test
    fun tracksAreCumulative() {
        val selection = emptySet<String>()
            .toggleTrack("droidCon", tracks)
            .toggleTrack("swiftCon", tracks)
        assertEquals(setOf("droidCon", "swiftCon"), selection)
    }

    @Test
    fun tappingSelectedTrackRemovesIt() {
        assertEquals(setOf("swiftCon"), setOf("droidCon", "swiftCon").toggleTrack("droidCon", tracks))
    }

    @Test
    fun selectingEveryTrackCollapsesToAll() {
        assertEquals(emptySet(), setOf("droidCon", "swiftCon").toggleTrack("agentic codingCon", tracks))
    }

    @Test
    fun selectionPersistedPerConference() = runBlocking {
        val appSettings = AppSettings(PropertiesSettings(Properties()).makeObservable().toFlowSettings())

        appSettings.setSelectedTracks("nextappcon2026", setOf("droidCon", "agentic codingCon"))

        assertEquals(setOf("droidCon", "agentic codingCon"), appSettings.selectedTracksFlow("nextappcon2026").first())
        assertEquals(emptySet(), appSettings.selectedTracksFlow("kotlinconf2026").first())

        appSettings.setSelectedTracks("nextappcon2026", emptySet())
        assertEquals(emptySet(), appSettings.selectedTracksFlow("nextappcon2026").first())
    }
}
