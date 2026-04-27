package com.example.myapplication.labs.ghost.sync

import org.intellij.lang.annotations.Language

/**
 * GhostSyncShader: AGSL effects for Neural Sync.
 *
 * Visualizes collaboration as glowing interference patterns and bridges.
 */
object GhostSyncShader {

    @Language("AGSL")
    val NEURAL_BRIDGE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPosA;
        uniform float2 iPosB;
        uniform float iStrength;
        uniform float3 iColor;

        float lineSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        fixed4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord;

            // Neural Bridge Line
            float d = lineSegment(p, iPosA, iPosB);

            // Pulsing interference pattern
            float pulse = sin(d * 0.1 - iTime * 5.0) * 0.5 + 0.5;
            float glow = exp(-d * 0.05) * iStrength;

            // Core filament
            float core = smoothstep(4.0, 0.0, d) * pulse;

            float3 finalColor = iColor * (glow * 0.5 + core);

            // Add a subtle wave along the bridge
            float2 dir = normalize(iPosB - iPosA);
            float2 perp = float2(-dir.y, dir.x);
            float distAlong = dot(p - iPosA, dir);
            float wave = sin(distAlong * 0.05 + iTime * 10.0) * 2.0;

            float dWave = lineSegment(p, iPosA + perp * wave, iPosB + perp * wave);
            finalColor += iColor * smoothstep(2.0, 0.0, dWave) * 0.5;

            return half4(finalColor, finalColor.r * 0.8);
        }
    """.trimIndent()
}
