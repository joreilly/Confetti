package dev.johnoreilly.confetti.livery

import kotlinx.serialization.json.JsonArray
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

/** A key of a contract's dataset: an [Input] or a [ListSpec]. */
sealed interface Field {
    val name: kotlin.String
}

class Input(
    override val name: kotlin.String,
    val type: InputType,
    val required: Boolean = false,
    val maxLength: kotlin.Int? = null,
    val default: JsonElement? = null,
    val description: kotlin.String? = null,
    val since: kotlin.Int = 0,
) : Field

/**
 * A list of items, each with the same [item] fields (PROTOCOL §4.1): bound as `<name>.count` and
 * `<name>.<i>.<field>` for `i` from 0, at most [max] long. A template unrolls it, one row per index
 * it draws, each shown while its index is below the count. Its value in a dataset is a
 * `List<Map<Input, Any>>` keyed by [item]'s inputs.
 */
class ListSpec(
    override val name: kotlin.String,
    val max: kotlin.Int,
    val description: kotlin.String? = null,
    val since: kotlin.Int = 0,
) : Field {
    private val _item = linkedMapOf<kotlin.String, Input>()
    val item: Map<kotlin.String, Input> get() = _item

    fun string(name: kotlin.String, required: Boolean = false, maxLength: kotlin.Int, description: kotlin.String? = null) =
        Input(name, InputType.String, required, maxLength, description = description).also {
            check(name !in _item) { "${this.name} declares $name twice" }
            _item[name] = it
        }

    /** The value name a template reads for [field] of item [index]. */
    fun valueName(index: kotlin.Int, field: Input): kotlin.String {
        require(_item[field.name] === field) { "${field.name} is not a field of ${this.name}" }
        require(index in 0 until max) { "${this.name} has at most $max items" }
        return "${this.name}.$index.${field.name}"
    }

    /** The value name of the number of items bound. */
    val countName: kotlin.String get() = "$name.count"
}

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
    private val _lists = linkedMapOf<String, ListSpec>()
    private val _actions = linkedMapOf<String, Action>()
    val inputs: Map<String, Input> get() = _inputs
    val lists: Map<String, ListSpec> get() = _lists
    val actions: Map<String, Action> get() = _actions
    val ref: String get() = "$id@$major"

    protected fun string(name: String, required: Boolean = false, maxLength: Int, description: String? = null) =
        add(Input(name, InputType.String, required, maxLength, description = description))

    protected fun bool(name: String, default: Boolean = false, description: String? = null) =
        add(Input(name, InputType.Bool, default = JsonPrimitive(default), description = description))

    protected fun image(name: String, description: String? = null) =
        add(Input(name, InputType.Image, description = description))

    protected fun action(name: String, kind: String, arg: String? = null, description: String? = null, since: Int = 0) =
        Action(name, kind, arg, description, since).also {
            check(since in 0..minor) { "$id action $name: since $since is not in 0..$minor" }
            _actions[name] = it
        }

    protected fun list(name: String, max: Int, description: String? = null, since: Int = 0, item: ListSpec.() -> Unit) =
        ListSpec(name, max, description, since).apply(item).also {
            check(it.name !in _inputs && it.name !in _lists) { "$id declares ${it.name} twice" }
            check(since in 0..minor) { "$id list $name: since $since is not in 0..$minor" }
            check(it.item.isNotEmpty()) { "$id list $name has no fields" }
            _lists[it.name] = it
        }

    private fun add(input: Input) = input.also {
        check(it.name !in _inputs) { "$id declares ${it.name} twice" }
        _inputs[it.name] = it
    }

    /** Example datasets: the picker's previews, the renders, and what an agent starts from. */
    abstract val examples: Map<String, Map<out Field, Any>>

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
                    i.description?.let { put("description", it) }
                    if (i.since > 0) put("since", i.since)
                }
            }
        }
        if (lists.isNotEmpty()) putJsonObject("lists") {
            lists.values.forEach { l ->
                putJsonObject(l.name) {
                    putJsonObject("item") {
                        l.item.values.forEach { i ->
                            putJsonObject(i.name) {
                                put("type", i.type.wire)
                                if (i.required) put("required", true)
                                i.maxLength?.let { put("maxLength", it) }
                                i.description?.let { put("description", it) }
                            }
                        }
                    }
                    put("max", l.max)
                    l.description?.let { put("description", it) }
                    if (l.since > 0) put("since", l.since)
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

    /**
     * A dataset for this contract, keyed by its own inputs and lists: what the app sends and binds.
     * A list's value is a `List<Map<Input, Any>>` keyed by its item's fields.
     */
    fun data(values: Map<out Field, Any>): JsonObject = buildJsonObject {
        values.forEach { (field, value) ->
            when (field) {
                is Input -> {
                    require(inputs[field.name] === field) { "${field.name} is not an input of $ref" }
                    put(field.name, primitive(field, value))
                }
                is ListSpec -> {
                    require(lists[field.name] === field) { "${field.name} is not a list of $ref" }
                    val items = value as? List<*> ?: error("${field.name}: a list's value is a List, not ${value::class}")
                    require(items.size <= field.max) { "${field.name}: ${items.size} items, at most ${field.max}" }
                    put(field.name, JsonArray(items.map { item ->
                        val fields = item as? Map<*, *> ?: error("${field.name}: an item is a Map, not $item")
                        buildJsonObject {
                            fields.forEach { (key, v) ->
                                val input = key as? Input ?: error("${field.name}: an item is keyed by its fields")
                                require(field.item[input.name] === input) { "${input.name} is not a field of ${field.name}" }
                                put(input.name, primitive(input, requireNotNull(v)))
                            }
                        }
                    }))
                }
            }
        }
    }

    private fun primitive(input: Input, value: Any): JsonPrimitive = when (value) {
        is String -> JsonPrimitive(value)
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        else -> error("${input.name}: unsupported ${value::class}")
    }
}

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
            tagline to "A tagline at its ninety character limit, to check that the header wraps and stays legibl",
            // With a tickets button too: a long venue must leave it room.
            ticketsUrl to "https://example.com/tickets"),
    )
}

