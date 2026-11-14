package dev.johnoreilly.confetti.livery.benchmark

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import dev.johnoreilly.confetti.fragment.SessionDetails
import dev.johnoreilly.confetti.livery.SessionCard
import dev.johnoreilly.confetti.livery.SessionSpeaker
import dev.johnoreilly.confetti.preview.sessionDetails
import dev.johnoreilly.confetti.ui.sessions.SessionItemView
import ee.schimke.livery.client.ActionRouter
import ee.schimke.livery.client.BindResult
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.client.Clock
import ee.schimke.livery.client.ContractBinder
import ee.schimke.livery.client.FetchResult
import ee.schimke.livery.client.Fetcher
import ee.schimke.livery.client.JavaCrypto
import ee.schimke.livery.client.LoadedTemplate
import ee.schimke.livery.client.MemoryBlobStore
import ee.schimke.livery.client.MemoryKeyValueStore
import ee.schimke.livery.client.RenderPlan
import ee.schimke.livery.client.StaticSource
import ee.schimke.livery.client.TemplateRepository
import ee.schimke.livery.client.TrustPolicy
import ee.schimke.livery.compose.LiveryHost
import ee.schimke.livery.compose.LiveryTemplate
import ee.schimke.livery.protocol.Manifest
import ee.schimke.livery.protocol.PlayerCapabilities
import ee.schimke.livery.protocol.Surfaces
import ee.schimke.livery.protocol.TemplateEvent
import java.io.File
import kotlin.math.ceil
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * How the sessions list draws a card.
 * - [Native]: Confetti's own `SessionItemView` (Material 3 `ListItem`, chips, bookmark button).
 * - [LiveryPerCard]: the series' Livery session template, each card with a `LiveryHost` and so a
 *   `LiveryResources` of its own, as `:livery:android`'s slots built them before this benchmark:
 *   every card decodes its template and reads its fonts again.
 * - [LiveryShared]: the same template, every card on one series-wide `LiveryResources`, prepared
 *   and its fonts loaded before the list shows (what `SeriesCatalog.warm` does on each catalog
 *   revision).
 */
enum class Lane(val description: String) {
    Native("Confetti SessionItemView (Compose UI)"),
    LiveryPerCard("Livery, a host per card (before)"),
    LiveryShared("Livery, one warmed host per series (after)"),
}

data class LaneResult(val series: String, val lane: Lane, val frameNanos: List<Long>, val arrivals: Int, val drawn: Int) {
    val perCardMicros get() = frameNanos.average() / 1_000.0 / SessionsBenchmark.ARRIVALS_PER_FRAME
    fun percentileMicros(p: Double): Double {
        val sorted = frameNanos.sorted()
        return sorted[(ceil(p * sorted.size).toInt() - 1).coerceIn(0, sorted.lastIndex)] / 1_000.0
    }
}

/** One series' catalog on disk: its manifest, its session template, and a repository over its fonts. */
class SeriesCatalog(val series: String, dir: File) {
    private val manifest = json.decodeFromString<Manifest>(File(dir, "manifest.json").readText())
    private val blobs = MemoryBlobStore()
    private val clock = Clock { 1_000L }
    val repository = TemplateRepository(
        StaticSource("https://catalog.invalid/"),
        Fetcher { _, _ -> FetchResult.Failed(503, "offline") },
        JavaCrypto,
        TrustPolicy.AllowUnsigned,
        blobs,
        MemoryKeyValueStore(),
        clock,
        listOf(PlayerCapabilities(Surfaces.IN_APP, maxRcApiLevel = 7, profiles = setOf("androidx"))),
    )
    private val contract = requireNotNull(manifest.contract("confetti/session@1")) { "$series has no session contract" }
    private val entry = requireNotNull(manifest.templates.firstOrNull { it.designId == "$series-session" }) { "$series has no $series-session template" }
    val template = LoadedTemplate(
        entry = entry,
        design = manifest.design(entry.designId),
        bytes = File(dir, "blobs/${entry.sha256}.rc").readBytes(),
        origin = LoadedTemplate.Origin.Bundled,
        contract = contract,
        assets = entry.assets.mapNotNull(manifest::asset),
    )
    /** The series' light theme pack, as `LiveryStyling.theme` hands a slot its colours. */
    private val theme: Bindings =
        (manifest.themes.firstOrNull { it.id == "$series-light" } ?: manifest.themes.firstOrNull { it.id == series })
            ?.let { Bindings(it.values) } ?: Bindings.EMPTY

