package dev.johnoreilly.confetti

import android.net.Uri

data class DeepLinkInfo(
    val conferenceId: String,
    val sessionId: String? = null,
)

/**
 * From a deep link like `https://confetti-app.dev/conference/devfeststockholm2023`
 * or `https://confetti-app.dev/conference/devfeststockholm2023/session/123`
 * extracts conference and optional session IDs.
 */
fun Uri.extractDeepLinkInfoOrNull(): DeepLinkInfo? {
    if (host != "confetti-app.dev") return null
    val path = path ?: return null
    if (path.firstOrNull() != '/') return null
    val parts = path.substring(1).split('/')
    if (parts.getOrNull(0) != "conference") return null
    val conferenceId = parts.getOrNull(1) ?: return null
    if (!conferenceId.all { it.isLetterOrDigit() }) return null

    val sessionId = if (parts.getOrNull(2) == "session") {
        parts.getOrNull(3)
    } else {
        null
    }

    return DeepLinkInfo(conferenceId = conferenceId, sessionId = sessionId)
}
