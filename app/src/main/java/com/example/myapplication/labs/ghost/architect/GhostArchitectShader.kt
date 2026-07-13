package com.example.myapplication.labs.ghost.architect

import org.intellij.lang.annotations.Language

/**
 * GhostArchitectShader: AGSL-powered visual language for the Architect engine.
 *
 * Includes:
 * 1. A blueprint grid background with a cyan glow.
 * 2. Trajectory beam effects for proposed moves.
 */
object GhostArchitectShader {

    /**
     * Renders a procedural blueprint grid with primary and secondary lines.
     * Includes a vertical scanning line effect to signify active analysis.
     *
     * Uniforms:
     * - [uResolution]: Screen dimensions for UV calculation.
     * - [uTime]: Driving force for the scanning line animation.
     * - [uAlpha]: Global transparency control for layer transitions.
     */
    @Language("AGSL")
    const val BLUEPRINT_SHADER = """
        uniform float2 uResolution;
        uniform float uTime;
        uniform float uAlpha;

        float grid(float2 p, float size) {
            float2 g = abs(mod(p, size) - size * 0.5);
            return smoothstep(0.0, 1.5, min(g.x, g.y));
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / uResolution;
            float2 p = fragCoord;

            // Primary Grid
            float g1 = 1.0 - grid(p, 100.0);
            // Secondary Grid
            float g2 = 1.0 - grid(p, 20.0);

            float3 gridColor = float3(0.0, 0.8, 1.0); // Cyan Blueprint
            float3 color = gridColor * g1 * 0.4 + gridColor * g2 * 0.1;

            // Scanning Line
            float scan = smoothstep(0.98, 1.0, sin(uv.y * 10.0 - uTime * 2.0) * 0.5 + 0.5);
            color += gridColor * scan * 0.2;

            return half4(color * uAlpha, uAlpha * 0.15);
        }
    """

    /**
     * Renders a glowing "Neural Trajectory" beam between two points.
     *
     * Mathematical Logic:
     * 1. Uses a signed distance field (SDF) for a line segment.
     * 2. Applies [smoothstep] to the distance to create a soft, glowing beam.
     * 3. Scales [thickness] and shifts [color] based on the move's [uWeight].
     *
     * Uniforms:
     * - [uStart], [uEnd]: Screen-space logical coordinates for the beam.
     * - [uWeight]: Heuristic importance (0.0 to 1.0). Weights > 0.7 trigger Magenta shift.
     */
    @Language("AGSL")
    const val TRAJECTORY_SHADER = """
        uniform float2 uStart;
        uniform float2 uEnd;
        uniform float uTime;
        uniform float uWeight;

        float lineSegment(float2 p, float2 a, float2 b) {
            float2 pa = p - a, ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float d = lineSegment(fragCoord, uStart, uEnd);

            // Pulsing beam
            float pulse = sin(uTime * 5.0) * 0.5 + 0.5;
            float thickness = 2.0 + uWeight * 4.0;
            float intensity = smoothstep(thickness, 0.0, d);

            float3 color = float3(0.0, 1.0, 0.8); // Neural Green-Cyan
            if (uWeight > 0.7) color = float3(1.0, 0.2, 0.5); // Focus/Priority moves are Magenta

            return half4(color * intensity * (0.5 + pulse * 0.5), intensity);
        }
    """
}
