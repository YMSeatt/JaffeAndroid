package com.example.myapplication.labs.ghost.flora

import org.intellij.lang.annotations.Language

/**
 * GhostFloraShader: AGSL procedural flower generation logic.
 *
 * This shader implements organic botanical forms using polar coordinate warping.
 * It is designed for zero-allocation performance by using purely procedural math.
 */
object GhostFloraShader {

    /**
     * AGSL source for the Flora visualizer.
     *
     * Uniforms:
     * - `size`: The viewport size for the individual flower (usually student icon width * 2).
     * - `time`: Global animation time for the swaying effect.
     * - `growth`: Maps to academic performance; affects the `petal()` distance function.
     * - `vitality`: Maps to behavior; interpolates between Cyan (1.0) and Magenta (0.0).
     * - `complexity`: Maps to activity frequency; determines the number of petal divisions.
     * - `seed`: Provides stable variation between different student flowers.
     */
    @Language("AGSL")
    const val FLORA_SHADER_SRC = """
        uniform float2 size;
        uniform float time;
        uniform float growth;
        uniform float vitality;
        uniform float complexity;
        uniform float seed;

        // 2D Rotation matrix
        float2 rotate(float2 p, float a) {
            float s = sin(a);
            float c = cos(a);
            return float2(p.x * c - p.y * s, p.x * s + p.y * c);
        }

        // Pseudo-random noise
        float hash(float2 p) {
            return frac(sin(dot(p, float2(12.9898, 78.233) + seed)) * 43758.5453);
        }

        // Procedural Petal Shape using Polar Coordinates
        float petal(float2 p, float count, float width) {
            float r = length(p);
            float a = atan(p.y, p.x);
            // Warp the angle to create petal divisions
            float petalShape = cos(a * count) * width;
            return smoothstep(growth + petalShape, growth + petalShape - 0.05, r);
        }

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * size) / min(size.y, size.x);

            // Apply rotation over time
            uv = rotate(uv, time * 0.2 + seed * 6.28);

            // Layered Petals (Complexity drives petal count)
            float petalCount = floor(5.0 + complexity * 8.0);
            float p1 = petal(uv, petalCount, 0.15);
            float p2 = petal(rotate(uv, 0.5), petalCount, 0.1);

            // Dynamic Colors based on Vitality
            // High Vitality -> Cyan (0, 1, 1), Low Vitality -> Magenta (1, 0, 1)
            float3 positiveColor = float3(0.0, 0.8, 0.9); // Cyan
            float3 negativeColor = float3(0.9, 0.1, 0.6); // Magenta
            float3 baseColor = mix(negativeColor, positiveColor, vitality);

            // Add center glow
            float center = 1.0 - smoothstep(0.0, 0.2, length(uv));
            float3 finalColor = baseColor * (p1 + p2 * 0.6) + float3(1.0, 1.0, 0.8) * center;

            // Final Alpha (Transparent background)
            float alpha = (p1 + p2 * 0.5 + center) * 0.8;

            return half4(finalColor * alpha, alpha);
        }
    """
}
