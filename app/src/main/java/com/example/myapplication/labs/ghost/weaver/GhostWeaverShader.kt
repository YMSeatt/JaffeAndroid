package com.example.myapplication.labs.ghost.weaver

import org.intellij.lang.annotations.Language

/**
 * GhostWeaverShader: AGSL shader for the "Neural Thread" effect.
 *
 * Visualizes academic connections as glowing, interwoven lines.
 */
object GhostWeaverShader {

    @Language("AGSL")
    const val NEURAL_THREAD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPointA;
        uniform float2 iPointB;
        uniform float iStrength;
        uniform float3 iColor;

        float line(float2 p, float2 a, float2 b, float width) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h) - width;
        }

        half4 main(float2 fragCoord) {
            // Base line distance
            float d = line(fragCoord, iPointA, iPointB, 1.5);

            // Sine-wave weaver effect
            float2 dir = normalize(iPointB - iPointA);
            float2 normal = float2(-dir.y, dir.x);
            float distAlong = dot(fragCoord - iPointA, dir);
            float wave = sin(distAlong * 0.05 - iTime * 5.0) * 5.0;

            float dWave = line(fragCoord, iPointA + normal * wave, iPointB + normal * wave, 1.0);

            float glow = exp(-abs(d) * 0.2) * iStrength;
            float waveGlow = exp(-abs(dWave) * 0.3) * iStrength * 0.5;

            float3 finalColor = iColor * (glow + waveGlow);

            return half4(finalColor, (glow + waveGlow) * 0.8);
        }
    """
}
