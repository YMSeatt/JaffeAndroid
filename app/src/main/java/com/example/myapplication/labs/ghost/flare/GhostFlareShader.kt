package com.example.myapplication.labs.ghost.flare

import org.intellij.lang.annotations.Language

/**
 * GhostFlareShader: AGSL source for the "Neural Flare" visual effect.
 *
 * This shader implements an anamorphic lens flare optimized for real-time mobile rendering.
 * It consists of three primary visual layers:
 * 1. **Central Starburst**: High-frequency radial rays that rotate and pulse.
 * 2. **Horizontal Lens Streak**: Simulates the horizontal dispersion typical of anamorphic lenses.
 * 3. **Chromatic Ring**: A soft dispersive halo that shifts colors toward the edges.
 *
 * ### Shader Uniforms:
 * - `iResolution`: The dimensions of the rendering surface.
 * - `iCenter`: The 2D logical center of the flare in logical canvas units.
 * - `iIntensity`: Global brightness multiplier.
 * - `iLife`: Normalized lifecycle progress (1.0 -> 0.0).
 * - `iColor`: The primary color of the flare (e.g., Gold or Cyan).
 * - `iTime`: Absolute system time used to drive ray rotation and pulsing.
 */
object GhostFlareShader {

    @Language("AGSL")
    val FLARE = """
        uniform float2 iResolution;
        uniform float2 iCenter;
        uniform float iIntensity;
        uniform float iLife;
        uniform float4 iColor;
        uniform float iTime;

        /**
         * Generates the radial starburst component.
         *
         * Uses a 1/d core with a 1000x ray multiplier to create thin, sharp spokes.
         * The `pow(..., 10.0)` factor isolates the rays into 8 distinct spokes.
         */
        float starburst(float2 uv, float intensity) {
            float d = length(uv);
            float m = 0.05 / d;

            // Add sharp static rays
            float rays = max(0.0, 1.0 - abs(uv.x * uv.y * 1000.0));
            m += rays * 0.5;

            // Add rotating dynamic rays (8 spokes)
            float a = atan(uv.y, uv.x);
            m += pow(abs(sin(a * 8.0 + iTime * 2.0)), 10.0) * 0.2;

            return m * intensity * iLife;
        }

        /**
         * Generates the horizontal anamorphic streak.
         *
         * The `0.002 / abs(uv.y)` factor creates an infinitely thin horizontal line,
         * which is then constrained by a `smoothstep` on the X axis to prevent
         * full-screen bleeding.
         */
        float anamorphicStreak(float2 uv, float intensity) {
            float s = 0.002 / abs(uv.y);
            s *= smoothstep(0.8, 0.0, abs(uv.x));
            return s * intensity * iLife;
        }

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - iCenter) / iResolution.y;

            float m = starburst(uv, iIntensity);
            m += anamorphicStreak(uv, iIntensity);

            // Chromatic ring
            float d = length(uv);
            float ring = smoothstep(0.15, 0.14, d) * smoothstep(0.12, 0.13, d);

            half3 finalColor = iColor.rgb * m;
            finalColor += half3(0.0, 0.5, 1.0) * ring * iIntensity * iLife * 0.3;

            return half4(finalColor, m * iColor.a);
        }
    """.trimIndent()
}
