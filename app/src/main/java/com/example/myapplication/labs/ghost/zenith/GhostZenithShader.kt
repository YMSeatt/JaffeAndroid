package com.example.myapplication.labs.ghost.zenith

import org.intellij.lang.annotations.Language

/**
 * GhostZenithShader: AGSL code for the "Neural Sea" background.
 *
 * This shader renders a multi-layered procedural background that reacts to
 * device tilt. It simulates a deep, luminous environment with "data currents"
 * flowing at different depths.
 */
object GhostZenithShader {

    @Language("AGSL")
    const val NEURAL_SEA = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iTilt; // x: pitch, y: roll
        uniform float3 iBaseColor;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
        }

        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            for (int i = 0; i < 3; ++i) {
                v += a * noise(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Layer 1: Deep currents (Slow, low parallax)
            float2 uv1 = uv * 2.0 + iTilt * 0.1;
            float n1 = fbm(uv1 + iTime * 0.05);

            // Layer 2: Mid-level luminescence (Medium speed, medium parallax)
            float2 uv2 = uv * 4.0 + iTilt * 0.3;
            float n2 = fbm(uv2 - iTime * 0.1);

            // Layer 3: Surface ripples (Fast, high parallax)
            float2 uv3 = uv * 8.0 + iTilt * 0.6;
            float n3 = fbm(uv3 + iTime * 0.2);

            half3 color = iBaseColor * 0.2; // Background darkness

            // Blend layers based on noise intensity and tilt-derived depth
            color += iBaseColor * n1 * 0.3;
            color += half3(0.0, 0.5, 0.5) * n2 * 0.2;
            color += half3(0.5, 0.0, 0.5) * n3 * 0.1;

            // Add a "Horizon" glow based on tilt
            float horizon = smoothstep(0.4, 0.0, abs(uv.y - 0.5 + iTilt.x * 0.2));
            color += iBaseColor * horizon * 0.1;

            return half4(color, 1.0);
        }
    """
}
