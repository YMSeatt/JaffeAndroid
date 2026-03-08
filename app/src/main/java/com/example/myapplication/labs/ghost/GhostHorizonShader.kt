package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostHorizonShader: AGSL code for the "Neural Horizon" effect.
 *
 * This shader renders a dynamic, context-aware background that reacts to
 * ambient light and simulated atmospheric pressure.
 */
object GhostHorizonShader {

    @Language("AGSL")
    const val NEURAL_HORIZON = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iLight;      // 0.0 (Dark) to 1.0 (Bright)
        uniform float iPressure;   // -1.0 to 1.0 verticality shift

        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;

            // Adjust horizon line based on pressure
            float horizonY = 0.5 + iPressure * 0.1;

            // Color Spectrum: Dark (Amber) vs Bright (Cyan)
            half3 colorAmber = half3(1.0, 0.4, 0.1);
            half3 colorCyan = half3(0.0, 0.8, 1.0);
            half3 baseColor = mix(colorAmber, colorCyan, iLight);

            // Atmospheric Gradient
            float dist = abs(uv.y - horizonY);
            float glow = exp(-dist * 5.0);

            half3 finalColor = baseColor * glow * (0.5 + 0.5 * sin(iTime * 0.5));

            // Add some "Neural Noise"
            float n = hash(uv + iTime * 0.01);
            finalColor += baseColor * n * 0.05 * (1.0 - iLight); // More noise in dark mode

            // Darken the edges for depth
            float vignette = uv.x * (1.0 - uv.x) * uv.y * (1.0 - uv.y) * 15.0;
            finalColor *= pow(vignette, 0.2);

            return half4(finalColor * 0.3, 1.0);
        }
    """
}