    init {
        runBlocking {
            for (asset in template.assets) blobs.put(asset.sha256, File(dir, "assets/${asset.sha256}.ttf").readBytes())
        }
    }

    fun host(events: (TemplateEvent) -> Unit) = LiveryHost(
        repository = repository,
        actions = ActionRouter(emptyMap(), { false }, { }, clock),
        events = { events(it) },
        clock = clock,
    )

    fun plan(session: SessionDetails): RenderPlan {
        val bound = ContractBinder.bind(contract, session.toCard().toJson())
        val bindings = (bound as? BindResult.Ok)?.bindings ?: error("$series: $bound")
        return RenderPlan(template, contract, theme + bindings)
    }
}

/** What `:livery:android` binds a talk to (`SessionDetails.toSessionCard`), for a talk that is not live. */
private fun SessionDetails.toCard() = SessionCard(
    title = title,
    speakers = speakers.joinToString { it.sessionSpeakerDetails.name },
    room = room?.name,
    time = "10:30 – 11:10",
    tag = tags.firstOrNull(),
    speakerList = speakers.map { SessionSpeaker(it.sessionSpeakerDetails.id, it.sessionSpeakerDetails.name) },
)

/**
 * A fling down the sessions list, without a list: a window of [VISIBLE] rows, each frame scrolled on
 * by [ARRIVALS_PER_FRAME] rows that compose for the first time while as many leave, as in the
 * player's own feed profile (rc-players' `RcFeedProfile`). Each frame's wall time (composition,
 * layout and a software draw of the window) is what rows arriving cost. Not a `LazyColumn`: its
 * prefetch would hide the per-row cost, which is what prefetch has to fit into idle time.
 */
class SessionsBenchmark(private val sessions: List<SessionDetails>) {
    fun play(catalog: SeriesCatalog?, lane: Lane, frames: Int): LaneResult {
        var drawn = 0
        val shared = catalog?.host { if (it.type == TemplateEvent.Type.Impression) drawn++ }
        if (lane == Lane.LiveryShared) runBlocking {
            // SeriesCatalog.warm: the template prepared and its fonts read before the list shows.
            shared!!.resources.prepared(catalog.template)
            shared.resources.typefaces(catalog.template)
        }
        val plans = catalog?.let { c -> sessions.map(c::plan) }
        var first by mutableIntStateOf(0)
        val scene = ImageComposeScene(width = WIDTH, height = ROW_HEIGHT * VISIBLE, density = Density(1f)) {
            MaterialTheme {
                Surface {
                    Column {
                        for (item in first until first + VISIBLE) {
                            key(item) { Row(lane, item, catalog, shared, plans) { drawn++ } }
                        }
                    }
                }
            }
        }
        try {
            var time = 0L
            scene.render(time)
            val nanos = List(frames) {
                first += ARRIVALS_PER_FRAME
                time += FRAME_NANOS
                val start = System.nanoTime()
                scene.render(time)
                System.nanoTime() - start
            }
            return LaneResult(catalog?.series ?: "native", lane, nanos, VISIBLE + frames * ARRIVALS_PER_FRAME, drawn)
        } finally {
            scene.close()
        }
    }

    @Composable
    private fun Row(lane: Lane, item: Int, catalog: SeriesCatalog?, shared: LiveryHost?, plans: List<RenderPlan>?, onDrawn: () -> Unit) {
        val modifier = Modifier.fillMaxWidth().height(ROW_HEIGHT.dp)
        val session = sessions[item % sessions.size]
        when (lane) {
            Lane.Native -> {
                SessionItemView(session, sessionSelected = {}, isBookmarked = false, addBookmark = {}, removeBookmark = {}, isLoggedIn = true)
                remember { onDrawn() }
            }
            Lane.LiveryPerCard -> {
                val host = remember { catalog!!.host { if (it.type == TemplateEvent.Type.Impression) onDrawn() } }
                LiveryTemplate(plans!![item % plans.size], host, modifier)
            }
            Lane.LiveryShared -> LiveryTemplate(plans!![item % plans.size], shared!!, modifier)
        }
    }

    companion object {
        const val WIDTH = 412
        const val ROW_HEIGHT = 140
        const val VISIBLE = 5
        const val ARRIVALS_PER_FRAME = 2
        const val FRAME_NANOS = 16_666_667L
    }
}

