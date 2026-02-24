package com.example.myapplication.labs.ghost.warp

import org.intellij.lang.annotations.Language

/**
 * GhostWarpShader: AGSL shaders for the "Ghost Warp" experiment.
 *
 * Implements a "Neural Spacetime Dilation" effect where the coordinate system
 * is distorted based on the "mass" (behavioral intensity) of student nodes.
 */
object GhostWarpShader {

    @Language("AGSL")
    const val NEURAL_WARP = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iIntensity; // Global intensity toggle

        // Student Mass Points (x, y, mass, radius)
        // Using a fixed array size for the PoC
        uniform float4 iPoints[10];
        uniform int iPointCount;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 warpedUv = uv;

            float2 totalOffset = float2(0.0);

            for (int i = 0; i < iPointCount; i++) {
                float2 p = iPoints[i].xy / iResolution.xy;
                float mass = iPoints[i].z;
                float radius = iPoints[i].w / iResolution.x;

                float2 dir = uv - p;
                float dist = length(dir);

                // Gravity Well effect:
                // Displacement is proportional to mass and follows an inverse-distance curve
                // but smoothed to avoid singularities.
                if (dist > 0.001) {
                    float pull = (mass * 0.05) / (dist * dist / radius + 0.1);
                    totalOffset -= normalize(dir) * pull * iIntensity;
                }
            }

            warpedUv += totalOffset;

            // Background Pattern (Grid)
            float2 grid = fract(warpedUv * 20.0);
            float line = smoothstep(0.02, 0.0, abs(grid.x - 0.5)) +
                         smoothstep(0.02, 0.0, abs(grid.y - 0.5));

            float3 color = mix(float3(0.02, 0.05, 0.1), float3(0.0, 0.5, 0.8), line * 0.3);

            // Add some "event horizon" glow near mass points
            for (int i = 0; i < iPointCount; i++) {
                float2 p = iPoints[i].xy / iResolution.xy;
                float dist = length(warpedUv - p);
                float glow = 0.001 / (dist + 0.01);
                color += float3(0.0, 0.8, 1.0) * glow * iIntensity;
            }

            return float4(color, 1.0);
        }
    """
}
