package com.example.myapplication.labs.ghost.entanglement

import org.intellij.lang.annotations.Language

/**
 * GhostEntanglementShader: AGSL-powered visualizer for quantum synchronicity.
 *
 * This shader implements the visual metaphors for the Ghost Entanglement engine:
 * 1. **Quantum Ripples**: Radial interference patterns centered on entangled students.
 * 2. **Social Bridges**: Glowing paths connecting highly coherent student nodes.
 *
 * The shader utilizes distance-based exponential decay for glows and sinusoidal
 * waves for pulsating "energy" movement.
 */
object GhostEntanglementShader {

    /**
     * The primary AGSL source for the Ghost Entanglement effect.
     *
     * ### Uniform Definitions:
     * - `iResolution`: Screen dimensions for UV normalization.
     * - `iTime`: Global animation time in radians.
     * - `iEntangledPosA / iEntangledPosB`: Screen coordinates of the two entangled students.
     * - `iCoherence`: The coherence score (0..1) driving visual intensity.
     * - `iColor`: The RGB color assigned to the connection (e.g., Cyan for positive).
     */
    @Language("AGSL")
    const val QUANTUM_RIPPLES = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iEntangledPosA;
        uniform float2 iEntangledPosB;
        uniform float iCoherence;
        uniform float3 iColor;

        /** Calculates an expanding radial ripple with exponential intensity decay. */
        float ripple(float2 uv, float2 center, float time, float coherence) {
            float dist = distance(uv, center);
            float strength = exp(-dist * 10.0);
            float wave = sin(dist * 30.0 - time * 5.0) * 0.5 + 0.5;
            return strength * wave * coherence;
        }

        /** Renders a glowing connection line between two normalized points. */
        float line(float2 uv, float2 a, float2 b, float time, float coherence) {
            float2 pa = uv - a;
            float2 ba = b - a;
            float h = clamp(dot(pa, ba) / dot(ba, ba), 0.0, 1.0);
            float dist = distance(uv, a + ba * h);

            // Pulse energy along the length of the bridge
            float pulse = sin(h * 10.0 - time * 8.0) * 0.5 + 0.5;
            float glow = exp(-dist * 25.0) * pulse * coherence;

            return glow;
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 posA = iEntangledPosA / iResolution.xy;
            float2 posB = iEntangledPosB / iResolution.xy;

            float rA = ripple(uv, posA, iTime, iCoherence);
            float rB = ripple(uv, posB, iTime, iCoherence);
            float l = line(uv, posA, posB, iTime, iCoherence);

            float total = clamp(rA + rB + l, 0.0, 1.0);

            // Subtle digital grid background
            float2 grid = fract(fragCoord * 0.05);
            float gridVal = (grid.x < 0.02 || grid.y < 0.02) ? 0.05 : 0.0;

            return half4(iColor * (total + gridVal), total * 0.8 + gridVal);
        }
    """
}