private val json = Json { ignoreUnknownKeys = true }

/**
 * Arguments: the output directory, then a directory of catalogs (`<series>/manifest.json`). Every
 * lane is warmed first, then the lanes interleave over five rounds, rotating their order, so no lane
 * pays for JIT or a busy moment on another's behalf.
 */
fun main(args: Array<String>) {
    val out = File(args.getOrNull(0) ?: "build/benchmark").also { it.mkdirs() }
    val dir = args.getOrNull(1)?.takeIf { it.isNotBlank() }?.let(::File)
        ?: error("pass -Pconfetti.benchmark.catalogs=<dir>, a directory of Livery catalogs (<series>/manifest.json)")
    val catalogs = listOf("confetti", "droidcon", "kotlinconf").filter { File(dir, "$it/manifest.json").isFile }.map { SeriesCatalog(it, File(dir, it)) }
    // Distinct rows: every title differs, so no row's text layout is another's.
    val sessions = List(40) { i -> sessionDetails.copy(id = "s$i", title = "${sessionDetails.title} (part ${i + 1})") }
    val benchmark = SessionsBenchmark(sessions)
    val runs = listOf<Pair<SeriesCatalog?, Lane>>(null to Lane.Native) +
        catalogs.flatMap { c -> listOf(c to Lane.LiveryPerCard, c to Lane.LiveryShared) }
    runs.forEach { (c, lane) -> benchmark.play(c, lane, WARMUP_FRAMES) }
    val samples = mutableMapOf<Pair<SeriesCatalog?, Lane>, MutableList<Long>>()
    val drawn = mutableMapOf<Pair<SeriesCatalog?, Lane>, Int>()
    val arrivals = mutableMapOf<Pair<SeriesCatalog?, Lane>, Int>()
    repeat(ROUNDS) { round ->
        runs.indices.forEach { i ->
            val run = runs[(i + round) % runs.size]
            val result = benchmark.play(run.first, run.second, FRAMES / ROUNDS)
            samples.getOrPut(run) { mutableListOf() } += result.frameNanos
            drawn[run] = (drawn[run] ?: 0) + result.drawn
            arrivals[run] = (arrivals[run] ?: 0) + result.arrivals
        }
    }
    val results = runs.map { run -> LaneResult(run.first?.series ?: "-", run.second, samples.getValue(run), arrivals.getValue(run), drawn.getValue(run)) }
    val native = results.first { it.lane == Lane.Native }.perCardMicros
    val report = buildString {
        appendLine("# Sessions list fling: a row arriving, Compose UI against Livery")
        appendLine()
        appendLine("- **JVM** — ${System.getProperty("java.vm.name")} ${System.getProperty("java.version")}, ${Runtime.getRuntime().availableProcessors()} CPUs")
        appendLine("- **Renderer** — Compose Desktop ImageComposeScene (skiko software raster), ${SessionsBenchmark.WIDTH} px wide, density 1")
        appendLine("- **Fling** — ${SessionsBenchmark.VISIBLE} rows on screen, ${SessionsBenchmark.ARRIVALS_PER_FRAME} new rows a frame, $FRAMES frames per lane")
        appendLine()
        appendLine("| Series | Lane | Per new row | p50 frame | p95 frame | vs Compose UI | Rows drawn |")
        appendLine("| --- | --- | ---: | ---: | ---: | ---: | ---: |")
        for (r in results) {
            appendLine(
                "| ${r.series} | ${r.lane.description} | ${us(r.perCardMicros)} | ${us(r.percentileMicros(0.5))} | ${us(r.percentileMicros(0.95))} | " +
                    "${"%.0f".format(100 * r.perCardMicros / native)}% | ${"%.0f".format(100.0 * r.drawn / r.arrivals)}% |",
            )
        }
        appendLine()
        appendLine("*Rows drawn*: the share of rows that drew before they scrolled away (a Livery row waits for its fonts until they are in memory).")
    }
    File(out, "sessions.md").writeText(report)
    println(report)
}

private fun us(micros: Double) = if (micros >= 1_000) "%.2f ms".format(micros / 1_000) else "%.0f µs".format(micros)

private const val WARMUP_FRAMES = 60
private const val FRAMES = 300
private const val ROUNDS = 5
