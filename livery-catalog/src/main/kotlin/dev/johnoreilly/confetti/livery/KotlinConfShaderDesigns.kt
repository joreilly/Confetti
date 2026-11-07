package dev.johnoreilly.confetti.livery

import androidx.compose.remote.core.CoreDocument
import androidx.compose.remote.core.RcPlatformServices
import androidx.compose.remote.core.RcProfiles
import androidx.compose.remote.core.RemoteContext
import androidx.compose.remote.core.operations.ColorAttribute
import androidx.compose.remote.core.operations.Header
import androidx.compose.remote.core.operations.Utils
import androidx.compose.remote.core.operations.layout.managers.BoxLayout
import androidx.compose.remote.core.operations.layout.managers.ColumnLayout
import androidx.compose.remote.core.operations.layout.managers.CoreText
import androidx.compose.remote.core.operations.layout.managers.RowLayout
import androidx.compose.remote.core.operations.layout.modifiers.HostNamedActionOperation
import androidx.compose.remote.creation.RemoteComposeWriter
import androidx.compose.remote.creation.RemoteComposeWriter.hTag
import androidx.compose.remote.creation.actions.HostAction
import androidx.compose.remote.creation.modifiers.RecordingModifier
import androidx.compose.remote.creation.modifiers.RoundedRectShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

/*
 * The shader experiment: KotlinConf's header with an AGSL mesh gradient behind it instead of the
 * radial-gradient mesh (meshGlows) the default design layers up. rc-players' CMP fork can't write a
 * shader yet (rc-players#826 adds it, unreleased), so this design is written one level down, with
 * AndroidX's own RemoteComposeWriter: the same wire format, built by hand.
 *
 * It is a design of its own, `variant: shader`, that only an experiment arm asks for
 * (experiments/kotlinconf-header-shader.json): a query without a design id draws the `default`
 * variant, so nobody outside the experiment sees it. It has two templates:
 *
 *  - `in-app-shader`, with the shader. Livery's publisher records `features: [shaders]` for any
 *    document with ShaderData, and a player only gets the template if it says it runs them
 *    (PlayerCapabilities.features; Confetti says so on Android 13+, see LiverySlots).
 *  - `in-app`, the fallback for every other player: KotlinConf's default header, as is. Within a
 *    design Livery prefers the template with the narrower size range, so the shader template
 *    caps its width (which a header never reaches) and wins wherever it's eligible.
 */

/** The experiment's designs, captured into catalogs/kotlinconf alongside KotlinConf's own. */
val KotlinConfShaderDesigns: List<DesignDef> = listOf(
    DesignDef(
        "kotlinconf-header-shader", ConferenceContract, KotlinConf, "KotlinConf header (AGSL shader)", "kotlinconf",
        templates = listOf(
            TemplateDef("in-app-shader", IN_APP_SHADER, 360, 290, raw = { kotlinConfShaderHeader(it, 360, 290) }),
            // Players without shaders: the default design's document, the same capture.
            TemplateDef("in-app", IN_APP, 360, 290, content = { KotlinConfHeader() }),
        ),
        variant = "shader",
    ),
)

/** In-app, the same as [IN_APP] but for a bounded width: narrower, so preferred where both fit. */
private const val IN_APP_SHADER =
    """{ "surface": "in-app", "rcApiLevel": 7, "profiles": ["androidx"], "size": { "minWidthDp": 280, "maxWidthDp": 4000 } }"""

/**
 * A slow mesh gradient: three soft lights in the theme's secondary and tertiary drifting over its
 * primary, with a faint diagonal sheen. `time` is the player's animation clock (so renders are
 * deterministic), `size` the painted area in pixels, the colours the theme roles a theme pack sets.
 */
internal const val MeshGradientAgsl = """
uniform float2 size;
uniform float time;
uniform float3 base;
uniform float3 glowA;
uniform float3 glowB;

float glow(float2 uv, float2 c, float r) {
    float d = length(uv - c) / r;
    return exp(-d * d * 2.2);
}

half4 main(float2 p) {
    float aspect = size.x / max(size.y, 1.0);
    float2 uv = float2(p.x / max(size.x, 1.0) * aspect, p.y / max(size.y, 1.0));
    float t = time * 0.21;
    float2 a = float2(aspect * (0.82 + 0.10 * sin(t)), 0.18 + 0.14 * cos(t * 1.3));
    float2 b = float2(aspect * (0.16 + 0.10 * cos(t * 0.8 + 2.5)), 0.78 + 0.12 * sin(t * 1.1));
    float2 c = float2(aspect * (0.38 + 0.16 * sin(t * 0.6 + 5.0)), 0.05 + 0.10 * cos(t * 0.9));
    float3 col = base;
    col = mix(col, glowA, 0.85 * glow(uv, a, 0.75));
    col = mix(col, glowB, 0.75 * glow(uv, b, 0.70));
    col = mix(col, glowA, 0.35 * glow(uv, c, 0.55));
    col += 0.035 * sin((uv.x + uv.y) * 5.0 - t * 2.0);
    return half4(half3(clamp(col, 0.0, 1.0)), 1.0);
}
"""

