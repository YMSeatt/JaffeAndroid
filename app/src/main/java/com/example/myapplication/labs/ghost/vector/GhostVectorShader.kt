package com.example.myapplication.labs.ghost.vector

import org.intellij.lang.annotations.Language

/**
 * GhostVectorShader: AGSL scripts for visualizing social gravity vectors.
 */
object GhostVectorShader {

    /**
     * Renders a glowing, directional "Social Needle" that indicates the magnitude
     * and direction of the social forces acting on a student.
     *
     * Uniforms:
     * - [iResolution]: Dimensions of the canvas.
     * - [iTime]: Elapsed time for animation.
     * - [iCenter]: The origin point of the vector (student center).
     * - [iAngle]: The direction of the force in radians.
     * - [iMagnitude]: The intensity of the force, driving length and brightness.
     * - [iColor]: The base color for the vector (usually cyan or magenta).
     */
    @Language("AGSL")
    const val VECTOR_NEEDLE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;
        uniform float iAngle;
        uniform float iMagnitude;
        uniform float3 iColor;

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 center = iCenter / iResolution.xy;
            float2 p = uv - center;
            p.x *= iResolution.x / iResolution.y;

            // Apply rotation to align with the force angle
            float s = sin(-iAngle);
            float c = cos(-iAngle);
            float2 pr = float2(p.x * c - p.y * s, p.x * s + p.y * c);

            // Scale needle size based on magnitude
            float magScale = clamp(iMagnitude / 150.0, 0.2, 1.2);
            float needleLength = 0.08 * magScale;
            float thickness = 0.002;

            // Draw the main needle body
            float line = smoothstep(thickness, 0.0, abs(pr.y))
                         * smoothstep(needleLength, needleLength - 0.01, pr.x)
                         * smoothstep(0.0, 0.005, pr.x);

            // Draw the arrow tip
            float tip = smoothstep(0.012, 0.0, abs(pr.y) + (pr.x - needleLength) * 2.0)
                        * step(needleLength - 0.02, pr.x)
                        * step(pr.x, needleLength);

            float mask = max(line, tip);
            float3 color = iColor * mask;

            // Add a "Neural Flow" trail effect
            float trail = smoothstep(0.02, 0.0, abs(pr.y)) * exp(-pr.x * 30.0) * 0.4;
            float flow = sin(iTime * 20.0 - pr.x * 80.0) * 0.5 + 0.5;
            color += iColor * trail * flow;

            return float4(color, (mask * 0.9 + trail * 0.4) * clamp(magScale * 2.0, 0.5, 1.0));
        }
    """
}
