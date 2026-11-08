package dev.johnoreilly.confetti.livery

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/*
 * The contracts between Confetti and its conference designs, in one place.
 *
 * This file is the source of truth: catalog/contracts/<name>.json is generated from it, designs can
 * only read an input or emit an action that is declared here (they take these objects, not
 * strings), and the app builds its data with the same declarations. A rename here breaks the
 * designs and the app at compile time instead of shipping a blank field.
 *
 * Evolution follows Livery (PROTOCOL.md §4.3): within a major, only add optional inputs and actions
 * with `since` set to the new minor; anything else is a new major (a new object here).
 */

enum class InputType(val wire: String) { String("string"), Bool("bool"), Color("color"), Image("image"), Int("int") }

class Input(
    val name: kotlin.String,
    val type: InputType,
    val required: Boolean = false,
    val maxLength: kotlin.Int? = null,
    val default: JsonElement? = null,
    val role: kotlin.String? = null,
    val description: kotlin.String? = null,
    val since: kotlin.Int = 0,
)

/**
 * [value] cut to fit this input's `maxLength`, for app data that isn't under our control (a long
 * session title): binding refuses an over-long value, which would drop the whole slot to its
 * fallback, so the app trims first.
 */
fun Input.fit(value: String): String {
    val max = maxLength ?: return value
    return if (value.length <= max) value else value.take(max - 1).trimEnd() + "…"
}

class Action(
    val name: String,
    val kind: String,
    /** At most one string argument: the document sends it as `{"<arg>": "..."}` (or bare for `value`). */
    val arg: String? = null,
    val description: String? = null,
    val since: Int = 0,
)

enum class DataSource { App, Decision, Feed }

abstract class ConfettiContract(
    val id: String,
    val major: Int,
    val minor: Int,
    val displayName: String,
    val dataSource: DataSource,
    /** The contract's file name under catalog/contracts/. */
    val fileName: String,
) {
    private val _inputs = linkedMapOf<String, Input>()
    private val _actions = linkedMapOf<String, Action>()
    val inputs: Map<String, Input> get() = _inputs
    val actions: Map<String, Action> get() = _actions
    val ref: String get() = "$id@$major"

    protected fun string(name: String, required: Boolean = false, maxLength: Int, description: String? = null) =
        add(Input(name, InputType.String, required, maxLength, description = description))

    protected fun bool(name: String, default: Boolean = false, description: String? = null) =
        add(Input(name, InputType.Bool, default = JsonPrimitive(default), description = description))

    protected fun image(name: String, description: String? = null) =
        add(Input(name, InputType.Image, description = description))

    protected fun action(name: String, kind: String, arg: String? = null, description: String? = null) =
        Action(name, kind, arg, description).also { _actions[name] = it }

    private fun add(input: Input) = input.also {
        check(it.name !in _inputs) { "$id declares ${it.name} twice" }
        _inputs[it.name] = it
    }

    /** Every contract takes the conference palette as theme roles, so a theme pack can recolour it. */
    val theme = ThemeInputs(
        primary = role("primary"), onPrimary = role("onPrimary"), secondary = role("secondary"),
        tertiary = role("tertiary"), surface = role("surface"), onSurface = role("onSurface"),
        onSurfaceVariant = role("onSurfaceVariant"),
    )

    private fun role(role: String) = add(Input("theme.$role", InputType.Color, role = role))

    /** Example datasets: the picker's previews, the renders, and what an agent starts from. */
    abstract val examples: Map<String, Map<Input, Any>>

    fun toJson(): JsonObject = buildJsonObject {
        put("id", id); put("major", major); put("minor", minor); put("title", displayName)
        put("dataSource", dataSource.name)
        putJsonObject("inputs") {
            inputs.values.forEach { i ->
                putJsonObject(i.name) {
                    put("type", i.type.wire)
                    if (i.required) put("required", true)
                    i.maxLength?.let { put("maxLength", it) }
                    i.default?.let { put("default", it) }
                    i.role?.let { put("role", it) }
                    i.description?.let { put("description", it) }
                    if (i.since > 0) put("since", i.since)
                }
            }
        }
        putJsonObject("actions") {
            actions.values.forEach { a ->
                putJsonObject(a.name) {
                    put("kind", a.kind)
                    a.arg?.let { arg -> putJsonObject("args") { putJsonObject(arg) { put("type", "string"); put("required", true) } } }
                    a.description?.let { put("description", it) }
                    if (a.since > 0) put("since", a.since)
                }
            }
        }
        putJsonObject("examples") { examples.forEach { (name, data) -> put(name, data(data)) } }
    }

    /** A dataset for this contract, keyed by its own inputs: what the app sends and binds. */
    fun data(values: Map<Input, Any>): JsonObject = buildJsonObject {
        values.forEach { (input, value) ->
            require(inputs[input.name] === input) { "${input.name} is not an input of $ref" }
            put(input.name, when (value) {
                is String -> JsonPrimitive(value)
                is Boolean -> JsonPrimitive(value)
                is Number -> JsonPrimitive(value)
                else -> error("${input.name}: unsupported ${value::class}")
            })
        }
    }
}

