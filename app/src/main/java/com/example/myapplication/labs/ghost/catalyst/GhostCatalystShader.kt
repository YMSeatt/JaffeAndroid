package com.example.myapplication.labs.ghost.catalyst

import org.intellij.lang.annotations.Language

/**
 * GhostCatalystShader: AGSL scripts for visualizing behavioral chain reactions.
 *
 * It features "Ionic Bonds" (glowing lines) and "Reaction Bubbles" (circular
 * distortions) to simulate a chemical environment.
 */
object GhostCatalystShader {
    @Language("AGSL")
    const val REACTION_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;
        uniform float iRate; // 0..1 Reaction Rate

        float hash(float2 p) {
            p = fract(p * float2(123.34, 456.21));
            p += dot(p, p + 45.32);
            return fract(p.x * p.y);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float3 finalColor = float3(0.0);

            // 1. Procedural "Reaction Bubbles" (Effervescence)
            for (float i = 0.0; i < 4.0; i++) {
                float2 pos = float2(hash(float2(i, 1.0)), hash(float2(i, 2.0)));
                pos.y = fract(pos.y + iTime * 0.1 * (i + 1.0)); // Rise up

                float d = distance(uv, pos);
                float size = 0.01 + 0.02 * hash(float2(i, 3.0));
                float bubble = smoothstep(size, size - 0.005, d);
                bubble *= (0.5 + 0.5 * sin(iTime * 2.0 + i));

                finalColor += iColor * bubble * iRate;
            }

            // 2. Heat haze / Brownian motion distortion
            float2 distort = float2(
                sin(uv.y * 10.0 + iTime) * 0.002,
                cos(uv.x * 10.0 + iTime) * 0.002
            ) * iRate;

            // 3. Ionic Background Glow
            float glow = 0.05 / (distance(uv, float2(0.5, 0.5)) + 0.5);
            finalColor += iColor * glow * 0.2;

            return float4(finalColor, length(finalColor) * 0.8);
        }
    """

    @Language("AGSL")
    const val IONIC_BOND = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;
        uniform float iIntensity;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Pulsating glowing line effect
            float pulse = sin(iTime * 5.0 + uv.x * 10.0) * 0.5 + 0.5;
            float3 color = iColor * pulse * iIntensity;

            // Add "sparkle" noise
            if (fract(sin(dot(uv, float2(12.9898, 78.233))) * 43758.5453) > 0.98) {
                color += 0.5 * iIntensity;
            }

            return float4(color, 0.9 * iIntensity);
        }
    """
}
