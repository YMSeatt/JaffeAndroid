package com.example.myapplication.labs.ghost.carbon

import org.intellij.lang.annotations.Language

/**
 * GhostCarbonShader: AGSL shader for the "Identical Resonance" effect.
 *
 * Visualizes the connections between behavioral twins using glowing,
 * pulsing neural bridges.
 */
object GhostCarbonShader {

    @Language("AGSL")
    const val RESONANCE_BRIDGE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPointA;
        uniform float2 iPointB;
        uniform float iStrength;

        float line(float2 p, float2 a, float2 b, float width) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h) - width;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Bridge line
            float d = line(fragCoord, iPointA, iPointB, 2.0);

            // Pulsing effect
            float pulse = 0.5 + 0.5 * sin(iTime * 3.0 + length(fragCoord - iPointA) * 0.01);
            float glow = exp(-abs(d) * 0.1) * pulse * iStrength;

            // Color: Carbon Gray with Cyan/Purple resonance
            float3 baseColor = float3(0.2, 0.8, 0.9); // Cyan-ish
            float3 glowColor = baseColor * glow;

            return half4(glowColor, glow * 0.8);
        }
    """
}
