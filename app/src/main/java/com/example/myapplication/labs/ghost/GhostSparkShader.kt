package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostSparkShader: AGSL scripts for the high-performance particle renderer.
 *
 * This shader renders a collection of discrete, glowing "Data Sparks" by mapping
 * uniform-passed coordinates into a distance-field field.
 */
object GhostSparkShader {
    @Language("AGSL")
    const val NEURAL_SPARK = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iPoints[100]; // Max 100 particles for performance in a single draw
        uniform float3 iColors[100];  // Particle colors (0..1 RGB)
        uniform float iLives[100];   // Particle life (1.0 down to 0.0)
        uniform float iSizes[100];   // Particle sizes
        uniform int iNumParticles;

        float4 main(float2 fragCoord) {
            float3 finalColor = float3(0.0);
            float finalAlpha = 0.0;

            // Iterate over active particles
            for (int i = 0; i < 100; i++) {
                if (i >= iNumParticles) break;

                // Distance to point in screen space
                float d = distance(fragCoord, iPoints[i]);

                // Particle radius in pixels
                float radius = iSizes[i] * iLives[i] * 2.0; // Scale for visibility

                // Glow falloff
                float glow = exp(-3.0 * d / radius);

                // Accumulate color weighted by glow and life
                finalColor += iColors[i] * glow * iLives[i];
                finalAlpha += glow * iLives[i];
            }

            // Clamping and bloom effect
            finalColor = clamp(finalColor, 0.0, 1.2);
            finalAlpha = clamp(finalAlpha, 0.0, 0.8);

            // Background "Neural Deep" tint
            float3 background = float3(0.005, 0.01, 0.02) * (1.0 - finalAlpha);

            return float4(finalColor + background, finalAlpha);
        }
    """
}