/**
 * KotlinConf's header over [MeshGradientAgsl], written with [RemoteComposeWriter]: the dates
 * and live pill, name, tagline and venue, and the tickets button when there is a ticketsUrl (the
 * default design's layout, without its turning "K" so the arms differ in the background only). The inputs are
 * named `USER:<input>` exactly as the Compose capture names them, so Livery binds both the same.
 */
fun kotlinConfShaderHeader(scope: TemplateScope, widthDp: Int, heightDp: Int): ByteArray {
    val contract = ConferenceContract
    val brand = KotlinConf
    val writer = RemoteComposeWriter(
        RcPlatformServices.None, 8,
        hTag(Header.DOC_WIDTH, widthDp),
        hTag(Header.DOC_HEIGHT, heightDp),
        hTag(Header.DOC_CONTENT_DESCRIPTION, ""),
        // AndroidX: the profile ShaderData is part of (widgets and Wear players don't read it).
        hTag(Header.DOC_PROFILES, RcProfiles.PROFILE_ANDROIDX),
        // Sizes in dp, as the Compose captures write them.
        hTag(Header.DOC_DENSITY_BEHAVIOR, CoreDocument.DENSITY_BEHAVIOR_DP),
    )
    val inputs = Inputs(writer, scope)
    val primary = inputs.color(contract.inputs.getValue("theme.primary"), brand.primary)
    val onPrimary = inputs.color(contract.inputs.getValue("theme.onPrimary"), brand.onPrimary)
    val secondary = inputs.color(contract.inputs.getValue("theme.secondary"), brand.secondary)
    val tertiary = inputs.color(contract.inputs.getValue("theme.tertiary"), brand.tertiary)
    val dates = inputs.string(contract.dates)
    val live = inputs.bool(contract.live)
    val name = inputs.string(contract.name)
    val tagline = inputs.string(contract.tagline)
    val location = inputs.string(contract.location)
    val ticketsUrl = inputs.string(contract.ticketsUrl)
    scope.emit(contract.tickets)
    val font = scope.font()

    // The tickets button only when there is a URL: visible = length(url) > 0, as the Compose
    // capture writes `isNotEmpty.visibleIf()`.
    val urlLength = Utils.idFromNan(writer.textLength(ticketsUrl))
    val ticketsVisible = Utils.idFromLong(
        writer.integerExpression(
            0b1110000, 0, 1, 0, 1, urlLength, IntegerOp.SelectIfGt, IntegerOp.SelectIfGt,
        ),
    ).toInt()
    // The live pill while `live` is true: select(live, VISIBLE, GONE), as `visibleIf()` writes it.
    val liveVisible = Utils.idFromLong(writer.integerExpression(0b1100, 0, 1, live, IntegerOp.SelectIfGt)).toInt()
    // The action's argument: {"url":"<ticketsUrl>"} (Livery maps a bare value only to `value`).
    val ticketsArg = writer.textMerge(writer.textMerge(writer.addText("{\"url\":\""), ticketsUrl), writer.addText("\"}"))

    fun channel(color: Int, attribute: Short) = writer.getColorAttribute(color, attribute)
    fun rgb(color: Int) = floatArrayOf(
        channel(color, ColorAttribute.COLOR_RED), channel(color, ColorAttribute.COLOR_GREEN), channel(color, ColorAttribute.COLOR_BLUE),
    )
    val base = rgb(primary)
    val glowA = rgb(secondary)
    val glowB = rgb(tertiary)

    fun text(id: Int, size: Float, weight: Float, maxLines: Int = 1, color: Int = onPrimary, modifier: RecordingModifier = RecordingModifier()) {
        writer.startTextComponent(
            modifier, id, color, size, 0, weight, font,
            /* flags: the colour is an id */ 1.toShort(), CoreText.TEXT_ALIGN_START.toShort(), CoreText.OVERFLOW_ELLIPSIS, maxLines,
        )
        writer.endTextComponent()
    }

    writer.root {
        writer.box(
            RecordingModifier().fillMaxWidth().clip(RoundedRectShape(24f, 24f, 24f, 24f)).backgroundId(primary)
                // The shader as the card's background: drawn at the card's own size, then the
                // content over it (a canvas child would stretch the card to the document's height).
                .drawWithContent {
                    val width = writer.addComponentWidthValue()
                    val height = writer.addComponentHeightValue()
                    val shader = writer.createShader(MeshGradientAgsl)
                        .setFloatUniform("size", width, height)
                        .setFloatUniform("time", RemoteContext.FLOAT_ANIMATION_TIME)
                        .setFloatUniform("base", base)
                        .setFloatUniform("glowA", glowA)
                        .setFloatUniform("glowB", glowB)
                        .commit()
                    writer.rcPaint.setShader(shader).commit()
                    writer.drawRect(0f, 0f, width, height)
                    writer.rcPaint.setShader(0).commit()
                    writer.drawComponentContent()
                },
            BoxLayout.START, BoxLayout.TOP,
        ) {
            writer.column(RecordingModifier().fillMaxWidth().padding(22f).spacedBy(6f), ColumnLayout.START, ColumnLayout.TOP) {
                writer.row(RecordingModifier().spacedBy(8f), RowLayout.START, RowLayout.CENTER) {
                    text(dates, 14f, 600f)
                    writer.box(
                        RecordingModifier().visibility(liveVisible).clip(RoundedRectShape(50f, 50f, 50f, 50f))
                            .backgroundId(inputs.alpha(onPrimary, 0.8f)).padding(8f, 2f, 8f, 2f),
                        BoxLayout.START, BoxLayout.TOP,
                    ) { text(writer.addText("Live now"), 11f, 700f, color = primary) }
                }
                text(name, 32f, 900f, maxLines = 2)
                text(tagline, 15f, 400f)
                writer.column(RecordingModifier().padding(0f, 8f, 0f, 0f).spacedBy(8f), ColumnLayout.START, ColumnLayout.TOP) {
                    writer.box(
                        RecordingModifier().clip(RoundedRectShape(50f, 50f, 50f, 50f))
                            .backgroundId(inputs.alpha(onPrimary, 0.2f)).padding(10f, 4f, 10f, 4f),
                        BoxLayout.START, BoxLayout.TOP,
                    ) { text(location, 12f, 600f) }
                    writer.box(
                        RecordingModifier().visibility(ticketsVisible).clip(RoundedRectShape(50f, 50f, 50f, 50f))
                            .backgroundId(onPrimary)
                            .onClick(HostAction(contract.tickets.name, HostNamedActionOperation.STRING_TYPE, ticketsArg))
                            .padding(14f, 5f, 14f, 5f),
                        BoxLayout.START, BoxLayout.TOP,
                    ) { text(writer.addText("Get tickets"), 13f, 700f, color = primary) }
                }
            }
        }
    }
    return writer.encodeToByteArray()
}