class ThemeInputs(
    val primary: Input, val onPrimary: Input, val secondary: Input, val tertiary: Input,
    val surface: Input, val onSurface: Input, val onSurfaceVariant: Input,
)

/** The conference header: the top of the home screen, and a launcher widget's banner. */
object ConferenceContract : ConfettiContract("confetti/conference", 1, 0, "Conference header", DataSource.App, "conference") {
    val name = string("name", required = true, maxLength = 40)
    val dates = string("dates", required = true, maxLength = 32, description = "already formatted for the locale")
    val location = string("location", maxLength = 60)
    val tagline = string("tagline", maxLength = 90)
    val live = bool("live", description = "the conference is on today")
    val ticketsUrl = string("ticketsUrl", maxLength = 200)
    val tickets = action("tickets", "open-url", arg = "url")

    override val examples get() = mapOf(
        "default" to mapOf(name to "droidcon London", dates to "29–30 OCT 2026", location to "Business Design Centre, London",
            tagline to "The Android developer community, in person.", live to true, ticketsUrl to "https://london.droidcon.com"),
        "kotlinconf" to mapOf(name to "KotlinConf 2026", dates to "May 21–23, 2026", location to "Messe München, Munich",
            tagline to "The Kotlin conference, by JetBrains.", live to true, ticketsUrl to "https://kotlinconf.com"),
        "long" to mapOf(name to "A conference name that is forty chars xx", dates to "Thirty-two characters of dates x",
            location to "A venue name that runs on for quite a while, sixty chars xx",
            tagline to "A tagline at its ninety character limit, to check that the header wraps and stays legibl"),
    )
}

/** One session: agenda rows, bookmarks, the "next up" widget and tile. */
object SessionContract : ConfettiContract("confetti/session", 1, 0, "Session card", DataSource.App, "session") {
    val title = string("title", required = true, maxLength = 120)
    val speakers = string("speakers", maxLength = 80, description = "names, comma separated")
    val room = string("room", maxLength = 30)
    val time = string("time", maxLength = 20, description = "already formatted, e.g. 10:30 – 11:10")
    val tag = string("tag", maxLength = 24)
    val live = bool("live")
    val bookmarked = bool("bookmarked")
    val open = action("open", "app:open-session")
    val bookmark = action("bookmark", "app:toggle-bookmark")

    override val examples get() = mapOf(
        "default" to mapOf(title to "Building adaptive apps with Compose", speakers to "Jane Doe, John Smith", room to "Hall 1",
            time to "10:30 – 11:10", tag to "compose", live to true, bookmarked to true),
        "kotlinconf" to mapOf(title to "What's new in Kotlin 2.4", speakers to "Alex Kotlin", room to "Auditorium",
            time to "09:00 – 09:45", tag to "language", live to true, bookmarked to false),
        "long" to mapOf(title to "A very long session title that keeps going to check how the card wraps at two lines and then ellipsizes the rest",
            speakers to "Speaker One, Speaker Two, Speaker Three, Speaker Four", room to "Room with a long name",
            time to "10:30 – 11:10", tag to "multiplatform"),
    )
}

