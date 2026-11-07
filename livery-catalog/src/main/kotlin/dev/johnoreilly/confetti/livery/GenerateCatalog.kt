package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import ee.schimke.composeai.rcplayer.protocol.RcBitmapData
import ee.schimke.composeai.rcplayer.compose.RcImageLoader
import ee.schimke.composeai.rcplayer.compose.RcFontVariations
import ee.schimke.composeai.rcplayer.compose.RcTypefaceLoader
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import ee.schimke.livery.client.BindResult
import ee.schimke.livery.client.Bindings
import ee.schimke.livery.client.ContractBinder
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.dynamicColorScheme
import ee.schimke.livery.protocol.Contract
import ee.schimke.livery.protocol.NamedValue
import ee.schimke.livery.render.RenderSpec
import ee.schimke.livery.render.TemplateRenderer
import ee.schimke.livery.protocol.PlacementRule
import ee.schimke.livery.protocol.ThemePack
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.put
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
private val json = Json { prettyPrint = true; prettyPrintIndent = "  "; ignoreUnknownKeys = true }

class CapturedTemplate(
    val design: DesignDef, val template: TemplateDef, val bytes: ByteArray,
    val reads: Set<String>, val emits: Set<String>, val fonts: Set<String>,
)

/** Captures one template to Remote Compose bytes on the JVM, recording what it read and emitted. */
fun capture(design: DesignDef, template: TemplateDef): CapturedTemplate = runBlocking {
    val scope = TemplateScope(design.contract, design.brand)
    val bytes = captureCommonRemoteDocument(
        creationDisplayInfo = RemoteCreationDisplayInfo(
            width = template.widthDp, height = template.heightDp, densityDpi = 160,
            densityBehavior = RemoteDensityBehavior.Dp,
        ),
    ) {
        CompositionLocalProvider(LocalTemplateScope provides scope) { template.content() }
    }
    CapturedTemplate(design, template, bytes, scope.reads, scope.emits, scope.fonts)
}

/** The contract as Livery reads it, so binding here is exactly what the device does. */
fun ConfettiContract.toLivery(): Contract = json.decodeFromJsonElement(toJson())

fun bind(contract: ConfettiContract, data: JsonObject): Bindings =
    when (val r = ContractBinder.bind(contract.toLivery(), data)) {
        is BindResult.Ok -> r.bindings
        is BindResult.Invalid -> error("${contract.ref}: ${r.problems.joinToString { "${it.name}: ${it.message}" }}")
    }

/** Renders a captured template with its design's example data (and a theme pack's values) to PNG. */
fun render(captured: CapturedTemplate, data: JsonObject, theme: Bindings = Bindings.EMPTY, dark: Boolean = true): ByteArray =
    TemplateRenderer.png(
        template = captured.bytes,
        bindings = bind(captured.design.contract, data) + theme,
        spec = RenderSpec(captured.template.widthDp, captured.template.heightDp, density = 2f, dark = dark),
        images = PreviewImages,
        typefaces = CatalogFonts.forSeries(captured.design.brand.series),
    )

/** Where the catalogs live when run from the repository root or from this module (tests). */
fun catalogsRoot(): File = listOf(File("catalogs"), File("livery-catalog/catalogs")).first { it.isDirectory }.absoluteFile

/**
 * The series catalogs' own font files (`catalogs/<series>/fonts/`), so renders draw the face a
 * device would download with the template rather than the JVM's default.
 */
object CatalogFonts {
    private val cache = mutableMapOf<String, RcTypefaceLoader>()

    fun forSeries(series: String, root: File = catalogsRoot()): RcTypefaceLoader = cache.getOrPut(series) {
        val dir = root.resolve("$series/fonts")
        val files = dir.resolve("fonts.json").takeIf { it.isFile }
            ?.let(::readFonts).orEmpty()
        val families = files.groupBy { it.family }.mapValues { (_, faces) ->
            FontFamily(faces.map { Font(dir.resolve(it.file), FontWeight(it.weight)) })
        }
        object : RcTypefaceLoader {
            override val families: Set<String> get() = families.keys + RcTypefaceLoader.Default.families
            override fun typeface(family: String, variations: RcFontVariations?): FontFamily? =
                // The player asks in lower case ("space mono"), as LiveryTypefaceLoader allows for.
                families.entries.firstOrNull { it.key.equals(family, ignoreCase = true) }?.value
                    ?: RcTypefaceLoader.Default.typeface(family, variations)
        }
    }
}

/** One entry of a catalog's `fonts/fonts.json` (this module has no serialization plugin). */
private data class FontFileJson(val file: String, val family: String, val weight: Int)

private fun readFonts(file: File): List<FontFileJson> =
    json.parseToJsonElement(file.readText()).jsonArray.map { e ->
        val o = e.jsonObject
        FontFileJson(o.getValue("file").jsonPrimitive.content, o.getValue("family").jsonPrimitive.content, o["weight"]?.jsonPrimitive?.int ?: 400)
    }

