package com.example.myapplication.labs.ghost.entropy

import org.intellij.lang.annotations.Language

/**
 * GhostEntropyShader: An AGSL shader that simulates "Neural Turbulence" and "Thermal Distortion."
 *
 * This shader distorts the student icon based on its current entropy score.
 * Higher entropy results in more visual "heat" and "flicker."
 */
object GhostEntropyShader {

    @Language("AGSL")
    const val ENTROPY_DISTORTION = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iEntropy; // 0.0 to 1.0
        uniform shader contents;

        /**
         * Simplex noise-like function for procedural turbulence.
         * Uses a pseudo-random hash to generate non-periodic fluctuations.
         */
        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        /**
         * Bilinear noise function for smooth gradients.
         */
        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                       mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
        }

        float4 main(float2 fragCoord) {
            // Normalize coordinates
            float2 uv = fragCoord / iResolution.xy;

            /**
             * Apply entropy-driven thermal distortion.
             * The intensity of the UV offset is directly proportional to iEntropy,
             * creating a "shimmer" effect that scales with neural turbulence.
             */
            float distortionIntensity = iEntropy * 0.05;
            float speed = 5.0;
            float frequency = 20.0;

            float noiseVal = noise(uv * frequency + iTime * speed);
            float2 offset = float2(noiseVal - 0.5, noiseVal - 0.5) * distortionIntensity;

            // Sample the underlying student icon with distorted UVs
            float4 color = contents.eval(fragCoord + offset * iResolution.xy);

            // Add a subtle entropy-driven glow (Magenta for high entropy)
            float3 entropyColor = float3(1.0, 0.0, 0.8); // Magenta
            color.rgb = mix(color.rgb, entropyColor, iEntropy * 0.3 * noiseVal);

            return color;
        }
    """
}
