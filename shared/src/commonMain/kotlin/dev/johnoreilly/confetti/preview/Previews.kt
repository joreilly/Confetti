package dev.johnoreilly.confetti.preview

import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Multi-preview annotation that fans a single @Composable preview across
 * three reference device sizes:
 *
 *  * Phone (small)    — 360 x 640 dp, similar to a compact Pixel 4a.
 *  * Phone (large)    — 411 x 914 dp, similar to a Pixel 6 / 7.
 *  * Tablet landscape — 960 x 600 dp, similar to a 7" tablet held sideways.
 *
 * Apply alongside `@Composable` on a stateless preview function and the
 * compose-preview renderer will emit one PNG per device.
 */
@Preview(name = "Phone (small)", widthDp = 360, heightDp = 640, showBackground = true)
@Preview(name = "Phone (large)", widthDp = 411, heightDp = 914, showBackground = true)
@Preview(name = "Tablet landscape", widthDp = 960, heightDp = 600, showBackground = true)
annotation class MobilePreviews