/** Every image a preview asks for is the same placeholder avatar: renders never touch the network. */
private object PreviewImages : RcImageLoader {
    private val avatar by lazy {
        val bitmap = ImageBitmap(160, 160)
        Canvas(bitmap).apply {
            drawRect(0f, 0f, 160f, 160f, Paint().apply { color = Color(0xFF5B6B7F) })
            drawCircle(Offset(80f, 62f), 30f, Paint().apply { color = Color(0xFFE8EDF3) })
            drawCircle(Offset(80f, 160f), 58f, Paint().apply { color = Color(0xFFE8EDF3) })
        }
        bitmap
    }
    override suspend fun load(data: RcBitmapData): ImageBitmap = avatar
}

/** The contract checks Livery's publisher doesn't do because it can't see Kotlin. */
fun checkContracts(captured: List<CapturedTemplate>, catalog: File, brand: ConferenceBrand): List<String> {
    val problems = mutableListOf<String>()
    // Every example binds through the same gate the device uses.
    AllContracts.forEach { c -> c.examples.forEach { (name, data) -> runCatching { bind(c, c.data(data)) }.onFailure { problems += "example $name: ${it.message}" } } }
    // Every design emits every action of its contract somewhere, and reads every required input.
    captured.groupBy { it.design }.forEach { (design, templates) ->
        val emitted = templates.flatMap { it.emits }.toSet()
        design.contract.actions.keys.filterNot { it in emitted }.forEach { problems += "${design.id}: never emits ${design.contract.ref} action '$it'" }
        templates.filter { it.template.name == "in-app" }.forEach { t ->
            design.contract.inputs.values.filter { it.required && it.name !in t.reads }
                .forEach { problems += "${design.id}/in-app: doesn't show required input '${it.name}'" }
        }
    }
    // Each template names its brand's font, and the catalog ships that font.
    val shipped = catalog.resolve("fonts/fonts.json").takeIf { it.isFile }
        ?.let(::readFonts)?.map { it.family }?.toSet().orEmpty()
    captured.forEach { t ->
        (t.fonts - shipped).filterNot { it.startsWith("system:") || it.startsWith("google:") }
            .forEach { problems += "${t.design.id}/${t.template.name}: font '$it' isn't in ${brand.series}/fonts/fonts.json" }
    }
    // Theme packs only set theme roles every contract declares.
    val roles = AllContracts.map { c -> c.inputs.values.filter { it.role != null }.map { it.name }.toSet() }.reduce { a, b -> a intersect b }
    catalog.resolve("themes").listFiles { f -> f.extension == "json" }.orEmpty().forEach { f ->
        val pack = json.decodeFromString<ThemePack>(f.readText())
        pack.values.keys.filterNot { it in roles }.forEach { problems += "themes/${f.name}: '$it' is not a theme role" }
    }
    // Placements name a design that exists and carry data its contract accepts.
    catalog.resolve("placements").listFiles { f -> f.extension == "json" }.orEmpty().forEach { f ->
        val rule = json.decodeFromString<PlacementRule>(f.readText())
        val design = AllDesigns.find { it.id == rule.template.designId && it.brand == brand }
        if (design == null) problems += "${brand.series}/placements/${f.name}: no ${brand.series} design ${rule.template.designId}"
        else runCatching { bind(design.contract, rule.data) }.onFailure { problems += "placements/${f.name}: ${it.message}" }
    }
    return problems
}

private fun JsonElement.pretty() = json.encodeToString(JsonElement.serializer(), this) + "\n"

/**
 * Writes the generated half of each conference's catalog source tree, `catalogs/<series>/`:
 * catalog.json, contracts/ from Contracts.kt, and per design design.json, <template>.rc and
 * <template>.json. themes/, placements/, experiments/ and fonts/ are hand-edited data (what an
 * admin's agent changes most) and are only checked. Renders go to [renders].
 *
 * One catalog per series: each is published to its own URL and bundled as its own copy, so one
 * conference's change never ships, or breaks, another's.
 */
fun main(args: Array<String>) {
    val root = File(args.getOrElse(0) { "livery-catalog/catalogs" })
    val renders = args.getOrNull(1)?.let(::File)
    val all = mutableListOf<CapturedTemplate>()
    for (brand in AllBrands) {
        val catalog = root.resolve(brand.series).apply { mkdirs() }
        val captured = generate(brand, catalog)
        all += captured
        renders?.let { renderSeries(brand, captured, catalog, it) }
    }
    renders?.let { println("renders: ${it.absolutePath}") }
}

