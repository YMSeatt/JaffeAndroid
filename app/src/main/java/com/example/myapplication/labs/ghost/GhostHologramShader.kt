package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostHologramShader: AGSL scripts for the 3D Holographic Parallax effect.
 *
 * This shader provides the "glassy" holographic overlay that responds to tilt.
 * It features chromatic aberration, scanlines, and a flickering "depth" glow.
 */
object GhostHologramShader {
    @Language("AGSL")
    const val HOLOGRAM_GLASS = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iTilt; // x: roll, y: pitch
        uniform float3 iColor;
        uniform float iFlicker;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Chromatic Aberration based on tilt
            float shift = length(iTilt) * 0.01;
            float r = 0.0; // Placeholder for texture lookup if we had one
            float g = 0.0;
            float b = 0.0;

            // Scanlines
            float scanline = sin(uv.y * 500.0 + iTime * 20.0) * 0.05;

            // Vertical glitch
            float glitch = 0.0;
            if (sin(iTime * 10.0 + uv.y * 5.0) > 0.98) {
                glitch = 0.1;
            }

            // Holographic Blue Glow
            float3 color = iColor;
            float alpha = 0.1 + scanline + glitch;

            // Edge Glow
            float edge = distance(uv, float2(0.5, 0.5));
            alpha += edge * 0.2;

            // Flicker
            alpha *= iFlicker;

            return float4(color, alpha * 0.3);
        }
    """

}
