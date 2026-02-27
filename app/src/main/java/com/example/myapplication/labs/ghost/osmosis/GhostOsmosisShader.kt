package com.example.myapplication.labs.ghost.osmosis

import org.intellij.lang.annotations.Language

/**
 * GhostOsmosisShader: AGSL script for fluid diffusion and osmotic pressure.
 *
 * Visualizes the Knowledge Diffusion Field with moving fluid-like patterns.
 */
object GhostOsmosisShader {
    @Language("AGSL")
    const val DIFFUSION_FIELD = """
        uniform float2 iResolution;
        uniform float iTime;
        uniform float3 iColor;
        uniform float iPressure; // 0..1

        // Simple Hash function for procedural noise
        float hash(float2 p) {
            p = fract(p * float2(123.34, 456.21));
            p += dot(p, p + 45.32);
            return fract(p.x * p.y);
        }

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

            // Flow simulation
            float2 flow = uv * 5.0;
            flow.x += iTime * 0.2;
            flow.y += sin(iTime * 0.5 + uv.x * 2.0) * 0.1;

            float n = noise(flow);

            // Diffusion intensity
            float intensity = n * iPressure;

            float3 finalColor = iColor * (0.2 + 0.8 * intensity);

            // Add a subtle "osmotic" glow
            float glow = 0.02 / (abs(n - 0.5) + 0.01);
            finalColor += iColor * glow * 0.1;

            return float4(finalColor, intensity * 0.6);
        }
    """
}