/** Captures [brand]'s designs into [catalog], after checking them; returns what it captured. */
fun generate(brand: ConferenceBrand, catalog: File): List<CapturedTemplate> {
    catalog.resolve("catalog.json").writeText(buildJsonObject {
        put("catalog", "confetti-${brand.series}"); put("channel", "main")
        put("ttlSeconds", 3600); put("expiresInDays", 30)
    }.pretty())
    catalog.resolve("contracts").apply { mkdirs() }.let { dir ->
        AllContracts.forEach { dir.resolve("${it.fileName}.json").writeText(it.toJson().pretty()) }
    }

    val captured = AllDesigns.filter { it.brand == brand }.flatMap { d -> d.templates.map { capture(d, it) } }
    val problems = checkContracts(captured, catalog, brand)
    if (problems.isNotEmpty()) error("Contract problems:\n  " + problems.joinToString("\n  "))

    val designsDir = catalog.resolve("designs")
    designsDir.deleteRecursively()
    captured.groupBy { it.design }.forEach { (design, templates) ->
        val dir = designsDir.resolve(design.id).apply { mkdirs() }
        dir.resolve("design.json").writeText(buildJsonObject {
            put("id", design.id); put("contract", design.contract.ref); put("title", design.title)
            put("author", "confetti"); put("variant", design.variant); put("install", design.install)
            put("tags", buildJsonObject { put("series", design.brand.series) })
        }.pretty())
        templates.forEach { t ->
            dir.resolve("${t.template.name}.rc").writeBytes(t.bytes)
            dir.resolve("${t.template.name}.json").writeText(buildJsonObject {
                put("target", json.parseToJsonElement(t.template.target))
                put("reads", buildJsonArray { t.reads.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
                put("emits", buildJsonArray { t.emits.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
                if (t.fonts.isNotEmpty()) put("fonts", buildJsonArray { t.fonts.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } })
            }.pretty())
            println("${brand.series}/${design.id}/${t.template.name}.rc  ${t.bytes.size} bytes  reads=${t.reads.size} emits=${t.emits} fonts=${t.fonts}")
        }
    }
    return captured
}

private fun renderSeries(brand: ConferenceBrand, captured: List<CapturedTemplate>, catalog: File, out: File) {
    out.mkdirs()
    captured.forEach { t ->
        val c = t.design.contract
        out.resolve("${t.design.id}-${t.template.name}.png").writeBytes(render(t, c.data(c.examples.getValue(t.design.example))))
        c.examples["long"]?.takeIf { t.template.name == "in-app" }?.let { long ->
            out.resolve("${t.design.id}-${t.template.name}-long.png").writeBytes(render(t, c.data(long)))
        }
    }
    if (brand == Droidcon) {
        // What an admin's data-only change looks like: the same droidcon documents, recoloured by
        // a different theme pack's values (here a venue blue for droidcon Berlin), no new capture.
        val berlin = Bindings(mapOf(
            "theme.primary" to NamedValue.Color(0xFF4FC3F7), "theme.secondary" to NamedValue.Color(0xFFFFD54F),
            "theme.tertiary" to NamedValue.Color(0xFF7E57C2), "theme.onPrimary" to NamedValue.Color(0xFF0B1A2A),
        ))
        captured.filter { it.template.name == "in-app" && it.design.contract != PromoContract }.forEach { t ->
            val c = t.design.contract
            out.resolve("${t.design.id}-in-app-berlin-theme.png").writeBytes(render(t, c.data(c.examples.getValue(t.design.example)), berlin))
        }
    }
    // Light and dark: the same documents with each mode's theme pack, as the app pushes them.
    fun pack(id: String) = catalog.resolve("themes/$id.json").takeIf { it.isFile }
        ?.let { Bindings(json.decodeFromString<ThemePack>(it.readText()).values) } ?: error("no theme pack $id")
    // Material 3 dynamic colour: a user's wallpaper scheme pushed as the theme roles instead of
    // the conference's, the layout unchanged. Two stand-in wallpapers, one per mode.
    val dynamic = mapOf(
        "dynamic-teal-light" to (Color(0xFF00897B) to false),
        "dynamic-rose-dark" to (Color(0xFFD81B60) to true),
    )
    captured.filter { it.template.name == "in-app" }.forEach { t ->
        val c = t.design.contract
        val data = c.data(c.examples.getValue(t.design.example))
        for (dark in listOf(false, true)) {
            val mode = if (dark) "dark" else "light"
            out.resolve("${t.design.id}-in-app-$mode.png")
                .writeBytes(render(t, data, pack(ConferenceSeries.themePack(brand.series, dark)), dark))
        }
        dynamic.forEach { (name, seed) ->
            val (color, dark) = seed
            val scheme = dynamicColorScheme(seedColor = color, isDark = dark, isAmoled = false)
            out.resolve("${t.design.id}-in-app-$name.png").writeBytes(render(t, data, scheme.toLiveryTheme(), dark))
        }
    }
}

/** The app's mapping (shared's LiverySlots does the same): a Material 3 scheme as the theme roles. */
fun ColorScheme.toLiveryTheme(): Bindings {
    fun Color.argb() = toArgb().toLong() and 0xFFFFFFFFL
    return Bindings(materialThemeValues(
        primary = primary.argb(), onPrimary = onPrimary.argb(), secondary = secondary.argb(),
        tertiary = tertiary.argb(), surface = surfaceContainerHigh.argb(), onSurface = onSurface.argb(),
        onSurfaceVariant = onSurfaceVariant.argb(),
    ).mapValues { NamedValue.Color(it.value) })
}
