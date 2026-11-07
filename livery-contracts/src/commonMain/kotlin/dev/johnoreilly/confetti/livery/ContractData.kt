package dev.johnoreilly.confetti.livery

import kotlinx.serialization.json.JsonObject

/*
 * Typed data for the app-filled contracts: what a screen hands a slot, whichever side draws it.
 *
 * The app's own model (Apollo's generated `SessionDetails`, `SpeakerDetails`) can't implement an
 * interface we define, and it has fields no design should see (ids, abstracts, social links). So
 * each contract gets a plain data class, and the app *copies* into it with one mapping function per
 * model (see shared's ConferenceSlots.kt). That copy is the only place the app's model meets a
 * design:
 *
 * - the fields are the contract's non-theme inputs, so a design can't read anything else;
 * - [values] is where every value is trimmed to fit ([Input.fit]): an over-long value would fail
 *   binding and drop the whole slot to its fallback;
 * - ContractDataTest fails when the contract gains an input its data class doesn't fill.
 *
 * The native (fallback) composable can take the same class, so both arms of an experiment draw
 * the same data and an old composable can be deleted without touching the screen around it.
 */
sealed interface ContractData {
    val contract: ConfettiContract

    /** The values keyed by the contract's own inputs, already fitted. */
    fun values(): Map<Input, Any>

    /** The JSON a slot binds: exactly what a template sees. */
    fun toJson(): JsonObject = contract.data(values())
}

private fun MutableMap<Input, Any>.putText(input: Input, value: String?) {
    value?.takeIf { it.isNotBlank() }?.let { put(input, input.fit(it)) }
}

/** `confetti/conference@1`. */
data class ConferenceCard(
    val name: String,
    val dates: String,
    val location: String? = null,
    val tagline: String? = null,
    val live: Boolean = false,
    val ticketsUrl: String? = null,
) : ContractData {
    override val contract get() = ConferenceContract

    override fun values(): Map<Input, Any> = buildMap {
        val c = ConferenceContract
        put(c.name, c.name.fit(name))
        put(c.dates, c.dates.fit(dates))
        putText(c.location, location)
        putText(c.tagline, tagline)
        put(c.live, live)
        // Never trimmed: a cut URL goes somewhere else.
        ticketsUrl?.takeIf { it.length <= (c.ticketsUrl.maxLength ?: Int.MAX_VALUE) }?.let { put(c.ticketsUrl, it) }
    }
}

/** `confetti/session@1`. */
data class SessionCard(
    val title: String,
    val speakers: String? = null,
    val room: String? = null,
    val time: String? = null,
    val tag: String? = null,
    val live: Boolean = false,
    val bookmarked: Boolean = false,
) : ContractData {
    override val contract get() = SessionContract

    override fun values(): Map<Input, Any> = buildMap {
        val c = SessionContract
        put(c.title, c.title.fit(title))
        putText(c.speakers, speakers)
        putText(c.room, room)
        putText(c.time, time)
        putText(c.tag, tag)
        put(c.live, live)
        put(c.bookmarked, bookmarked)
    }
}

/** `confetti/speaker@1`. */
data class SpeakerCard(
    val name: String,
    val tagline: String? = null,
    /** Only https binds (Livery PROTOCOL §6.1); anything else draws the design's placeholder. */
    val photoUrl: String? = null,
    val sessions: String? = null,
) : ContractData {
    override val contract get() = SpeakerContract

    override fun values(): Map<Input, Any> = buildMap {
        val c = SpeakerContract
        put(c.name, c.name.fit(name))
        putText(c.tagline, tagline)
        photoUrl?.takeIf { it.startsWith("https://") }?.let { put(c.photo, it) }
        putText(c.sessions, sessions)
    }
}

/**
 * The first A/B test: Livery's conference designs against Confetti's own composables, the ones the
 * slots fall back to anyway. Run with Firebase A/B Testing over Remote Config: one parameter per
 * conference series, `sdui_<series>` (`sdui_droidcon`, `sdui_kotlinconf`), set to an arm. The app
 * reads the arm before drawing (`SduiArms`), so the native arm never loads Livery, and every
 * event from either arm carries `exp_sdui` with the same contract action names.
 */
object SduiExperiment {
    const val ID = "sdui"

    /** Confetti's own Compose UI: no Livery slot draws. */
    const val NATIVE = "native"

    /** The conference's Livery designs, falling back to the composables where there's no template. */
    const val LIVERY = "livery"

    val ARMS = listOf(NATIVE, LIVERY)

    /** The design id native-arm events carry, so analytics can split taps by who drew them. */
    const val NATIVE_DESIGN = "native"
}
