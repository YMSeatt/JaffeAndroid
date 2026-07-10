package com.example.myapplication.labs.ghost.scape

import org.intellij.lang.annotations.Language

/**
 * GhostScapeShader: AGSL shaders for the Ghost Scape neural audio visualization.
 */
object GhostScapeShader {
    @Language("AGSL")
    const val SCAPE_RIPPLE = """
        uniform float2 iResolution;
        uniform float2 iCenter;
        uniform float iRadius;
        uniform float iIntensity;
        uniform float4 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution;
            float2 center = iCenter / iResolution;
            float aspect = iResolution.x / iResolution.y;

            float2 diff = (uv - center);
            diff.x *= aspect;
            float dist = length(diff);

            float radius = iRadius / iResolution.x;
            float ringWidth = 0.05;

            // Create a sharp expanding ripple
            float ripple = smoothstep(radius - ringWidth, radius, dist) *
                           smoothstep(radius + ringWidth, radius, dist);

            // Add a subtle inner glow
            float glow = exp(-5.0 * abs(dist - radius)) * 0.5;

            float finalAlpha = (ripple + glow) * iIntensity * (1.0 - radius * 2.0);
            return iColor * clamp(finalAlpha, 0.0, 1.0);
        }
    """
}
