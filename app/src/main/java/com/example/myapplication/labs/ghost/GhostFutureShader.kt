package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostFutureShader: AGSL shader for the Temporal Warp visualization.
 *
 * It features an amber-tinted digital interface with scanlines, chromatic
 * aberration, and jittery time-lapse effects to simulate "Neural Future Vision."
 */
object GhostFutureShader {
    @Language("AGSL")
    const val TEMPORAL_WARP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity;

        float random(float2 st) {
            return fract(sin(dot(st.xy, float2(12.9898, 78.233))) * 43758.5453123);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float time = iTime * 2.0;

            // Chromatic aberration (horizontal)
            float shift = 0.005 * iIntensity;
            float r = 0.8; // Fixed red-ish tint for Red Channel
            float g = 0.6; // Amber
            float b = 0.2; // Amber

            // Jittery time-lapse effect
            float2 jitterUv = uv;
            if (random(float2(time, 0.0)) > 0.98) {
                jitterUv.x += (random(float2(time, 1.0)) - 0.5) * 0.02;
                jitterUv.y += (random(float2(time, 2.0)) - 0.5) * 0.02;
            }

            // Scanlines
            float scanline = sin(uv.y * 400.0 + time * 10.0) * 0.04;

            // Amber Digital Tint
            half4 color = half4(r, g, b, 1.0);

            // Pulse based on time
            float pulse = 0.05 * sin(time * 0.5) + 0.1;
            color.a = (0.15 + pulse + scanline) * iIntensity;

            // Vignette
            float dist = distance(uv, float2(0.5, 0.5));
            color.a *= (1.2 - dist);

            return color;
        }
    """
}
