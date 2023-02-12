package dev.johnoreilly.confetti.wear.tile

import dev.johnoreilly.confetti.analytics.AnalyticsEvent

class TileAnalyticsEvent(val type: Type, val conference: String? = null) : AnalyticsEvent {
    override val id: String = "tile_${type.name.lowercase()}"

    override val properties: Map<String, Any> = buildMap {
        if (conference != null) {
            put("conference", conference)
        }
    }

    enum class Type {
        Add, Enter, Leave, Remove
    }
}