package com.example.myapplication.labs.ghost.vision

import org.intellij.lang.annotations.Language

/**
 * GhostVisionShader: AGSL Neural AR Viewport Shaders.
 *
 * This object contains futuristic, high-performance shaders for rendering
 * an AR HUD. It includes:
 * - `AR_HUD`: Scanning lines, data brackets, and a "Neural Static" overlay.
 * - `VISION_GLYPH`: A pulsating neural data ring for student orientation nodes.
 */
object GhostVisionShader {

    @Language("AGSL")
    const val AR_HUD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iStaticIntensity;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // 1. Futuristic Scanning Lines
            float scanline = sin(uv.y * 100.0 + iTime * 5.0) * 0.05;

            // 2. Neural Static / Noise
            float noise = fract(sin(dot(uv + iTime, float2(12.9898, 78.233))) * 43758.5453);
            half4 noiseColor = half4(0.0, 1.0, 1.0, noise * 0.1 * iStaticIntensity);

            // 3. Grid Pattern
            float2 grid = fract(uv * 10.0);
            float gridLine = step(0.98, grid.x) + step(0.98, grid.y);
            half4 gridColor = half4(0.0, 1.0, 1.0, gridLine * 0.05);

            // 4. Edge Vignette
            float vignette = 1.0 - length(uv - 0.5) * 1.5;

            half4 finalColor = gridColor + noiseColor;
            finalColor.rgb += scanline;
            finalColor.a = (finalColor.a + 0.1) * vignette;

            return finalColor * half4(0.0, 0.8, 1.0, 1.0); // Ghost Cyan Tint
        }
    """

    @Language("AGSL")
    const val VISION_GLYPH = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iPulse;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float dist = length(uv - 0.5);

            // Pulse Ring
            float ring = smoothstep(0.4 + iPulse * 0.1, 0.41 + iPulse * 0.1, dist) -
                         smoothstep(0.42 + iPulse * 0.1, 0.43 + iPulse * 0.1, dist);

            // Data Brackets (Mock)
            float brackets = step(0.45, abs(uv.x - 0.5)) * step(0.45, abs(uv.y - 0.5));

            half4 color = half4(0.0, 1.0, 1.0, (ring + brackets * 0.5));
            return color;
        }
    """
}
