package com.example.myapplication.labs.ghost.glitch

import org.intellij.lang.annotations.Language

/**
 * GhostGlitchShader: Specialized AGSL scripts for the Ghost Glitch experience.
 *
 * This shader simulates digital corruption and "Neural Feedback" using:
 * - Chromatic Aberration: Splitting RGB channels based on glitch intensity.
 * - Digital Noise: Procedural "blocks" of color distortion.
 * - Horizontal Shift: Scanline-based pixel displacement.
 */
object GhostGlitchShader {
    @Language("AGSL")
    const val NEURAL_GLITCH = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity; // 0.0 to 1.0

        float hash(float n) { return fract(sin(n) * 43758.5453123); }

        float noise(float2 p) {
            return hash(p.x + p.y * 57.0);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Apply intensity-based horizontal shift
            float shift = 0.0;
            if (iIntensity > 0.1) {
                float block = floor(uv.y * 20.0 + iTime * 10.0);
                if (hash(block) < iIntensity * 0.5) {
                    shift = (hash(block + 1.0) - 0.5) * iIntensity * 0.2;
                }
            }

            float2 uvR = uv + float2(shift + 0.01 * iIntensity, 0.0);
            float2 uvG = uv + float2(shift, 0.0);
            float2 uvB = uv + float2(shift - 0.01 * iIntensity, 0.0);

            // Mock colors for the "feedback" effect (Cyan/Magenta dominance)
            float r = noise(uvR * 10.0 + iTime) * 0.5 * iIntensity;
            float g = noise(uvG * 10.0 + iTime) * 0.8 * iIntensity;
            float b = noise(uvB * 10.0 + iTime) * 1.0 * iIntensity;

            float alpha = iIntensity * 0.4;

            // Add horizontal scanlines
            float scanline = sin(uv.y * 800.0) * 0.1 * iIntensity;

            return float4(float3(r, g, b) + scanline, alpha);
        }
    """
}
