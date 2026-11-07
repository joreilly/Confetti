package dev.johnoreilly.confetti.ui.livery

import dev.johnoreilly.confetti.analytics.AnalyticsEvent

/**
 * One of Livery's flat analytics events (`livery_impression`, `livery_action`, `livery_exposure`,
 * … with `design_id`, `action`, `exp_sdui`, … parameters), through Confetti's own
 * [dev.johnoreilly.confetti.analytics.AnalyticsLogger], so every platform's pipeline gets them.
 */
data class LiveryAnalyticsEvent(override val id: String, override val properties: Map<String, Any>) : AnalyticsEvent