/** Contract inputs as the writer's named values, recorded on the [scope] as the Compose helpers do. */
private class Inputs(val writer: RemoteComposeWriter, val scope: TemplateScope) {
    fun string(input: Input): Int {
        require(input.type == InputType.String) { "${input.name} is ${input.type}" }
        scope.read(input)
        // The contract's default, never example data (see `input` in Authoring.kt).
        return writer.addNamedString("USER:${input.name}", input.default?.jsonPrimitive?.contentOrNull ?: "")
    }

    fun color(input: Input, initial: Color): Int {
        require(input.type == InputType.Color) { "${input.name} is ${input.type}" }
        scope.read(input)
        return writer.addNamedColor("USER:${input.name}", initial.toArgb())
    }

    fun bool(input: Input): Int {
        require(input.type == InputType.Bool) { "${input.name} is ${input.type}" }
        scope.read(input)
        val initial = input.default?.jsonPrimitive?.booleanOrNull ?: false
        // A named int, 0 or 1: the Compose capture's named boolean on the wire.
        return Utils.idFromLong(writer.addNamedInt("USER:${input.name}", if (initial) 1 else 0)).toInt()
    }

    /** [color] at [alpha]: an ARGB expression of its channels, so it follows the theme pack. */
    fun alpha(color: Int, alpha: Float): Int {
        fun channel(attribute: Short) = writer.getColorAttribute(color, attribute)
        return writer.addColorExpression(
            alpha, channel(ColorAttribute.COLOR_RED), channel(ColorAttribute.COLOR_GREEN), channel(ColorAttribute.COLOR_BLUE),
        ).toInt() and 0xFFFF
    }
}

/** IntegerExpression operators, as the Compose capture writes them (`select(len > 0, 1, 0)`). */
private object IntegerOp {
    const val SelectIfGt = 0x10016
}
