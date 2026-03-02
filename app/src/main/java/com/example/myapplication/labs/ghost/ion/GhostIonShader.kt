package com.example.myapplication.labs.ghost.ion

import org.intellij.lang.annotations.Language

/**
 * GhostIonShader: AGSL shaders for the "Ghost Ion" experiment.
 *
 * Visualizes classroom energy as ionized gas clouds and electrostatic
 * discharge patterns.
 */
object GhostIonShader {

    @Language("AGSL")
    const val ION_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float iGlobalBalance; // -1.0 to 1.0

        // Ion Points (x, y, charge, density)
        uniform float4 iPoints[10];
        uniform int iPointCount;

        // Simple Hash for procedural noise
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

        float4 main(float2 fragCoord) {
            float2 uv = fragCoord / iResolution.xy;
            float3 finalColor = float3(0.0);

            // Base background glow based on global balance
            float3 bgBase = mix(float3(0.1, 0.02, 0.02), float3(0.02, 0.05, 0.1), iGlobalBalance * 0.5 + 0.5);
            finalColor = bgBase;

            // Ionic cloud simulation
            float n = noise(uv * 4.0 + iTime * 0.2);
            n += 0.5 * noise(uv * 8.0 - iTime * 0.4);

            for (int i = 0; i < iPointCount; i++) {
                float2 p = iPoints[i].xy / iResolution.xy;
                float charge = iPoints[i].z;
                float density = iPoints[i].w;

                float d = length(uv - p);

                // Ion Core
                float core = smoothstep(0.1 * density, 0.0, d);
                float3 ionColor = mix(float3(1.0, 0.2, 0.2), float3(0.2, 0.8, 1.0), charge * 0.5 + 0.5);

                finalColor += ionColor * core * 0.5;

                // Electrostatic discharge (Flicker/Lightning)
                float discharge = step(0.98, hash(uv + iTime)) * step(d, 0.2 * density) * n;
                finalColor += float3(0.8, 0.9, 1.0) * discharge;
            }

            // Atmospheric Ionization (Overall haze)
            finalColor += bgBase * n * 0.3;

            return float4(finalColor, 0.6); // Semi-transparent overlay
        }
    """
}
