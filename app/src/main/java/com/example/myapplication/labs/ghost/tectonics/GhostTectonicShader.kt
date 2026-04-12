package com.example.myapplication.labs.ghost.tectonics

import org.intellij.lang.annotations.Language

/**
 * GhostTectonicShader: AGSL scripts for the "Ghost Tectonics" layer.
 *
 * This shader visualizes social stress as procedural cracks and "magma glow" in the
 * classroom's background. It uses a combination of fractal noise and distance-based
 * stress mapping.
 *
 * ### Shader Architecture:
 * 1. **Stress Field Generation**: Aggregates normalized stress values from up to 20 student
 *    nodes using a `smoothstep` radial influence (600 units).
 * 2. **Lithosphere Texture**: Uses Fractal Brownian Motion (fbm) to generate a gritty,
 *    rock-like texture that serves as the base layer.
 * 3. **Procedural Cracking**: Applies a threshold to the fbm noise, modulated by the local
 *    stress intensity, to "tear" the background.
 * 4. **Magma Dynamics**: Renders an orange-red glow in high-stress zones, pulsing
 *    sinusoidally to simulate active geological pressure.
 * 5. **Seismic Grain**: Adds high-frequency stochastic noise to simulate environmental
 *    instability.
 *
 * ### Uniforms:
 * - `iResolution`: Standard screen dimensions.
 * - `iTime`: Animation clock for fbm shifting and magma pulsing.
 * - `iNodes`: Array of float3 containing (x, y, stress) for each student.
 * - `iNodeCount`: Number of active nodes passed to the GPU.
 */
object GhostTectonicShader {

    @Language("AGSL")
    const val SOCIAL_TECTONICS = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iNodes[20]; // x, y, stress (normalized)
        uniform int iNodeCount;

        // Simple hash function for noise
        float hash(float2 p) {
            return fract(sin(dot(p, float2(12.9898, 78.233))) * 43758.5453);
        }

        // 2D Noise
        float noise(float2 p) {
            float2 i = floor(p);
            float2 f = fract(p);
            float a = hash(i);
            float b = hash(i + float2(1.0, 0.0));
            float c = hash(i + float2(0.0, 1.0));
            float d = hash(i + float2(1.0, 1.0));
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
        }

        // Fractal Brownian Motion for the "lithosphere" texture
        float fbm(float2 p) {
            float v = 0.0;
            float a = 0.5;
            float2 shift = float2(100.0);
            for (int i = 0; i < 4; i++) {
                v += a * noise(p);
                p = p * 2.0 + shift;
                a *= 0.5;
            }
            return v;
        }

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float2 p = fragCoord;

            float totalStress = 0.0;
            float minEdgeDist = 1.0;

            // 1. Calculate combined stress field from nodes
            for (int i = 0; i < iNodeCount; i++) {
                float2 nodePos = iNodes[i].xy;
                float nodeStress = iNodes[i].z;
                float d = distance(p, nodePos);

                // Stress Influence Radius: 600 logical units
                float influence = smoothstep(600.0, 0.0, d);
                totalStress += influence * nodeStress;
            }

            totalStress = clamp(totalStress, 0.0, 1.0);

            // 2. Generate "Cracks" based on fbm and totalStress
            float n = fbm(p * 0.005 + iTime * 0.1);
            float crack = smoothstep(0.48, 0.5, n * totalStress);

            // "Magma Glow" increases with stress
            float3 magmaColor = float3(1.0, 0.3, 0.1); // Orange/Red
            float3 baseColor = float3(0.05, 0.05, 0.07); // Dark lithosphere

            // Pulsate the magma
            float pulse = sin(iTime * 2.0) * 0.2 + 0.8;
            float3 glow = magmaColor * totalStress * pulse * 0.4;

            float3 finalColor = mix(baseColor, magmaColor * pulse, crack);
            finalColor += glow;

            // Add some "Seismic Grain"
            float grain = hash(fragCoord + iTime) * 0.05;
            finalColor += grain;

            return float4(finalColor, 0.85);
        }
    """
}
