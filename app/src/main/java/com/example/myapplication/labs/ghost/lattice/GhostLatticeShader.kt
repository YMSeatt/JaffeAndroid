package com.example.myapplication.labs.ghost.lattice

import org.intellij.lang.annotations.Language

/**
 * GhostLatticeShader: Contains the AGSL shader source for rendering the social lattice.
 */
object GhostLatticeShader {

    /**
     * NEURAL_LATTICE: A high-performance line shader designed to visualize student connections.
     *
     * **Visual Characteristics:**
     * - **Glow**: Implements a distance-based glow effect using an inverse-square law fallback.
     * - **Pulsing**: Features a moving "energy pulse" that travels along the line based on `iTime`.
     * - **Interference**: If `iType` indicates FRICTION (1.0), it applies a high-frequency sine
     *   wave interference to the alpha channel to simulate instability.
     *
     * **Uniforms:**
     * - `iResolution`: The dimensions of the drawing area.
     * - `iTime`: Accumulated time for animations.
     * - `iStartPos`: Pixel coordinates for the connection start.
     * - `iEndPos`: Pixel coordinates for the connection end.
     * - `iColor`: Normalized RGB color for the connection.
     * - `iStrength`: Intensity multiplier (0.0 to 1.0) derived from student proximity.
     * - `iType`: Connection classification (0=Collaboration, 1=Friction, 2=Neutral).
     */
    @Language("AGSL")
    const val NEURAL_LATTICE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iStartPos;
        uniform float2 iEndPos;
        uniform float3 iColor;
        uniform float iStrength;
        uniform float iType;

        float lineSegment(float2 p, float2 a, float2 b) {
            float2 ba = b - a;
            float2 pa = p - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            return length(pa - ba * h);
        }

        half4 main(float2 fragCoord) {
            float d = lineSegment(fragCoord, iStartPos, iEndPos);
            float thickness = 1.5 + sin(iTime * 3.0 + length(fragCoord) * 0.01) * 0.5;
            float glow = 0.05 / (d * d + 0.001);
            float alpha = smoothstep(thickness + 2.0, thickness, d);
            alpha += glow * iStrength * 0.5;
            float3 color = iColor;
            float2 dir = normalize(iEndPos - iStartPos);
            float posOnLine = dot(fragCoord - iStartPos, dir) / length(iEndPos - iStartPos);
            float pulse = smoothstep(0.1, 0.0, abs(fract(posOnLine - iTime * 0.5) - 0.5) - 0.45);
            color += float3(1.0, 1.0, 1.0) * pulse * iStrength;
            if (iType > 0.5 && iType < 1.5) {
                float interference = sin(posOnLine * 100.0 + iTime * 20.0);
                alpha *= (0.7 + 0.3 * interference);
            }
            return half4(color * alpha * iStrength, alpha * iStrength);
        }
    """
}
