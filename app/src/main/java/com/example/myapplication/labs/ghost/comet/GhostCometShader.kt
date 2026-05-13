package com.example.myapplication.labs.ghost.comet

import org.intellij.lang.annotations.Language

/**
 * GhostCometShader: AGSL shader for rendering glowing comet trails.
 *
 * This shader renders tapering, high-energy streaks that follow the comets.
 */
object GhostCometShader {
    @Language("AGSL")
    const val COMET_TRAIL = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[12];
        uniform float iLife;
        uniform half4 iColor;

        float distanceToSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float opacity = 0.0;

            for (int i = 0; i < 11; i++) {
                float d = distanceToSegment(fragCoord, iPoints[i], iPoints[i+1]);
                // Taper the width based on index (tail is thinner)
                float width = mix(2.0, 10.0, float(i) / 11.0);
                float glow = exp(-d / width);
                opacity += glow * (float(i) / 11.0);
            }

            opacity = clamp(opacity, 0.0, 1.0) * iLife;
            return iColor * opacity;
        }
    """
}
