package com.example.myapplication.labs.ghost.flare

import org.intellij.lang.annotations.Language

/**
 * GhostFlareShader: AGSL source for high-intensity behavioral flares.
 *
 * Implements an anamorphic lens flare with:
 * 1. Central Starburst (radial rays)
 * 2. Horizontal Lens Streak (anamorphic distortion)
 * 3. Chromatic Ring (dispersion effect)
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

        float starburst(float2 uv, float intensity) {
            float d = length(uv);
            float m = 0.05 / d;

            // Add rays
            float rays = max(0.0, 1.0 - abs(uv.x * uv.y * 1000.0));
            m += rays * 0.5;

            // Rotate rays over time
            float a = atan(uv.y, uv.x);
            m += pow(abs(sin(a * 8.0 + iTime * 2.0)), 10.0) * 0.2;

            return m * intensity * iLife;
        }

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
