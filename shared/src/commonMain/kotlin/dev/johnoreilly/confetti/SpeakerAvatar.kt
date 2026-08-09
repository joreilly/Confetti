package dev.johnoreilly.confetti

import dev.johnoreilly.confetti.fragment.SpeakerDetails

/**
 * The avatar URL to render for this speaker on the current platform.
 *
 * Native platforms use [SpeakerDetails.photoUrl] (the original Sessionize-hosted image) directly.
 * The web target instead routes through the backend's proxied [SpeakerDetails.photoUrlThumbnail],
 * since Sessionize's CDN rejects the same URL when fetched via browser fetch()/XHR from a foreign
 * origin (likely anti-hotlink/bot protection) even though it works fine via direct navigation or a
 * native HTTP client - the proxy sidesteps that, at the cost of an extra hop native platforms don't
 * need to pay for.
 */
expect fun SpeakerDetails.avatarUrl(): String?
