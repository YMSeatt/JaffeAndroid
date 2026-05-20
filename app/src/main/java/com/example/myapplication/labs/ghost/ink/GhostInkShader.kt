package com.example.myapplication.labs.ghost.ink

import org.intellij.lang.annotations.Language

/**
 * GhostInkShader: Neural Ink AGSL Shader.
 *
 * This shader provides a glowing, procedurally animated effect for persistent
 * classroom annotations. It simulates "Energy Ink" that pulses and exhibits
 * minor data-inspired artifacts.
 */
object GhostInkShader {

    @Language("AGSL")
    const val NEURAL_INK_SHADER = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[64];
        uniform int iPointCount;
        uniform float4 iColor;
        uniform float iIntensity;

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float sdSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float d = 1000.0;

            // Loop through points to find distance to the stroke segment
            for (int i = 0; i < iPointCount - 1; i++) {
                if (i >= 63) break;
                d = min(d, sdSegment(fragCoord, iPoints[i], iPoints[i+1]));
            }

            // Glowing line effect
            float glow = iIntensity / (d + 2.0);
            glow = pow(glow, 0.9);

            // Subtle pulsing based on time
            float pulse = 0.9 + 0.1 * sin(iTime * 3.0);
            glow *= pulse;

            // Color synthesis
            float3 baseColor = iColor.rgb * glow;

            // Add "Neural Flicker"
            float flicker = hash(fragCoord.x * fragCoord.y * iTime);
            if (flicker > 0.99) {
                baseColor += float3(1.0, 1.0, 1.0) * 0.3 * glow;
            }

            return half4(baseColor, clamp(glow, 0.0, 1.0) * iColor.a);
        }
    """
}