/** One session: agenda rows, bookmarks, the "next up" widget and tile. */
object SessionContract : ConfettiContract("confetti/session", 1, 1, "Session card", DataSource.App, "session") {
    val title = string("title", required = true, maxLength = 120)
    val speakers = string("speakers", maxLength = 80, description = "names, comma separated; for templates before minor 1, which read speakerList")
    val room = string("room", maxLength = 30)
    val time = string("time", maxLength = 20, description = "already formatted, e.g. 10:30 – 11:10")
    val tag = string("tag", maxLength = 24)
    val live = bool("live")
    val bookmarked = bool("bookmarked")
    val open = action("open", "app:open-session")
    val bookmark = action("bookmark", "app:toggle-bookmark")

    /** The speakers, each its own tap target: a design unrolls up to [MAX_SPEAKERS] of them. */
    val speakerList = list("speakerList", max = MAX_SPEAKERS, since = 1, description = "the speakers, in billing order") {
        string("name", required = true, maxLength = 40)
        string("id", required = true, maxLength = 64, description = "what openSpeaker sends back")
    }
    val speakerName = speakerList.item.getValue("name")
    val speakerId = speakerList.item.getValue("id")

    /** One speaker tapped: the item's `id`. */
    val openSpeaker = action("openSpeaker", "app:open-speaker", arg = "id", since = 1)

    const val MAX_SPEAKERS = 4

    private fun people(vararg names: String) = names.mapIndexed { i, n -> mapOf(speakerName to n, speakerId to "speaker-${i + 1}") }

