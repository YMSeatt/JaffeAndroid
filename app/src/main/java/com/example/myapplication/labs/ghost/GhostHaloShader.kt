package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostHaloShader: AGSL source for the "Neural Halo" peak performer effect.
 *
 * This shader renders a thin, rotating, and pulsing ring with an ethereal scatter.
 * It uses a golden/cyan color palette to represent high academic and behavioral stability.
 */
object GhostHaloShader {
    @Language("AGSL")
    const val NEURAL_HALO = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform half4 iColor; // Primary Halo Color (Golden/Cyan)

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * iResolution.xy) / min(iResolution.y, iResolution.x);

            // Polar coordinates
            float r = length(uv);
            float angle = atan2(uv.y, uv.x);

            // Halo parameters
            float radius = 0.35 + 0.02 * sin(iTime * 2.0);
            float thickness = 0.015 + 0.005 * cos(iTime * 1.5 + angle * 3.0);

            // The Ring
            float ring = smoothstep(thickness, 0.0, abs(r - radius));

            // Ethereal Scatter / Glow
            float scatter = exp(-abs(r - radius) * 15.0);

            // Rotation Sparkles
            float sparkles = 0.5 + 0.5 * sin(angle * 5.0 + iTime * 4.0);
            float finalGlow = ring + scatter * 0.4 * sparkles;

            // Chromatic shift based on angle
            half4 colorShift = iColor;
            colorShift.rgb += 0.1 * sin(angle + iTime);

            return colorShift * finalGlow;
        }
    """
}