object SpeakerContract : ConfettiContract("confetti/speaker", 1, 0, "Speaker card", DataSource.App, "speaker") {
    val name = string("name", required = true, maxLength = 60)
    val tagline = string("tagline", maxLength = 80, description = "job title and company")
    val photo = image("photo")
    val sessions = string("sessions", maxLength = 20, description = "already formatted, e.g. 2 sessions")
    val open = action("open", "app:open-speaker")

    override val examples get() = mapOf(
        "default" to mapOf(name to "Jane Doe", tagline to "Android GDE · Acme", sessions to "2 sessions"),
        "kotlinconf" to mapOf(name to "Alex Kotlin", tagline to "Kotlin team · JetBrains", sessions to "Keynote"),
    )
}

/** Promotions and growth: placements on the home feed, the agenda and the speaker page. */
object PromoContract : ConfettiContract("confetti/promo", 1, 0, "Conference promotion", DataSource.Decision, "promo") {
    val eyebrow = string("eyebrow", maxLength = 20)
    val headline = string("headline", required = true, maxLength = 48)
    val body = string("body", maxLength = 120)
    val cta = string("cta", required = true, maxLength = 20)
    val url = string("url", required = true, maxLength = 200)
    val ctaAction = action("cta", "open-url", arg = "url")
    val dismiss = action("dismiss", "dismiss")

    override val examples get() = mapOf(
        "default" to mapOf(eyebrow to "TONIGHT", headline to "Community party at 7pm",
            body to "Drinks, demos and the famous droidcon quiz. Badge required.", cta to "SEE DETAILS", url to "https://london.droidcon.com"),
        "kotlinconf" to mapOf(eyebrow to "Workshops", headline to "Hands-on Compose Multiplatform",
            body to "A full-day workshop the day before the conference. 12 seats left.", cta to "Book a seat", url to "https://kotlinconf.com/workshops"),
    )
}

val AllContracts: List<ConfettiContract> = listOf(ConferenceContract, SessionContract, SpeakerContract, PromoContract)

/**
 * Which design family a conference is drawn in: the `series` tag every design carries, and what
 * the app asks for (`TemplateQuery.tags`). A conference with no series keeps Confetti's own UI.
 */
object ConferenceSeries {
    const val TAG = "series"

    private val prefixes = listOf("droidcon" to "droidcon", "kotlinconf" to "kotlinconf")

    /** Every series with designs: one Livery catalog each (`catalogs/<series>/`). */
    val ALL: List<String> = prefixes.map { it.second }.distinct()

    fun of(conferenceId: String): String? =
        prefixes.firstOrNull { (prefix, _) -> conferenceId.startsWith(prefix) }?.second

    /** The Livery segment a placement targets a conference's attendees with. */
    fun segment(conferenceId: String): String = "conference:$conferenceId"

    /** The theme pack for a series in the app's current mode: `droidcon-light`, `kotlinconf-dark`. */
    fun themePack(series: String, dark: Boolean): String = "$series-${if (dark) "dark" else "light"}"
}

/**
 * The theme roles every contract takes (see [ThemeInputs]), from a Material 3 colour scheme: how a
 * user's dynamic colours reach a design. Named arguments so each platform passes the scheme's
 * fields explicitly; one mapping for the apps and the catalog's renders.
 *
 * `surface` is the colour a design draws its cards on: pass the scheme's `surfaceContainerHigh`,
 * so a card stands off the screen behind it in both modes.
 */
fun materialThemeValues(
    primary: Long,
    onPrimary: Long,
    secondary: Long,
    tertiary: Long,
    surface: Long,
    onSurface: Long,
    onSurfaceVariant: Long,
): Map<String, Long> = mapOf(
    "theme.primary" to primary,
    "theme.onPrimary" to onPrimary,
    "theme.secondary" to secondary,
    "theme.tertiary" to tertiary,
    "theme.surface" to surface,
    "theme.onSurface" to onSurface,
    "theme.onSurfaceVariant" to onSurfaceVariant,
)