    override val examples get() = mapOf(
        "default" to mapOf(title to "Building adaptive apps with Compose", speakers to "Jane Doe, John Smith", room to "Hall 1",
            time to "10:30 – 11:10", tag to "compose", live to true, bookmarked to true,
            speakerList to people("Jane Doe", "John Smith")),
        "kotlinconf" to mapOf(title to "What's new in Kotlin 2.4", speakers to "Alex Kotlin", room to "Auditorium",
            time to "09:00 – 09:45", tag to "language", live to true, bookmarked to false,
            speakerList to people("Alex Kotlin")),
        "long" to mapOf(title to "A very long session title that keeps going to check how the card wraps at two lines and then ellipsizes the rest",
            speakers to "Speaker One, Speaker Two, Speaker Three, Speaker Four", room to "Room with a long name",
            time to "10:30 – 11:10", tag to "multiplatform",
            speakerList to people("A speaker name at forty characters long", "Speaker Two", "Speaker Three", "Speaker Four")),
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
        // Every input at its limit: the renders check the card still fits.
        "long" to mapOf(name to "A speaker name that runs to sixty characters, just to check!", tagline to
            "A job title and a company name that wraps over two lines and keeps on going on.",
            sessions to "Twenty-char sessions"),
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
        "long" to mapOf(eyebrow to "Twenty chars eyebrow", headline to "A headline that runs to forty-eight characters x",
            body to "A body that runs on for all of the one hundred and twenty characters it may use, to check that the whole promotion fits.",
            cta to "Twenty-char CTA text", url to "https://kotlinconf.com/workshops"),
    )
}

/**
 * A break in the agenda: coffee, lunch, registration, the party. Not a talk, so nothing to open or
 * bookmark: no actions, as Confetti's own row has none.
 */
object BreakContract : ConfettiContract("confetti/break", 1, 0, "Agenda break", DataSource.App, "break") {
    val title = string("title", required = true, maxLength = 60)
    val room = string("room", maxLength = 30)
    val time = string("time", maxLength = 20, description = "already formatted, e.g. 10:30 – 11:10")
    val coffee = bool("coffee", description = "a break between talks (coffee, lunch)")
    val social = bool("social", description = "a party or reception; neither flag is an information item, e.g. registration")
    val live = bool("live")

    override val examples get() = mapOf(
        "default" to mapOf(title to "Coffee break", room to "Foyer", time to "10:30 – 11:00", coffee to true, live to true),
        "kotlinconf" to mapOf(title to "Break", room to "Keynote, Room 14", time to "10:00 – 10:15", coffee to true),
        "social" to mapOf(title to "Conference party", room to "Main hall", time to "19:00 – 23:00", social to true),
        "long" to mapOf(title to "Registration and badge pickup, with breakfast at the door xx",
            room to "Room with a long name x", time to "07:30 – 09:00"),
    )
}

/** The top bar's title on a conference's screens: the conference's name on its agenda, a tab's name elsewhere. */
object TitleContract : ConfettiContract("confetti/title", 1, 0, "Top bar title", DataSource.App, "title") {
    val title = string("title", required = true, maxLength = 40)

    override val examples get() = mapOf(
        "default" to mapOf(title to "droidcon London 2026"),
        "kotlinconf" to mapOf(title to "KotlinConf 2026"),
        "long" to mapOf(title to "A conference name that is forty chars xx"),
    )
}

val AllContracts: List<ConfettiContract> = listOf(ConferenceContract, SessionContract, SpeakerContract, PromoContract, BreakContract, TitleContract)

/**
 * Which design family a conference is drawn in: the `series` tag every design carries, and what
 * the app asks for (`TemplateQuery.tags`). A conference with no series keeps Confetti's own UI.
 */
object ConferenceSeries {
    const val TAG = "series"

    private val prefixes = listOf("droidcon" to "droidcon", "kotlinconf" to "kotlinconf")

    /** Every conference series that can have designs of its own: one Livery catalog each (`catalogs/<series>/`). */
    val ALL: List<String> = prefixes.map { it.second }.distinct()

    /**
     * The base catalog (`catalogs/confetti/`): Confetti's own look, drawn by Livery, for every
     * conference without a catalog of its own.
     */
    const val BASE: String = "confetti"

    /** The catalog a conference draws from: its series', when there is one, else [BASE]. */
    fun catalogFor(conferenceId: String, available: Set<String>): String =
        of(conferenceId)?.takeIf { it in available } ?: BASE

    fun of(conferenceId: String): String? =
        prefixes.firstOrNull { (prefix, _) -> conferenceId.startsWith(prefix) }?.second

    /** The Livery segment a placement targets a conference's attendees with. */
    fun segment(conferenceId: String): String = "conference:$conferenceId"

    /** The theme pack for a series in the app's current mode: `droidcon-light`, `kotlinconf-dark`. */
    fun themePack(series: String, dark: Boolean): String = "$series-${if (dark) "dark" else "light"}"
}
