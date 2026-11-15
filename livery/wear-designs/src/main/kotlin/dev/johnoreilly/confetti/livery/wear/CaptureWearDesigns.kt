package dev.johnoreilly.confetti.livery.wear

import androidx.compose.remote.creation.compose.capture.RemoteCreationDisplayInfo
import androidx.compose.remote.creation.compose.capture.RemoteDensity
import androidx.compose.remote.creation.compose.capture.RemoteDensityBehavior
import androidx.compose.remote.creation.compose.capture.captureCommonRemoteDocument
import androidx.compose.runtime.Composable
import ee.schimke.livery.capture.CapturedTemplate
import ee.schimke.livery.capture.CatalogContracts
import ee.schimke.livery.capture.DesignFolder
import ee.schimke.livery.protocol.SizeRange
import ee.schimke.livery.protocol.Surfaces
import ee.schimke.livery.protocol.Target
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okio.ByteString.Companion.toByteString
import java.io.File

/*
 * Captures every series' Wear designs (WearDesigns.kt) on the plain JVM, into
 * <out>/<series>/designs/<series>-<slot>-wear/: what `captureWearDesigns` runs. Each is a design of
 * its own, variant `wear`, tagged `device=wear` beside its series, so only a watch's query (which
 * names the device) finds it and the phone's plain query still gets the series' default design.
 *
 *   captureWearDesigns <captured catalogs> <out>
 */

/** A watch's in-app slot: narrower than any phone design's minimum width (280 dp). */
val WearTarget = Target(Surfaces.IN_APP, 7, listOf("androidx"), size = SizeRange(minWidthDp = 160, maxWidthDp = 279))

/** One of the contracts a watch draws, and the Wear design for it. */
class WearSlot(
    val name: String,
    val contract: String,
    val title: String,
    val heightDp: Int,
    /** The contract's actions the design emits: what a watch can do from it. */
    val emits: List<String>,
    val content: @Composable (WearValues, WearBrand) -> Unit,
)

val WearSlots = listOf(
    WearSlot("session", "confetti/session@1", "session", WearHeights.SESSION, listOf("open", "bookmark")) { v, b -> WearSessionCard(v, b) },
    WearSlot("section", "confetti/section@1", "section header", WearHeights.SECTION, emptyList()) { v, b -> WearSection(v, b) },
    WearSlot("session-header", "confetti/session-header@1", "session page", WearHeights.SESSION_HEADER, emptyList()) { v, b -> WearSessionHeader(v, b) },
    WearSlot("speaker-header", "confetti/speaker-header@1", "speaker page", WearHeights.SPEAKER_HEADER, emptyList()) { v, b -> WearSpeakerHeader(v, b) },
)

fun main(args: Array<String>) {
    val (captured, out) = args.map(::File)
    val series = captured.listFiles { f -> f.isDirectory }.orEmpty().map { it.name }.sorted()
    check(series.isNotEmpty()) { "no series captured in $captured" }
    for (s in series) {
        val brand = WearBrand(s, pack(captured, s), WearStyle.of(s))
        for (slot in WearSlots) {
            val contract = CatalogContracts.contract(File(captured, s), slot.contract)
            val template = capture(slot, brand, WearValues(contract))
            // Every action a watch can take from it is in the design, and no other: a watch's card
            // has its speakers on one line (no openSpeaker), and it opens no venue map, links or
            // full-screen photo (no openRoom, openSocial, openPhoto).
            check(template.emits.sorted() == slot.emits.sorted()) { "${id(s, slot)} emits ${template.emits}, not ${slot.emits}" }
            write(File(out, "$s/designs/${id(s, slot)}"), s, slot, contract.ref, template)
            println("$s/${id(s, slot)}/wear.rc  ${template.bytes.size} bytes  reads=${template.reads.size} emits=${template.emits}")
        }
    }
}

/** Captures [slot]'s design for [brand] at a watch's width, as :livery:catalog captures the phone's. */
fun capture(slot: WearSlot, brand: WearBrand, values: WearValues): CapturedTemplate = runBlocking {
    val bytes = captureCommonRemoteDocument(
        creationDisplayInfo = RemoteCreationDisplayInfo(
            width = WEAR_WIDTH, height = slot.heightDp, densityDpi = 160,
            densityBehavior = RemoteDensityBehavior.Dp,
        ),
        // The host's density as an expression: any dp a design turns into pixels is converted on
        // the watch that plays it.
        remoteDensity = RemoteDensity.Host,
    ) { slot.content(values, brand) }
    CapturedTemplate.of(bytes.toByteString(), WearTarget, mapOf("design" to id(brand.series, slot)))
}

fun id(series: String, slot: WearSlot) = "$series-${slot.name}-wear"

private val json = Json { prettyPrint = true }

private fun write(dir: File, series: String, slot: WearSlot, contract: String, template: CapturedTemplate) {
    DesignFolder(dir).write(mapOf("wear" to template))
    val design = buildJsonObject {
        put("id", id(series, slot))
        put("contract", contract)
        put("title", "${WearStyle.of(series).title} ${slot.title} (Wear)")
        put("author", "confetti")
        put("variant", "wear")
        put("install", "Always")
        putJsonObject("tags") {
            put("series", series)
            put("device", "wear")
        }
    }
    File(dir, "design.json").writeText(json.encodeToString(JsonObject.serializer(), design) + "\n")
}

/** The series' dark pack (a watch is dark), `theme.<role>` → ARGB: the designs' initial colours. */
fun pack(captured: File, series: String): Map<String, Int> {
    val file = File(captured, "$series/themes/$series-dark.json").takeIf { it.isFile } ?: return emptyMap()
    val values = Json.parseToJsonElement(file.readText()).jsonObject.getValue("values").jsonObject
    return values.mapValues { (_, v) -> v.jsonObject.getValue("argb").jsonPrimitive.long.toInt() }
}
