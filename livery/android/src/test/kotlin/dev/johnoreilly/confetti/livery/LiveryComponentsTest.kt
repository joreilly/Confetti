package dev.johnoreilly.confetti.livery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import dev.johnoreilly.confetti.analytics.AnalyticsEvent
import dev.johnoreilly.confetti.analytics.AnalyticsLogger
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.preview.johnOreillySpeaker
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.components.SessionHeaderModel
import dev.johnoreilly.confetti.ui.components.SpeakerHeaderModel
import dev.johnoreilly.confetti.ui.livery.toSeed
import dev.johnoreilly.confetti.ui.icons.AccessTime
import dev.johnoreilly.confetti.ui.icons.ConfettiIcons
import dev.johnoreilly.confetti.ui.livery.withHeadingFamily
import dev.johnoreilly.confetti.ui.livery.withThemePack
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.protocol.NamedValue
import ee.schimke.livery.protocol.ThemeRoles
import dev.johnoreilly.confetti.ui.components.ConferenceComponents
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import dev.johnoreilly.confetti.sdui.sduiModule
import dev.johnoreilly.confetti.ui.components.NativeComponents
import dev.johnoreilly.confetti.ui.components.SessionCardModel
import dev.johnoreilly.confetti.ui.livery.ConferenceSlot
import dev.johnoreilly.confetti.ui.livery.LiveryComponents
import dev.johnoreilly.confetti.ui.livery.toSessionCard
import kotlinx.datetime.LocalDateTime
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The conference components as the app's screens use them: the livery arm draws the bundled
 * catalogs (the base one where a conference has none), the native arm Confetti's own rows, both
 * tracked as the experiment's events, and the native arm never builds Livery.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LiveryComponentsTest {
    @get:Rule val compose = createComposeRule()

    private val live = LocalDateTime.parse("2023-04-13T14:10")
    private val logged = mutableListOf<AnalyticsEvent>()
    private val analytics = object : AnalyticsLogger {
        override fun logEvent(event: AnalyticsEvent) { synchronized(logged) { logged += event } }
    }

    private fun livery() = LiveryComponents(
        confettiLiveryStyling(ApplicationProvider.getApplicationContext(), liveryHttpClient(OkHttpClient()), analytics, refresh = false),
        TrackedNativeComponents(SduiTracking(analytics), SduiExperiment.LIVERY),
    )

    /** Added after shared's binding, [sduiModule] overrides it with the sdui experiment's components. */
    @Test fun liveryModuleOverridesTheNativeBinding() {
        val koin = koinApplication {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(
                module {
                    single<ConferenceComponents> { NativeComponents() }
                    single { OkHttpClient() }
                    single<AnalyticsLogger> { analytics }
                },
                sduiModule,
            )
        }.koin
        assertTrue(koin.get<ConferenceComponents>() is SduiComponents)
    }

    /** Remote Config's kill switch: the app's real components, with Livery never built. */
    @Test fun theKillSwitchNeverBuildsLivery() {
        val off = FirebaseSduiArms(config = { mapOf(FirebaseSduiArms.KILL_SWITCH to "false")[it] })
        val components = confettiComponents(
            ApplicationProvider.getApplicationContext(), OkHttpClient(), analytics, arms = off,
        )
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    for (conference in listOf("kotlinconf2026", "droidconlondon2025", "fosdem2025")) {
                        components.SessionCard(card(conference))
                    }
                }
            }
        }
        compose.waitForIdle()
        assertEquals(3, nativeRows())
        assertEquals(0, compose.onAllNodes(liverySlot).fetchSemanticsNodes().size)
        assertFalse(components.liveryStarted)
    }

    @Test fun nativeArmNeverBuildsLivery() {
        val components = SduiComponents(
            arms = { SduiExperiment.NATIVE },
            native = TrackedNativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE),
            livery = { error("the native arm must not build Livery") },
        )
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    for (conference in listOf("kotlinconf2026", "droidconlondon2025")) components.SessionCard(card(conference))
                    components.Promo("home.feed", "droidconlondon2026", Modifier.fillMaxWidth())
                }
            }
        }
        compose.waitForIdle()
        assertEquals(2, nativeRows())
        compose.onAllNodesWithText(sessionDetails.title)[0].performClick()
        compose.waitForIdle()
        assertFalse(components.liveryStarted)
        val events = synchronized(logged) { logged.toList() }
        assertEquals(2, events.count { it.id == "livery_impression" })
        val tap = events.single { it.id == "livery_action" }
        assertEquals("open", tap.properties["action"])
        assertEquals(SduiExperiment.NATIVE, tap.properties["exp_${SduiExperiment.ID}"])
        assertEquals(SduiExperiment.NATIVE_DESIGN, tap.properties["design_id"])
    }

    /** With no catalog yet, the livery arm builds Livery and draws Confetti's own rows. */
    /** Each conference draws its series' catalog, and one without a series (here Fosdem) the base. */
    @Test fun liveryArmDrawsEachConferencesCatalog() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    components.SessionCard(card("kotlinconf2026"))
                    components.SessionCard(card("droidconlondon2025"))
                    components.SessionCard(card("fosdem2025"))
                }
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 3 }
        assertTrue(components.liveryStarted)
        assertEquals(0, nativeRows())
        val tags = compose.onAllNodes(liverySlot).fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag] }
        assertEquals(listOf("kotlinconf-session", "droidcon-session", "confetti-session"), tags.map { it.substringAfterLast(':') })
        // The templates' impressions, tagged with the arm, as the native arm's rows are.
        compose.waitUntil(10_000) {
            synchronized(logged) { logged.filter { it.id == "livery_impression" }.map { it.properties["design_id"] }.toSet() } ==
                setOf("kotlinconf-session", "droidcon-session", "confetti-session")
        }
        val impression = synchronized(logged) { logged.first { it.id == "livery_impression" } }
        assertEquals(SduiExperiment.LIVERY, impression.properties["exp_${SduiExperiment.ID}"])
    }

    /**
     * A design's speaker names are each their own tap target: tapping the second of two speakers
     * opens that speaker (`openSpeaker` with its `id`), not the session and not the first speaker.
     */
    @Test fun tappingASpeakerInADesignOpensThatSpeaker() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        val speakers = mutableListOf<String>()
        val sessions = mutableListOf<String>()
        val card = card("droidconlondon2025").let {
            SessionCardModel(
                it.conference, it.session, it.now, false, true, null, emptyList(),
                sessionSelected = { id -> sessions += id }, addBookmark = {}, removeBookmark = {}, onNavigateToSignIn = {},
                speakerSelected = { id -> speakers += id },
            )
        }
        compose.setContent { MaterialTheme(colorScheme = lightColorScheme()) { components.SessionCard(card) } }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 1 }
        val second = sessionDetails.speakers[1].sessionSpeakerDetails
        val name = hasText(second.name) and hasAnyAncestor(liverySlot)
        compose.waitUntil(10_000) { compose.onAllNodes(name).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(name).performClick()
        compose.waitUntil(10_000) { speakers.isNotEmpty() }
        assertEquals(listOf(second.id), speakers)
        assertEquals(emptyList<String>(), sessions)
    }

    /** A break draws its conference's break design, and the base catalog's where the conference has none. */
    @Test fun breaksDrawTheirConferencesBreakDesign() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        val coffee = sessionDetails.copy(id = "break-1", type = "break", title = "Coffee break", speakers = emptyList())
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    components.SessionCard(card("kotlinconf2026", coffee))
                    components.SessionCard(card("fosdem2025", coffee))
                }
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 2 }
        val tags = compose.onAllNodes(liverySlot).fetchSemanticsNodes().map { it.config[SemanticsProperties.TestTag].substringAfterLast(':') }
        assertEquals(listOf("kotlinconf-break", "confetti-break"), tags)
    }

    /** The top bar's title draws its conference's title design, with the text it was given. */
    @Test fun theTopBarTitleDrawsTheConferencesTitleDesign() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        compose.setContent { MaterialTheme(colorScheme = lightColorScheme()) { components.TopBarTitle("droidconlondon2025", "droidcon London 2025") } }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 1 }
        assertEquals("droidcon-title", compose.onAllNodes(liverySlot).fetchSemanticsNodes().single().config[SemanticsProperties.TestTag].substringAfterLast(':'))
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("droidcon London 2025") and hasAnyAncestor(liverySlot)).fetchSemanticsNodes().isNotEmpty() }
    }

    /**
     * A slot Confetti draws nothing for (the header) never counts a native impression: native
     * impressions match the native rows on screen, whether or not the catalog has templates.
     */
    @Test fun anEmptyFallbackCountsNoImpression() {
        val styling = confettiLiveryStyling(ApplicationProvider.getApplicationContext(), liveryHttpClient(OkHttpClient()), analytics, refresh = false)
        compose.setContent {
            Column {
                ConferenceSlot(styling, "droidconlondon2025", ConferenceCard(name = "droidcon London", dates = "29-30 Oct"), fallback = null)
                ConferenceSlot(styling, "droidconlondon2025", sessionDetails.toSessionCard(live, isBookmarked = false), identity = "s1") {
                    BasicText("native row")
                }
            }
        }
        // Either the session's template or its native row, once the slot has planned.
        compose.waitUntil(10_000) { synchronized(logged) { logged.any { it.id == "livery_impression" } } }
        Thread.sleep(200)
        compose.waitForIdle()
        val nativeRows = compose.onAllNodesWithText("native row").fetchSemanticsNodes().size
        val nativeImpressions = synchronized(logged) {
            logged.count { it.id == "livery_impression" && it.properties["design_id"] == SduiExperiment.NATIVE_DESIGN }
        }
        assertEquals(nativeRows, nativeImpressions)
    }

    /** A row a lazy list reuses for another session reports that session's impression too. */
    @Test fun anImpressionPerSessionShown() {
        val native = TrackedNativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE)
        var session by mutableStateOf(sessionDetails)
        compose.setContent { native.SessionCard(card("kotlinconf2026", session)) }
        fun impressions() = synchronized(logged) { logged.count { it.id == "livery_impression" } }
        compose.waitUntil(10_000) { impressions() == 1 }
        session = sessionDetails.copy(id = "another-session", title = "Another session")
        compose.waitForIdle()
        compose.waitUntil(10_000) { impressions() == 2 }
    }

    /** A signed-out bookmark tap counts, as a design's `bookmark` action does. */
    @Test fun signedOutBookmarkTapsCount() {
        val native = TrackedNativeComponents(SduiTracking(analytics), SduiExperiment.NATIVE)
        val signedOut = card("kotlinconf2026").let {
            SessionCardModel(it.conference, it.session, it.now, false, false, null, emptyList(), {}, {}, {}, {})
        }
        compose.setContent { MaterialTheme { native.SessionCard(signedOut) } }
        compose.onNodeWithContentDescription("add bookmark").performClick()
        compose.waitForIdle()
        val tap = synchronized(logged) { logged.single { it.id == "livery_action" } }
        assertEquals("bookmark", tap.properties["action"])
    }

    /**
     * A speaker page's header draws the base catalog's design, and a link tapped in it opens that
     * link; a conference whose catalog has no header design (KotlinConf, for now) draws Confetti's.
     */
    @Test fun speakerHeadersDrawTheBaseDesign() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        val opened = mutableListOf<String>()
        fun header(conference: String) = SpeakerHeaderModel(conference, johnOreillySpeaker, onPhotoClick = {}, onSocialLinkClicked = { opened += it })
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    components.SpeakerHeader(header("fosdem2025"))
                    components.SpeakerHeader(header("kotlinconf2026"))
                }
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 1 }
        assertEquals("confetti-speaker-header", compose.onAllNodes(liverySlot).fetchSemanticsNodes().single().config[SemanticsProperties.TestTag].substringAfterLast(':'))
        // KotlinConf's is Confetti's own header, its name outside any template.
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasText(johnOreillySpeaker.name) and !hasAnyAncestor(liverySlot)).fetchSemanticsNodes().size == 1
        }
        val github = hasText("GitHub") and hasAnyAncestor(liverySlot)
        compose.waitUntil(10_000) { compose.onAllNodes(github).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(github).performClick()
        compose.waitUntil(10_000) { opened.isNotEmpty() }
        assertEquals(listOf("https://github.com/joreilly"), opened)
    }

    /** A session page's header draws the base catalog's design, its room opening the venue. */
    @Test fun sessionHeadersDrawTheBaseDesign() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        var venue = 0
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                components.SessionHeader(SessionHeaderModel("fosdem2025", sessionDetails, onRoomClick = { venue++ }))
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 1 }
        assertEquals("confetti-session-header", compose.onAllNodes(liverySlot).fetchSemanticsNodes().single().config[SemanticsProperties.TestTag].substringAfterLast(':'))
        val room = hasText(sessionDetails.room!!.name) and hasAnyAncestor(liverySlot)
        compose.waitUntil(10_000) { compose.onAllNodes(room).fetchSemanticsNodes().isNotEmpty() }
        compose.onNode(room).performClick()
        compose.waitUntil(10_000) { venue == 1 }
    }

    /**
     * A conference with a catalog of its own colours Confetti's own UI from its theme pack; any
     * other keeps the colour the server gives it.
     */
    @Test fun theConferenceThemeComesFromItsThemePack() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        var kotlinconf: Color? = null
        var fosdem: Color? = null
        compose.setContent {
            // Inspection mode: the theme's defaults (light), without the Koin settings.
            CompositionLocalProvider(LocalInspectionMode provides true) {
                components.ConferenceTheme("kotlinconf2026", "0xFF008000") { kotlinconf = MaterialTheme.colorScheme.primary }
                components.ConferenceTheme("fosdem2025", "0xFF008000") { fosdem = MaterialTheme.colorScheme.primary }
            }
        }
        compose.waitUntil(10_000) { kotlinconf == Color(0xFF6B3CF0) }
        // Seeded from green, not Confetti's purple or KotlinConf's.
        val seeded = requireNotNull(fosdem)
        assertTrue("$seeded", seeded.green > seeded.red && seeded.green > seeded.blue)
    }

    @Test fun aThemePackLaysOnlyPairedRolesOverTheScheme() {
        val base = lightColorScheme()
        val pack = Bindings(mapOf(
            ThemeRoles.PRIMARY to NamedValue.Color(0xFF6B3CF0), ThemeRoles.ON_PRIMARY to NamedValue.Color(0xFFFFFFFF),
            ThemeRoles.SURFACE to NamedValue.Color(0xFFEDDEE8), ThemeRoles.SECONDARY to NamedValue.Color(0xFFB10FC9),
            ThemeRoles.ON_SURFACE to NamedValue.Color(0xFF19191C),
        ))
        val branded = base.withThemePack(pack)
        assertEquals(Color(0xFF6B3CF0), branded.primary)
        assertEquals(Color(0xFF19191C), branded.onBackground)
        // A pack's surface is a card's (what toLiveryTheme sends it from), not the page's; and its
        // secondary has no onSecondary, so the scheme's stays.
        assertEquals(Color(0xFFEDDEE8), branded.surfaceContainerHigh)
        assertEquals(base.surface, branded.surface)
        assertEquals(base.secondary, branded.secondary)
        assertEquals("0xFF6B3CF0", Color(0xFF6B3CF0).toSeed())
    }

    /** A section header draws the base catalog's design; a conference with none draws Confetti's. */
    @Test fun sectionHeadersDrawTheBaseDesign() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Column {
                    components.SectionHeader("fosdem2025", "10:30", ConfettiIcons.AccessTime)
                    components.SectionHeader("kotlinconf2026", "11:00", ConfettiIcons.AccessTime)
                }
            }
        }
        compose.waitUntil(10_000) { compose.onAllNodes(liverySlot).fetchSemanticsNodes().size == 1 }
        assertEquals("confetti-section", compose.onAllNodes(liverySlot).fetchSemanticsNodes().single().config[SemanticsProperties.TestTag].substringAfterLast(':'))
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("10:30") and hasAnyAncestor(liverySlot)).fetchSemanticsNodes().isNotEmpty() }
        compose.waitUntil(10_000) { compose.onAllNodes(hasText("11:00") and !hasAnyAncestor(liverySlot)).fetchSemanticsNodes().size == 1 }
    }

    /** A conference whose catalog ships a face sets its headings in it; the base catalog ships none. */
    @Test fun headingsTakeTheSeriesFace() {
        val components = SduiComponents(arms = { SduiExperiment.LIVERY }, native = NativeComponents(), livery = ::livery)
        var kotlinconf: Typography? = null
        var fosdem: Typography? = null
        compose.setContent {
            CompositionLocalProvider(LocalInspectionMode provides true) {
                components.ConferenceTheme("kotlinconf2026", null) { kotlinconf = MaterialTheme.typography }
                components.ConferenceTheme("fosdem2025", null) { fosdem = MaterialTheme.typography }
            }
        }
        compose.waitUntil(10_000) { kotlinconf?.headlineMedium?.fontFamily != null }
        val type = requireNotNull(kotlinconf)
        // Headings only: body text stays the platform's.
        assertEquals(type.headlineMedium.fontFamily, type.titleSmall.fontFamily)
        assertEquals(Typography().bodyLarge.fontFamily, type.bodyLarge.fontFamily)
        assertEquals(Typography().headlineMedium.fontFamily, requireNotNull(fosdem).headlineMedium.fontFamily)
    }

    @Test fun aHeadingFamilyLeavesSizesAndBodyTextAlone() {
        val base = Typography()
        val type = base.withHeadingFamily(FontFamily.Monospace)
        assertEquals(FontFamily.Monospace, type.displayLarge.fontFamily)
        assertEquals(FontFamily.Monospace, type.titleSmall.fontFamily)
        assertEquals(base.titleSmall.fontSize, type.titleSmall.fontSize)
        assertEquals(base.bodyMedium, type.bodyMedium)
        assertEquals(base.labelSmall, type.labelSmall)
    }

    private fun card(conference: String, session: SessionDetails = sessionDetails) = SessionCardModel(
        conference = conference, session = session, now = live, isBookmarked = false, isLoggedIn = true,
        trackColor = null, tags = emptyList(), sessionSelected = {}, addBookmark = {}, removeBookmark = {}, onNavigateToSignIn = {},
    )

    /** Confetti's own rows: the title as text outside any template. */
    private fun nativeRows() =
        compose.onAllNodes(hasText(sessionDetails.title) and !hasAnyAncestor(liverySlot)).fetchSemanticsNodes().size

    /** A node a Livery template drew: LiveryHost tags each player `livery:<placement or contract>:<design>`. */
    private val liverySlot = SemanticsMatcher("is a Livery slot") {
        it.config.getOrNull(SemanticsProperties.TestTag)?.startsWith("livery:") == true
    }
}
