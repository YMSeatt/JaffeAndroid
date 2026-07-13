package com.example.myapplication.labs.ghost.silhouette

import org.intellij.lang.annotations.Language

/**
 * GhostSilhouetteShader: AGSL shaders for the drag placeholder effect.
 */
object GhostSilhouetteShader {
    @Language("AGSL")
    const val SILHOUETTE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;
        uniform float2 iSize;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 halfSize = iSize * 0.5;
            float2 localPos = fragCoord - iCenter;
            float2 d = abs(localPos) - halfSize;
            float dist = length(max(d, 0.0)) + min(max(d.x, d.y), 0.0);

            // Glow effect
            float glow = 0.02 / (abs(dist) + 0.01);

            // Pulse
            glow *= (sin(iTime * 4.0) * 0.2 + 0.8);

            // Edge detection for a clean outline
            float outline = smoothstep(2.0, 0.0, abs(dist));

            float alpha = (glow + outline) * 0.6;
            float3 finalColor = iColor * (glow + outline);

            return float4(finalColor * alpha, alpha);
        }
    """
}
