package com.example.myapplication.labs.ghost.origami

import org.intellij.lang.annotations.Language

/**
 * GhostOrigamiShader: AGSL shaders for the Ghost Neural Origami experiment.
 */
object GhostOrigamiShader {
    /**
     * PAPER_CREASE: Renders a "crease" shadow along the folding axis.
     */
    @Language("AGSL")
    const val PAPER_CREASE = """
        uniform float2 iResolution;
        uniform float iFoldProgress;
        uniform float iFoldAxis; // 0.0 to 1.0 (relative width)

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float distToAxis = abs(uv.x - iFoldAxis);

            // Calculate shadow intensity based on fold progress and distance to axis
            float shadow = 1.0 - (exp(-distToAxis * 50.0) * iFoldProgress * 0.8);

            // Darken the color near the crease
            return float4(shadow, shadow, shadow, 1.0);
        }
    """

    /**
     * BACKSIDE_MATERIAL: A procedural texture for the "back" of the seating chart.
     *
     * Features:
     * - Neural Grid: A subtle, glowing data grid.
     * - Fiber Noise: Simulates the texture of a digital "Neural Paper".
     */
    @Language("AGSL")
    const val BACKSIDE_MATERIAL = """
        uniform float2 iResolution;
        uniform float iTime;

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Neural Grid
            float2 gridPos = uv * 20.0;
            float2 gridLine = abs(fract(gridPos - 0.5) - 0.5) / fwidth(gridPos);
            float grid = 1.0 - min(gridLine.x, gridLine.y);
            grid = clamp(grid, 0.0, 1.0) * 0.2;

            // Fiber Noise
            float noise = hash(uv * 1000.0);
            float fiber = pow(noise, 10.0) * 0.1;

            float3 bgColor = float3(0.02, 0.05, 0.1); // Deep Space Blue
            float3 glowColor = float3(0.0, 0.8, 1.0); // Cyan Glow

            float3 color = bgColor + glowColor * grid + glowColor * fiber;

            return float4(color, 1.0);
        }
    """
}
