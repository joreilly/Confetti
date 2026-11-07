package dev.johnoreilly.confetti.livery

import androidx.compose.remote.creation.compose.shaders.RemoteBrush
import androidx.compose.remote.creation.compose.shaders.runtimeShader
import androidx.compose.remote.creation.compose.state.RemoteColor
import androidx.compose.remote.creation.compose.state.RemoteTimeVariables

/*
 * The shader experiment: KotlinConf's header over an AGSL mesh gradient instead of meshGlows.
 * Only experiments/header_shader.json asks for this design (variant `shader`). Its `in-app-shader`
 * template is published as `features: [shaders]`, so players without shaders get `in-app`, the
 * default design's document. Livery prefers the narrower size range, hence the width cap.
 */
val KotlinConfShaderDesigns: List<DesignDef> = listOf(
    DesignDef(
        "kotlinconf-header-shader", ConferenceContract, KotlinConf, "KotlinConf header (AGSL shader)", "kotlinconf",
        templates = listOf(
            TemplateDef("in-app-shader", IN_APP_SHADER, 360, 290) { KotlinConfHeader(shader = true) },
            TemplateDef("in-app", IN_APP, 360, 290) { KotlinConfHeader() },
        ),
        variant = "shader",
    ),
)

private const val IN_APP_SHADER =
    """{ "surface": "in-app", "rcApiLevel": 7, "profiles": ["androidx"], "size": { "minWidthDp": 280, "maxWidthDp": 4000 } }"""

/** The theme's primary, secondary and tertiary as the gradient's colours, on the player's clock. */
internal fun meshGradientShader(c: BrandColors): RemoteBrush = RemoteBrush.runtimeShader(
    MeshGradientAgsl,
    floatUniforms = mapOf(
        "time" to listOf(RemoteTimeVariables.animationTime),
        "base" to c.primary.rgb(),
        "glowA" to c.secondary.rgb(),
        "glowB" to c.tertiary.rgb(),
    ),
)

private fun RemoteColor.rgb() = listOf(red, green, blue)

/** Three soft lights drifting over a base colour, with a faint sheen. `size` is added by the brush. */
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
