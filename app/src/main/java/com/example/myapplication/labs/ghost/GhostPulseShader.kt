package com.example.myapplication.labs.ghost

import org.intellij.lang.annotations.Language

/**
 * GhostPulseShader: AGSL scripts for the "Neural Pulse" resonance visualization.
 *
 * This shader renders expanding ripples that represent "Data Waves" moving through
 * the classroom. It uses a combination of radial distance functions and
 * fractal noise to create an organic, pulsating look.
 */
object GhostPulseShader {
    @Language("AGSL")
    const val NEURAL_PULSE = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float2 iCenter;       // Origin of the pulse
        uniform float3 iColor;        // Pulse color
        uniform float iIntensity;     // How strong the pulse is (0.0 to 1.0)
        uniform float iRadius;        // Current expansion radius

        float hash(float2 p) {
            return fract(sin(dot(p, float2(127.1, 311.7))) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(hash(i + float2(0.0, 0.0)), hash(i + float2(1.0, 0.0)), u.x),
                       mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord;

            float dist = distance(p, iCenter);

            // The Wave: A ring that expands over time
            float wave = exp(-pow(dist - iRadius, 2.0) / 400.0);

            // Add some "Neural" texture using noise
            float n = noise(uv * 10.0 + iTime * 0.5);
            wave *= 0.8 + 0.4 * n;

            // Fade out as it expands
            float life = clamp(1.0 - (iRadius / 1000.0), 0.0, 1.0);

            float alpha = wave * iIntensity * life;

            // Color with inner glow
            float3 finalColor = iColor * (wave + 0.2);

            return float4(finalColor * alpha, alpha * 0.3);
        }
    """
}
