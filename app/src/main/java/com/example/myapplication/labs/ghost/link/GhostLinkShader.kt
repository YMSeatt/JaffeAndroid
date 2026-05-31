package com.example.myapplication.labs.ghost.link

import org.intellij.lang.annotations.Language

/**
 * GhostLinkShader: AGSL effects for Neural Link.
 *
 * Features "Neural Strands" with glowing filaments and data pulses.
 */
object GhostLinkShader {

    @Language("AGSL")
    val NEURAL_STRAND = """
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

        half4 main(float2 fragCoord) {
            float2 p = fragCoord;

            // Core Strand
            float d = lineSegment(p, iPosA, iPosB);

            // Animated data pulse moving from A to B
            float2 dir = normalize(iPosB - iPosA);
            float distAlong = dot(p - iPosA, dir);
            float bridgeLen = length(iPosB - iPosA);

            float pulsePos = fract(iTime * 0.5) * bridgeLen;
            float pulse = exp(-pow(distAlong - pulsePos, 2.0) / 1000.0);

            // Ethereal glow
            float glow = exp(-d * 0.08) * iStrength;

            // Sharp filament core
            float core = smoothstep(3.0, 0.0, d);

            float3 finalColor = iColor * (glow * 0.4 + core * 0.6 + pulse * core * 1.5);

            // Add subtle chromatic distortion at the edges of the glow
            finalColor.r += glow * 0.1 * sin(iTime * 2.0);
            finalColor.b += glow * 0.1 * cos(iTime * 2.0);

            return half4(finalColor, glow * 0.9);
        }
    """.trimIndent()
}
