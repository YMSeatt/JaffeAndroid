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
        uniform float2 iCenters[20];   // Origins of the pulses
        uniform float3 iColors[20];    // Pulse colors
        uniform float iIntensities[20]; // Pulse intensities
        uniform float iRadii[20];      // Pulse expansion radii
        uniform int iNumPulses;        // Active pulse count

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

            float3 totalColor = float3(0.0);
            float totalAlpha = 0.0;

            // Add some "Neural" texture using noise once per fragment
            float n = noise(uv * 10.0 + iTime * 0.5);
            float noiseFactor = 0.8 + 0.4 * n;

            for (int i = 0; i < 20; i++) {
                if (i >= iNumPulses) break;

                float2 center = iCenters[i];
                float radius = iRadii[i];
                float intensity = iIntensities[i];
                float3 color = iColors[i];

                float dist = distance(p, center);

                // The Wave: A ring that expands over time
                float wave = exp(-pow(dist - radius, 2.0) / 400.0);
                wave *= noiseFactor;

                // Fade out as it expands
                float life = clamp(1.0 - (radius / 1000.0), 0.0, 1.0);

                float alpha = wave * intensity * life;

                // Accumulate color and alpha
                totalColor += color * (wave + 0.2) * alpha;
                totalAlpha += alpha;
            }

            return float4(totalColor, totalAlpha * 0.3);
        }
    """
}
